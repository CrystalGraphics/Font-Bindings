package com.crystalgraphics.msdfgen;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an MSDFgen Shape - a 2D vector shape composed of contours.
 * Manages native memory - must be {@link #free()}'d when no longer needed.
 *
 * <h3>Thread safety of {@link #free()}</h3>
 * <p>{@code freed} is an {@link AtomicBoolean}, and {@link #free()} only calls
 * the native free once, via {@code compareAndSet}. This makes {@link #free()}
 * safe to call explicitly from owning code <em>and</em> safe as a finalizer
 * backstop for anyone who forgets to — whichever runs first wins the race,
 * and the other becomes a no-op. (Previously {@code freed} was a plain
 * {@code boolean}: a caller explicitly freeing a shape while the finalizer
 * thread concurrently ran its own free-if-not-freed check could both observe
 * {@code freed == false} and both call the native free, corrupting the
 * native heap. That race is why some call sites used to avoid calling
 * {@link #free()} at all and relied solely on finalization — with this fix,
 * explicit freeing is safe again and should be preferred, since it doesn't
 * depend on GC/finalizer timing to bound native memory use.)</p>
 */
public final class MSDFShape {

    private long nativeHandle;
    private final AtomicBoolean freed = new AtomicBoolean(false);

    MSDFShape(long nativeHandle) {
        this.nativeHandle = nativeHandle;
    }

    public static MSDFShape create() {
        long handle = MSDFNative.nShapeAlloc();
        if (handle == 0) {
            throw new MSDFException("Failed to allocate shape");
        }
        return new MSDFShape(handle);
    }

    public MSDFContour addContour() {
        checkNotFreed();
        long contourHandle = MSDFNative.nShapeAddContour(nativeHandle);
        if (contourHandle == 0) {
            throw new MSDFException("Failed to add contour to shape");
        }
        return new MSDFContour(contourHandle, false);
    }

    public int getContourCount() {
        checkNotFreed();
        return MSDFNative.nShapeGetContourCount(nativeHandle);
    }

    public MSDFContour getContour(int index) {
        checkNotFreed();
        long contourHandle = MSDFNative.nShapeGetContour(nativeHandle, index);
        if (contourHandle == 0) {
            throw new MSDFException("Failed to get contour at index " + index);
        }
        return new MSDFContour(contourHandle, false);
    }

    public void removeContour(MSDFContour contour) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeRemoveContour(nativeHandle, contour.getNativeHandle()));
    }

    public int getEdgeCount() {
        checkNotFreed();
        return MSDFNative.nShapeGetEdgeCount(nativeHandle);
    }

    public void normalize() {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeNormalize(nativeHandle));
    }

    public boolean validate() {
        checkNotFreed();
        return MSDFNative.nShapeValidate(nativeHandle) != 0;
    }

    public void orientContours() {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeOrientContours(nativeHandle));
    }

    public void edgeColoringSimple(double angleThreshold) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeEdgeColorsSimple(nativeHandle, angleThreshold));
    }

    public void edgeColoringInkTrap(double angleThreshold) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeEdgeColorsInkTrap(nativeHandle, angleThreshold));
    }

    public void edgeColoringByDistance(double angleThreshold) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeEdgeColorsByDistance(nativeHandle, angleThreshold));
    }

    public int getYAxisOrientation() {
        checkNotFreed();
        return MSDFNative.nShapeGetYAxisOrientation(nativeHandle);
    }

    public void setYAxisOrientation(int orientation) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nShapeSetYAxisOrientation(nativeHandle, orientation));
    }

    /**
     * Returns shape bounds as {left, bottom, right, top}.
     */
    public double[] getBounds() {
        checkNotFreed();
        double[] bounds = new double[4];
        MSDFResult.check(MSDFNative.nShapeBound(nativeHandle, bounds));
        return bounds;
    }

    public double[] getBoundsMiters(double[] boundsInOut, double border, double miterLimit, int polarity) {
        checkNotFreed();
        double[] bounds = new double[4];
        System.arraycopy(boundsInOut, 0, bounds, 0, 4);
        MSDFResult.check(MSDFNative.nShapeBoundMiters(nativeHandle, bounds, border, miterLimit, polarity));
        return bounds;
    }

    public double getOneShotDistance(double originX, double originY) {
        checkNotFreed();
        return MSDFNative.nShapeOneShotDistance(nativeHandle, originX, originY);
    }

    public void free() {
        if (freed.compareAndSet(false, true)) {
            MSDFNative.nShapeFree(nativeHandle);
            nativeHandle = 0;
        }
    }

    public boolean isFreed() {
        return freed.get();
    }

    long getNativeHandle() {
        checkNotFreed();
        return nativeHandle;
    }

    private void checkNotFreed() {
        if (freed.get()) {
            throw new IllegalStateException("Shape has been freed");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }
}
