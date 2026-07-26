package com.crystalgraphics.harfbuzz;

public final class HBGlyphInfo {
    /** Mirrors {@code HB_GLYPH_FLAG_UNSAFE_TO_BREAK} from {@code hb-buffer.h}. */
    public static final int HB_GLYPH_FLAG_UNSAFE_TO_BREAK = 0x00000001;

    private final int codepoint;
    private final int cluster;
    private final int flags;

    public HBGlyphInfo(int codepoint, int cluster, int flags) {
        this.codepoint = codepoint;
        this.cluster = cluster;
        this.flags = flags;
    }

    public int getCodepoint() { return codepoint; }
    public int getCluster() { return cluster; }

    /** Raw flags from {@code hb_glyph_info_get_glyph_flags}. See {@link #isUnsafeToBreak()}. */
    public int getFlags() { return flags; }

    /**
     * {@code true} if HarfBuzz cannot guarantee that shaping is unaffected by a line break
     * immediately before this glyph — i.e. re-shaping the surrounding text independently
     * could produce different glyphs/advances than slicing the already-shaped result here.
     * {@code false} means it's provably safe to slice existing glyph/advance data at this
     * boundary without re-shaping.
     */
    public boolean isUnsafeToBreak() {
        return (flags & HB_GLYPH_FLAG_UNSAFE_TO_BREAK) != 0;
    }

    @Override
    public String toString() {
        return "HBGlyphInfo{glyph=" + codepoint + ", cluster=" + cluster + ", flags=" + flags + '}';
    }
}
