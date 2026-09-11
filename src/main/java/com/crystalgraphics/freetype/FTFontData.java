package com.crystalgraphics.freetype;

import com.crystalgraphics.NativeLoader;

/**
 * A font's bytes copied into native memory once, for any number of FreeType faces and msdfgen fonts
 * to open without copying them again: every size of a font, every worker thread and msdfgen's
 * outlines can share one copy.
 *
 * <pre>{@code
 * FTFontData data = FTFontData.copyOf(fontBytes);
 * FTFace small = library.newFaceFromFontData(data, 0);
 * FTFace large = workerLibrary.newFaceFromFontData(data, 0);        // no second copy
 * FreeTypeMSDFIntegration.Font outlines = msdf.loadFontData(data, 0);
 * data.release();          // each face and font keeps the bytes until it is destroyed
 * }</pre>
 *
 * <ul>
 *   <li>Every face and font holds a reference of its own, so the owners may let go in any order —
 *       finalizers included — and the memory goes with the last.</li>
 *   <li>A binding opening something on it holds this object's monitor across the native call; that
 *       is what stops {@link #release()} freeing the bytes before the call has taken its reference.</li>
 * </ul>
 */
public final class FTFontData {

    private long nativePtr;
    private final int size;

    private FTFontData(long nativePtr, int size) {
        this.nativePtr = nativePtr;
        this.size = size;
    }

    /**
     * Copies {@code data} into native memory.
     *
     * @throws IllegalArgumentException if {@code data} is null or empty
     * @throws OutOfMemoryError if the native allocation fails
     */
    public static FTFontData copyOf(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data must not be null or empty");
        }
        NativeLoader.ensureLoaded();
        long ptr = nCreate(data, data.length);
        if (ptr == 0) {
            throw new OutOfMemoryError("Cannot allocate " + data.length + " bytes of native font data");
        }
        return new FTFontData(ptr, data.length);
    }

    /** The number of bytes. */
    public int size() {
        return size;
    }

    /**
     * The native handle, for a binding to open a face on. Hold this object's monitor from this call
     * until the native call that takes the handle returns.
     *
     * @throws IllegalStateException if released
     */
    public synchronized long getNativePtr() {
        if (nativePtr == 0) {
            throw new IllegalStateException("FTFontData has been released");
        }
        return nativePtr;
    }

    /** Lets go of this object's reference; faces and fonts opened on it keep theirs. Idempotent. */
    public synchronized void release() {
        if (nativePtr != 0) {
            nRelease(nativePtr);
            nativePtr = 0;
        }
    }

    public synchronized boolean isReleased() {
        return nativePtr == 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            super.finalize();
        }
    }

    private static native long nCreate(byte[] data, int length);
    private static native void nRelease(long handle);
}
