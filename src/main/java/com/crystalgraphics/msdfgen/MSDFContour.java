package com.crystalgraphics.msdfgen;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code freed} is an {@link AtomicBoolean} so {@link #free()} calls the
 * native free exactly once even under a race between an explicit caller and
 * the finalizer — see {@link MSDFShape}'s javadoc for the full rationale.
 */
public final class MSDFContour {

    private long nativeHandle;
    private final boolean owned;
    private final AtomicBoolean freed = new AtomicBoolean(false);

    MSDFContour(long nativeHandle, boolean owned) {
        this.nativeHandle = nativeHandle;
        this.owned = owned;
    }

    public static MSDFContour create() {
        long handle = MSDFNative.nContourAlloc();
        if (handle == 0) {
            throw new MSDFException("Failed to allocate contour");
        }
        return new MSDFContour(handle, true);
    }

    public void addEdge(MSDFSegment segment) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nContourAddEdge(nativeHandle, segment.getNativeHandle()));
    }

    public void removeEdge(MSDFSegment segment) {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nContourRemoveEdge(nativeHandle, segment.getNativeHandle()));
    }

    public int getEdgeCount() {
        checkNotFreed();
        return MSDFNative.nContourGetEdgeCount(nativeHandle);
    }

    public MSDFSegment getEdge(int index) {
        checkNotFreed();
        long segHandle = MSDFNative.nContourGetEdge(nativeHandle, index);
        if (segHandle == 0) {
            throw new MSDFException("Failed to get edge at index " + index);
        }
        return new MSDFSegment(segHandle, false);
    }

    public int getWinding() {
        checkNotFreed();
        return MSDFNative.nContourGetWinding(nativeHandle);
    }

    public void reverse() {
        checkNotFreed();
        MSDFResult.check(MSDFNative.nContourReverse(nativeHandle));
    }

    public double[] getBounds() {
        checkNotFreed();
        double[] bounds = new double[4];
        MSDFResult.check(MSDFNative.nContourBound(nativeHandle, bounds));
        return bounds;
    }

    public double[] getBoundsMiters(double[] boundsInOut, double border, double miterLimit, int polarity) {
        checkNotFreed();
        double[] bounds = new double[4];
        System.arraycopy(boundsInOut, 0, bounds, 0, 4);
        MSDFResult.check(MSDFNative.nContourBoundMiters(nativeHandle, bounds, border, miterLimit, polarity));
        return bounds;
    }

    public void free() {
        if (owned && freed.compareAndSet(false, true)) {
            MSDFNative.nContourFree(nativeHandle);
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
            throw new IllegalStateException("Contour has been freed");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        free();
        super.finalize();
    }
}
