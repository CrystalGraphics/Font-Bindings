package com.crystalgraphics.harfbuzz;

import com.crystalgraphics.NativeLoader;
import com.crystalgraphics.NativeReachability;
/**
 * HarfBuzz text buffer. Holds a sequence of Unicode codepoints to be shaped,
 * and after shaping, the resulting glyph info and positions.
 * Must be explicitly destroyed via {@link #destroy()}.
 */
public final class HBBuffer {

    private long nativePtr;

    private HBBuffer(long nativePtr) {
        this.nativePtr = nativePtr;
    }

    public static HBBuffer create() {
        NativeLoader.ensureLoaded();
        long ptr = nCreate();
        if (ptr == 0) {
            throw new RuntimeException("Failed to create HarfBuzz buffer");
        }
        return new HBBuffer(ptr);
    }

    public void addUTF8(String text) {
        checkNotDestroyed();
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        byte[] utf8 = toUTF8(text);
        try {
            nAddUTF8(nativePtr, utf8, utf8.length, 0, utf8.length);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void addUTF8(String text, int itemOffset, int itemLength) {
        checkNotDestroyed();
        byte[] utf8 = toUTF8(text);
        try {
            nAddUTF8(nativePtr, utf8, utf8.length, itemOffset, itemLength);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void addCodepoints(int[] codepoints) {
        checkNotDestroyed();
        try {
            nAddCodepoints(nativePtr, codepoints, codepoints.length, 0, codepoints.length);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void setDirection(int direction) {
        checkNotDestroyed();
        try {
            nSetDirection(nativePtr, direction);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public int getDirection() {
        checkNotDestroyed();
        try {
            return nGetDirection(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void setScript(int script) {
        checkNotDestroyed();
        try {
            nSetScript(nativePtr, script);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public int getScript() {
        checkNotDestroyed();
        try {
            return nGetScript(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void setLanguage(String language) {
        checkNotDestroyed();
        try {
            nSetLanguage(nativePtr, language);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void guessSegmentProperties() {
        checkNotDestroyed();
        try {
            nGuessSegmentProperties(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public int getLength() {
        checkNotDestroyed();
        try {
            return nGetLength(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    /**
     * Returns glyph info after shaping. Each element is:
     * [codepoint (glyph ID after shaping), cluster].
     */
    public HBGlyphInfo[] getGlyphInfos() {
        checkNotDestroyed();
        try {
            return nGetGlyphInfos(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    /**
     * Returns glyph positions after shaping. Each element is:
     * [xAdvance, yAdvance, xOffset, yOffset] in font units.
     */
    public HBGlyphPosition[] getGlyphPositions() {
        checkNotDestroyed();
        try {
            return nGetGlyphPositions(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    /** Ints written per glyph into {@code infoOut} by {@link #getGlyphData}: codepoint, cluster, flags. */
    public static final int INFO_STRIDE = 3;
    /** Ints written per glyph into {@code posOut} by {@link #getGlyphData}: xAdv, yAdv, xOff, yOff. */
    public static final int POSITION_STRIDE = 4;

    /**
     * Allocation-free readback: fills caller-owned arrays instead of returning
     * {@link HBGlyphInfo}/{@link HBGlyphPosition} object arrays.
     *
     * <p>Prefer this in any hot path. {@link #getGlyphInfos()} and {@link #getGlyphPositions()}
     * allocate one Java object per glyph, and their native side performs a {@code FindClass} plus
     * {@code GetMethodID} on every call — a classloader lookup by string name, twice per shaped
     * run. That cost does not scale with glyph count, so it dominates short strings: measured
     * across a thousand UI labels, readback was 63% of all HarfBuzz time while the actual shaping
     * call was 23%.
     *
     * <p>Reusing the same two arrays across calls means a steady stream of shaping allocates
     * nothing at all.
     *
     * @param infoOut receives {@link #INFO_STRIDE} ints per glyph; may be {@code null} to query
     *                the count only
     * @param posOut  receives {@link #POSITION_STRIDE} ints per glyph; may be {@code null} to
     *                query the count only
     * @return the glyph count on success. If either array is too small <strong>nothing is
     *         written</strong> and {@code -count} is returned, so the caller can grow to
     *         {@code count * STRIDE} and call again.
     */
    public int getGlyphData(int[] infoOut, int[] posOut) {
        checkNotDestroyed();
        try {
            return nGetGlyphData(nativePtr, infoOut, posOut);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void reset() {
        checkNotDestroyed();
        try {
            nReset(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public void clearContents() {
        checkNotDestroyed();
        try {
            nClearContents(nativePtr);
        } finally {
            NativeReachability.fence(this);
        }
    }

    public long getNativePtr() {
        return nativePtr;
    }

    public void destroy() {
        if (nativePtr != 0) {
            nDestroy(nativePtr);
            nativePtr = 0;
        }
    }

    public boolean isDestroyed() {
        return nativePtr == 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        try {
            if (nativePtr != 0) {
                System.err.println("[CrystalGraphics] WARNING: HBBuffer was not destroyed! "
                    + "Call destroy() explicitly to avoid native memory leaks.");
                nDestroy(nativePtr);
                nativePtr = 0;
            }
        } finally {
            super.finalize();
        }
    }

    private void checkNotDestroyed() {
        if (nativePtr == 0) {
            throw new IllegalStateException("HBBuffer has been destroyed");
        }
    }

    private static byte[] toUTF8(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported", e);
        }
    }

    private static native int nGetGlyphData(long bufferPtr, int[] infoOut, int[] posOut);
    private static native long nCreate();
    private static native void nDestroy(long bufferPtr);
    private static native void nAddUTF8(long bufferPtr, byte[] utf8, int textLen, int itemOffset, int itemLength);
    private static native void nAddCodepoints(long bufferPtr, int[] codepoints, int len, int itemOffset, int itemLength);
    private static native void nSetDirection(long bufferPtr, int direction);
    private static native int nGetDirection(long bufferPtr);
    private static native void nSetScript(long bufferPtr, int script);
    private static native int nGetScript(long bufferPtr);
    private static native void nSetLanguage(long bufferPtr, String language);
    private static native void nGuessSegmentProperties(long bufferPtr);
    private static native int nGetLength(long bufferPtr);
    private static native HBGlyphInfo[] nGetGlyphInfos(long bufferPtr);
    private static native HBGlyphPosition[] nGetGlyphPositions(long bufferPtr);
    private static native void nReset(long bufferPtr);
    private static native void nClearContents(long bufferPtr);
}
