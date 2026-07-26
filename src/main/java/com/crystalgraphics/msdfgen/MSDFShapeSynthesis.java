package com.crystalgraphics.msdfgen;

/**
 * Synthetic italic for MSDF-tier glyphs — applied directly to the vector {@link MSDFShape}
 * extracted by {@link FreeTypeMSDFIntegration}, entirely in Java, with no native code of its
 * own. This mirrors the bitmap-tier path ({@code FTFace.outlineShear}, which calls FreeType's
 * {@code FT_Outline_Transform} in native code) but works because msdfgen's {@link MSDFShape}
 * already exposes a full vector contour/segment/control-point API ({@link MSDFContour#getEdge},
 * {@link MSDFSegment#getPoint}/{@code setPoint}) independent of FreeType's own outline
 * representation.
 *
 * <h3>Italic (shear)</h3>
 * <p>{@code x' = x + skewX * y}, applied to every control point of every segment — the exact
 * same transform {@code FTFace.outlineShear} applies via {@code FT_Outline_Transform}, just
 * done point-by-point here instead of via a native 16.16 matrix. A shear is a linear affine
 * map: it preserves every edge relationship exactly, so it's always topologically safe
 * regardless of glyph complexity.</p>
 *
 * <h3>Bold — deliberately NOT here</h3>
 * <p>Synthetic bold for MSDF-tier glyphs is applied as a distance-field threshold bias in
 * {@link com.crystalgraphics.text.msdf.CgMsdfGenerator} instead of a geometry edit — see that
 * class's synthetic-bold handling. An earlier version of this class offset each contour vertex
 * outward along its local normal (mirroring {@code FT_Outline_Embolden}'s point-shift
 * principle), but naive per-vertex "inflate" on Bezier control points routinely produced
 * self-intersecting slivers on tight curves (the counters of o/d/g in particular) — degenerate
 * geometry MSDF generation has no error-correction pass to absorb (see
 * {@code CgMsdfGenerator}'s "Error Correction" javadoc). A distance-field bias sidesteps this
 * entirely: it's a shift on a continuous scalar field, with no topology to break.</p>
 */
public final class MSDFShapeSynthesis {

    private MSDFShapeSynthesis() { }

    public static void shear(MSDFShape shape, double skewX) {
        int contourCount = shape.getContourCount();
        for (int c = 0; c < contourCount; c++) {
            MSDFContour contour = shape.getContour(c);
            int edgeCount = contour.getEdgeCount();
            for (int e = 0; e < edgeCount; e++) {
                MSDFSegment seg = contour.getEdge(e);
                int pointCount = seg.getPointCount();
                for (int p = 0; p < pointCount; p++) {
                    double[] pt = seg.getPoint(p);
                    seg.setPoint(p, pt[0] + skewX * pt[1], pt[1]);
                }
            }
        }
    }
}
