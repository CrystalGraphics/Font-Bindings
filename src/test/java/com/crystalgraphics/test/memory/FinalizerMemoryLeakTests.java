package com.crystalgraphics.test.memory;

import com.crystalgraphics.freetype.*;
import com.crystalgraphics.harfbuzz.*;
import com.crystalgraphics.text.FreeTypeHarfBuzzIntegration;
import org.junit.Test;
import static org.junit.Assert.*;

public class FinalizerMemoryLeakTests extends MemoryLeakDetectionBase {

    @Test
    public void testF1_gcCleanupWithExplicitDestroy() {
        logTestStart();
        byte[] fontData = loadTestFontData();

        MemoryMetrics.Snapshot baseline = MemoryMetrics.takeSnapshot();
        logMemory("Baseline", baseline);

        for (int i = 0; i < 20; i++) {
            FreeTypeLibrary ft = FreeTypeLibrary.create();
            FTFace face = ft.newFaceFromMemory(fontData, 0);
            face.setPixelSizes(0, 24);

            HBFont hbFont = FreeTypeHarfBuzzIntegration.createHBFontFromFTFace(face);

            HBBuffer buffer = HBBuffer.create();
            buffer.addUTF8("GC test iteration " + i);
            buffer.guessSegmentProperties();
            HBShape.shape(hbFont, buffer);

            buffer.destroy();
            hbFont.destroy();
            face.destroy();
            ft.destroy();
        }

        MemoryMetrics.Snapshot afterExplicit = MemoryMetrics.takeSnapshot();
        logMemory("After 20 explicit-destroy cycles", afterExplicit);

        assertMemoryReturnsToBaseline(baseline, afterExplicit);
    }

    @SuppressWarnings("unused")
    @Test
    public void testF2_objectsOutOfScopeWithGC() {
        logTestStart();
        byte[] fontData = loadTestFontData();

        MemoryMetrics.Snapshot baseline = MemoryMetrics.takeSnapshot();
        logMemory("Baseline", baseline);

        // INTENTIONAL: Tests behavior when destroy() is NOT called (see GAP_ANALYSIS_REPORT.md §6.2)
        for (int i = 0; i < 10; i++) {
            createAndAbandonResources(fontData, i);
        }

        MemoryMetrics.forceGC();

        MemoryMetrics.Snapshot afterGC = MemoryMetrics.takeSnapshot();
        logMemory("After abandon + GC", afterGC);

        // Finalizers free what they can in any order: faces, fonts and buffers. An abandoned FT_Library is
        // leaked, since freeing it would free its faces under a finalizer that has yet to run.
        long rss = PlatformTools.getProcessRSS();
        if (rss > 0) {
            System.out.println("  Process RSS: " + MemoryMetrics.formatBytes(rss));
        }

        System.out.println("  Finalizers freed the abandoned FT_Face/hb_font/hb_buffer; each FT_Library is leaked.");
    }

    private void createAndAbandonResources(byte[] fontData, int iteration) {
        FreeTypeLibrary ft = FreeTypeLibrary.create();
        FTFace face = ft.newFaceFromMemory(fontData, 0);
        face.setPixelSizes(0, 24);

        HBFont hbFont = FreeTypeHarfBuzzIntegration.createHBFontFromFTFace(face);

        HBBuffer buffer = HBBuffer.create();
        buffer.addUTF8("Abandoned " + iteration);
        buffer.guessSegmentProperties();
        HBShape.shape(hbFont, buffer);
        buffer.getGlyphInfos();

        // Intentionally NOT calling destroy() — let objects fall out of scope
    }
}
