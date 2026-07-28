package com.crystalgraphics.msdfgen;

import com.crystalgraphics.NativeReachability;

public final class MSDFGenerator {

    private MSDFGenerator() {}

    public static void generateSdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform) {
        try {
            MSDFResult.check(MSDFNative.nGenerateSdf(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper()
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generatePsdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform) {
        try {
            MSDFResult.check(MSDFNative.nGeneratePsdf(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper()
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generateMsdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform) {
        try {
            MSDFResult.check(MSDFNative.nGenerateMsdf(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper()
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generateMtsdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform) {
        try {
            MSDFResult.check(MSDFNative.nGenerateMtsdf(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper()
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generateSdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform,
                                   boolean overlapSupport) {
        try {
            MSDFResult.check(MSDFNative.nGenerateSdfWithConfig(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                overlapSupport
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generateMsdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform,
                                    boolean overlapSupport,
                                    int errorCorrectionMode,
                                    int distanceCheckMode,
                                    double minDeviationRatio,
                                    double minImproveRatio) {
        try {
            MSDFResult.check(MSDFNative.nGenerateMsdfWithConfig(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                overlapSupport,
                errorCorrectionMode, distanceCheckMode,
                minDeviationRatio, minImproveRatio
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generateMtsdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform,
                                     boolean overlapSupport,
                                     int errorCorrectionMode,
                                     int distanceCheckMode,
                                     double minDeviationRatio,
                                     double minImproveRatio) {
        try {
            MSDFResult.check(MSDFNative.nGenerateMtsdfWithConfig(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                overlapSupport,
                errorCorrectionMode, distanceCheckMode,
                minDeviationRatio, minImproveRatio
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void generatePsdf(MSDFBitmap output, MSDFShape shape, MSDFTransform transform,
                                    boolean overlapSupport) {
        try {
            MSDFResult.check(MSDFNative.nGeneratePsdfWithConfig(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                overlapSupport
            ));
        } finally {
            NativeReachability.fence(output);
            NativeReachability.fence(shape);
        }
    }

    public static void errorCorrection(MSDFBitmap bitmap, MSDFShape shape, MSDFTransform transform,
                                       int errorCorrectionMode, int distanceCheckMode,
                                       double minDeviationRatio, double minImproveRatio) {
        try {
            MSDFResult.check(MSDFNative.nErrorCorrection(
                bitmap.getNativeHandle(), bitmap.getType(), bitmap.getWidth(), bitmap.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                errorCorrectionMode, distanceCheckMode,
                minDeviationRatio, minImproveRatio
            ));
        } finally {
            NativeReachability.fence(bitmap);
            NativeReachability.fence(shape);
        }
    }

    public static void errorCorrection(MSDFBitmap bitmap, MSDFShape shape, MSDFTransform transform) {
        errorCorrection(bitmap, shape, transform,
            MSDFConstants.ERROR_CORRECTION_EDGE_PRIORITY,
            MSDFConstants.DISTANCE_CHECK_AT_EDGE,
            MSDFConstants.DEFAULT_MIN_DEVIATION_RATIO,
            MSDFConstants.DEFAULT_MIN_IMPROVE_RATIO);
    }

    public static void errorCorrectionFastDistance(MSDFBitmap bitmap, MSDFTransform transform,
                                                   double minDeviationRatio) {
        try {
            MSDFResult.check(MSDFNative.nErrorCorrectionFastDistance(
                bitmap.getNativeHandle(), bitmap.getType(), bitmap.getWidth(), bitmap.getHeight(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                minDeviationRatio
            ));
        } finally {
            NativeReachability.fence(bitmap);
        }
    }

    public static void errorCorrectionFastDistance(MSDFBitmap bitmap, MSDFTransform transform) {
        errorCorrectionFastDistance(bitmap, transform, MSDFConstants.DEFAULT_MIN_DEVIATION_RATIO);
    }

    public static void errorCorrectionFastEdge(MSDFBitmap bitmap, MSDFTransform transform,
                                               double minDeviationRatio) {
        try {
            MSDFResult.check(MSDFNative.nErrorCorrectionFastEdge(
                bitmap.getNativeHandle(), bitmap.getType(), bitmap.getWidth(), bitmap.getHeight(),
                transform.getRangeLower(), transform.getRangeUpper(),
                minDeviationRatio
            ));
        } finally {
            NativeReachability.fence(bitmap);
        }
    }

    public static void errorCorrectionFastEdge(MSDFBitmap bitmap, MSDFTransform transform) {
        errorCorrectionFastEdge(bitmap, transform, MSDFConstants.DEFAULT_MIN_DEVIATION_RATIO);
    }

    public static void distanceSignCorrection(MSDFBitmap bitmap, MSDFShape shape, MSDFTransform transform, int fillRule) {
        try {
            MSDFResult.check(MSDFNative.nDistanceSignCorrection(
                bitmap.getNativeHandle(), bitmap.getType(), bitmap.getWidth(), bitmap.getHeight(),
                shape.getNativeHandle(),
                transform.getScaleX(), transform.getScaleY(),
                transform.getTranslateX(), transform.getTranslateY(),
                transform.getRangeLower(), transform.getRangeUpper(),
                fillRule
            ));
        } finally {
            NativeReachability.fence(bitmap);
            NativeReachability.fence(shape);
        }
    }

    public static void renderSdf(MSDFBitmap output, MSDFBitmap sdf, MSDFTransform transform, float sdThreshold) {
        try {
            MSDFResult.check(MSDFNative.nRenderSdf(
                output.getNativeHandle(), output.getType(), output.getWidth(), output.getHeight(),
                sdf.getNativeHandle(), sdf.getType(), sdf.getWidth(), sdf.getHeight(),
                transform.getRangeLower(), transform.getRangeUpper(),
                sdThreshold
            ));
        } finally {
            NativeReachability.fence(output);
        }
    }

    public static void renderSdf(MSDFBitmap output, MSDFBitmap sdf, MSDFTransform transform) {
        renderSdf(output, sdf, transform, 0.5f);
    }
}
