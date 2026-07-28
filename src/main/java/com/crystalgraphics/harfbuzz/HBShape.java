package com.crystalgraphics.harfbuzz;

import com.crystalgraphics.NativeLoader;
import com.crystalgraphics.NativeReachability;

public final class HBShape {

    private HBShape() {
    }

    public static void shape(HBFont font, HBBuffer buffer) {
        shape(font, buffer, null);
    }

    public static void shape(HBFont font, HBBuffer buffer, String[] features) {
        NativeLoader.ensureLoaded();
        if (font == null || font.isDestroyed()) {
            throw new IllegalArgumentException("font must not be null or destroyed");
        }
        if (buffer == null || buffer.isDestroyed()) {
            throw new IllegalArgumentException("buffer must not be null or destroyed");
        }
        // Both wrappers become unreachable the instant their pointers are read out, and shaping is
        // the longest native call in the module — the widest window in which a GC can run their
        // finalizers and free a font or buffer that hb_shape is still using. See
        // NativeReachability for why this is a real crash and not a theoretical one.
        try {
            nShape(font.getNativePtr(), buffer.getNativePtr(), features);
        } finally {
            NativeReachability.fence(font);
            NativeReachability.fence(buffer);
        }
    }

    private static native void nShape(long fontPtr, long bufferPtr, String[] features);
}
