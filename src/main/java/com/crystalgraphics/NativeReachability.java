package com.crystalgraphics;

/**
 * Keeps an object with a finalizer alive across a native call that uses its handle.
 *
 * <h3>The problem</h3>
 *
 * <p>Every wrapper in this module holds a native pointer in a field and frees it from a finalizer as
 * a leak backstop. That combination has a race that looks impossible from the source:
 *
 * <pre>{@code
 * public int getGlyphData(int[] infoOut, int[] posOut) {
 *     return nGetGlyphData(nativePtr, infoOut, posOut);   // <-- buffer may be freed during this call
 * }
 * }</pre>
 *
 * <p>Once {@code nativePtr} has been read into the argument, {@code this} is never touched again.
 * The JIT knows that, so as far as the collector is concerned the wrapper is unreachable from the
 * moment the field load completes — <em>while the native call it belongs to is still running</em>.
 * If a GC happens in that window the finalizer runs on the finalizer thread and calls
 * {@code hb_buffer_destroy} on a pointer the native code is actively using. The result is a
 * use-after-free: {@code EXCEPTION_ACCESS_VIOLATION} inside the JNI library, at a pc that usually
 * points nowhere useful, on a call site that appears entirely safe.
 *
 * <p>This is not theoretical here — it is the cause of the intermittent JVM crashes in the bindings
 * tests. It is also load-timing-dependent, so it hides for long stretches and reappears whenever
 * something unrelated changes GC timing, which makes it very easy to misattribute to whatever was
 * edited last.
 *
 * <h3>Why not Reference.reachabilityFence</h3>
 *
 * <p>That is the correct tool and does exactly this, but it arrived in Java 9. This module compiles
 * to Java 8 bytecode and runs on a Java 8 VM under MC 1.7.10, where calling it is a
 * {@code NoSuchMethodError} at runtime rather than a compile error — the worst failure mode
 * available. The synchronized-block form below is what {@code reachabilityFence}'s own
 * documentation names as the pre-Java-9 equivalent: entering a monitor on the object is a use of
 * it, so the reference must stay live until the fence is passed.
 *
 * <p>The monitor itself does nothing. It is never contended, these wrappers are not shared across
 * threads under a lock, and the block is empty. Only the reachability side effect is wanted.
 *
 * <h3>Using it</h3>
 *
 * <p>Call it in a {@code finally} after any native call that passes a handle owned by a finalizable
 * object, so the fence is crossed even when the native side throws:
 *
 * <pre>{@code
 * try {
 *     return nGetGlyphData(nativePtr, infoOut, posOut);
 * } finally {
 *     NativeReachability.fence(this);
 * }
 * }</pre>
 */
public final class NativeReachability {

    private NativeReachability() {
    }

    /**
     * Marks {@code target} as still in use at this point, preventing the collector from treating it
     * as unreachable — and therefore finalizable — any earlier.
     *
     * @param target the finalizable owner of a native handle; {@code null} is ignored so callers
     *               need no guard
     */
    @SuppressWarnings("EmptySynchronizedStatement")
    public static void fence(Object target) {
        if (target == null) {
            return;
        }
        synchronized (target) {
            // Intentionally empty. Entering the monitor is a use of `target`, which is the entire
            // point: it forbids the JIT from shortening the reference's lifetime past this call.
        }
    }
}
