package com.crystalgraphics.freetype;

import com.crystalgraphics.msdfgen.FreeTypeMSDFIntegration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * What the natives hold on to. Shared font bytes must outlive whichever owner lets go first, and a
 * font file a face is reading must open from any path and stay deletable, or an installed font could
 * not be uninstalled while the game runs.
 */
public class FontDataOwnershipTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void facesOnOneCopyOutliveItsRelease() throws IOException {
        FTFontData data = FTFontData.copyOf(testFont());
        FreeTypeLibrary first = FreeTypeLibrary.create();
        FreeTypeLibrary second = FreeTypeLibrary.create();
        try {
            FTFace a = first.newFaceFromFontData(data, 0);
            FTFace b = second.newFaceFromFontData(data, 0);
            data.release();
            try {
                drawsA(a);
            } finally {
                a.destroy();
            }
            try {
                drawsA(b);
            } finally {
                b.destroy();
            }
        } finally {
            first.destroy();
            second.destroy();
        }
    }

    @Test
    public void msdfOutlinesReadTheSharedCopy() throws IOException {
        FTFontData data = FTFontData.copyOf(testFont());
        FreeTypeMSDFIntegration msdf = FreeTypeMSDFIntegration.create();
        try {
            FreeTypeMSDFIntegration.Font font = msdf.loadFontData(data, 0);
            data.release();
            try {
                outlinesA(font);
            } finally {
                font.destroy();
            }
        } finally {
            msdf.destroy();
        }
    }

    /** A font installed under a user profile named in another script. */
    @Test
    public void aFontFileOpensFromAPathInAnyScript() throws IOException {
        Path file = temp.newFolder("フォント").toPath().resolve("Ünïcode 字体.ttf");
        Files.write(file, testFont());
        FreeTypeLibrary library = FreeTypeLibrary.create();
        FreeTypeMSDFIntegration msdf = FreeTypeMSDFIntegration.create();
        try {
            FTFace face = library.newFace(file.toString(), 0);
            try {
                drawsA(face);
            } finally {
                face.destroy();
            }
            FreeTypeMSDFIntegration.Font font = msdf.loadFont(file.toString(), 0);
            try {
                outlinesA(font);
            } finally {
                font.destroy();
            }
        } finally {
            msdf.destroy();
            library.destroy();
        }
    }

    @Test
    public void aFontFileStaysDeletableWhileOpen() throws IOException {
        Path forFace = temp.getRoot().toPath().resolve("face.ttf");
        Path forOutlines = temp.getRoot().toPath().resolve("outlines.ttf");
        Files.write(forFace, testFont());
        Files.write(forOutlines, testFont());
        FreeTypeLibrary library = FreeTypeLibrary.create();
        FreeTypeMSDFIntegration msdf = FreeTypeMSDFIntegration.create();
        try {
            FTFace face = library.newFace(forFace.toString(), 0);
            try {
                Files.delete(forFace);
                drawsA(face);
            } finally {
                face.destroy();
            }
            FreeTypeMSDFIntegration.Font font = msdf.loadFont(forOutlines.toString(), 0);
            try {
                Files.delete(forOutlines);
                outlinesA(font);
            } finally {
                font.destroy();
            }
        } finally {
            msdf.destroy();
            library.destroy();
        }
    }

    /** Loads and renders a glyph, so its outline is read off the stream now rather than at open. */
    private static void drawsA(FTFace face) {
        face.setPixelSizes(0, 24);
        face.loadGlyph(face.getCharIndex('A'), FTLoadFlags.FT_LOAD_DEFAULT);
        face.renderGlyph(FTRenderMode.FT_RENDER_MODE_NORMAL);
        assertTrue(face.getGlyphBitmap().getWidth() > 0);
    }

    private static void outlinesA(FreeTypeMSDFIntegration.Font font) {
        FreeTypeMSDFIntegration.GlyphData glyph = font.loadGlyph('A');
        try {
            assertTrue(glyph.getAdvance() > 0);
        } finally {
            glyph.getShape().free();
        }
    }

    private byte[] testFont() throws IOException {
        InputStream in = getClass().getResourceAsStream("/test-font.ttf");
        try {
            return FreeTypeLibrary.readAllBytes(in);
        } finally {
            in.close();
        }
    }
}
