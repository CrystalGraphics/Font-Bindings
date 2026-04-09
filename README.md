# FreeType + MSDFgen + HarfBuzz Java Bindings

JNI bindings for [FreeType](https://freetype.org/), [MSDFgen](https://github.com/Chlumsky/msdfgen), and [HarfBuzz](https://harfbuzz.github.io/) — designed for LWJGL 2.9.3 and Java 8 compatibility.

This is a unified native bindings library that provides:
- **FreeType**: Font loading, glyph rasterization, bitmap rendering
- **MSDFgen**: Multi-Channel Signed Distance Field generation for GPU text rendering
- **HarfBuzz**: OpenType text shaping with full Unicode support


## Supported Platforms

| Platform | Architecture            | FreeType+HarfBuzz | MSDFgen |
|----------|-------------------------|-------------------|---------|
| Windows  | x86_64                  | ✅                 | ✅       |
| Windows  | x86                     | ✅                 | ✅       |
| Linux    | x86_64                  | ✅                 | ✅       |
| macOS    | x86_64                  | ✅                 | ✅       |
| macOS    | aarch64 (Apple Silicon) | ✅                 | ✅       |

## Quick Start — FreeType + HarfBuzz

```java
FreeTypeLibrary ft = FreeTypeLibrary.create();
FTFace face = ft.newFace("/path/to/font.ttf", 0);
face.setPixelSizes(0, 24);

// Create HarfBuzz font from FreeType face
HBFont hbFont = FreeTypeHarfBuzzIntegration.createHBFontFromFTFace(face);

// Shape text
HBBuffer buffer = HBBuffer.create();
buffer.addUTF8("Hello, World!");
buffer.guessSegmentProperties();
HBShape.shape(hbFont, buffer);

// Get results
HBGlyphInfo[] infos = buffer.getGlyphInfos();
HBGlyphPosition[] positions = buffer.getGlyphPositions();

// Render each glyph
for (int i = 0; i < infos.length; i++) {
    face.loadGlyph(infos[i].getCodepoint(), FTLoadFlags.FT_LOAD_RENDER);
    FTBitmap bitmap = face.getGlyphBitmap();
    // ... upload bitmap to GL texture at positioned offset ...
}

// Cleanup (order matters: HB before FT)
buffer.destroy();
hbFont.destroy();
face.destroy();
ft.destroy();
```

## Quick Start — MSDFgen

```java
import com.crystalgraphics.msdfgen.*;

// Create a simple square shape
MSDFShape shape = MSDFShape.create();
MSDFContour contour = shape.addContour();

MSDFSegment top = MSDFSegment.createLinear();
top.setPoint(0, 0, 1);
top.setPoint(1, 1, 1);
contour.addEdge(top);

// ... add more segments ...

// Prepare shape
shape.normalize();
shape.edgeColoringSimple(3.0);

// Generate MSDF
MSDFBitmap bitmap = MSDFBitmap.allocMsdf(32, 32);
MSDFTransform transform = MSDFTransform.autoFrame(shape, 32, 32, 4.0);
MSDFGenerator.generateMsdf(bitmap, shape, transform);

// Get pixel data
float[] pixels = bitmap.getPixelData(); // 32 * 32 * 3 floats

// Clean up native memory
bitmap.free();
shape.free();
```

## Building Natives

The JNI bindings for **FreeType**, **HarfBuzz** and **MSDFgen** are compiled into a single native
shared library (`freetype_msdfgen_harfbuzz_jni`) per platform using **Zig as a cross-compiler**.

The build system is **library-agnostic**: all library definitions are in `gradle.properties`. The
Gradle script reads the library list, compiles each one, and links all objects into a single shared
library.

```
native-build.gradle.kts    — Generic Zig-based cross-platform build (no hardcoded library properties)
gradle.properties          — All library definitions, versions, targets, compiler flags
include/jni/               — Platform-independent JNI headers for cross-compilation
native/                    — Native source trees (local sources, overrides, git clones)
```

All tasks are in the `native build` group:

| Task                 | Description                                                     |
|----------------------|-----------------------------------------------------------------|
| `downloadZig`        | Download Zig compiler (auto-skips if on PATH)                   |
| `downloadNativeDeps` | Download all libraries with `source=download:`                  |
| `buildNatives`       | Cross-compile for all targets (depends on `downloadNativeDeps`) |
| `buildNativesLocal`  | Prints command to build for current platform only               |
| `cleanNatives`       | Remove all native build artifacts and output binaries           |


## API Reference

### FreeType + HarfBuzz
- [FreeType API](docs/FREETYPE_API.md)
- [HarfBuzz API](docs/HARFBUZZ_API.md)
- [Text Rendering Guide](docs/TEXT_RENDERING_GUIDE.md)

### MSDFgen
- [MSDFgen Integration Guide](docs/MSDFGEN_INTEGRATION_GUIDE.md)
- [MSDFgen FreeType Integration](docs/MSDFGEN_FREETYPE_INTEGRATION.md)

### MSDFgen API Design
- `MSDFShape` / `MSDFContour` / `MSDFSegment` - vector shape construction (mirrors msdfgen C++ API)
- `MSDFBitmap` - pixel data container (SDF/PSDF/MSDF/MTSDF types)
- `MSDFGenerator` - SDF generation entry points
- `MSDFTransform` - projection + distance range configuration
- All native objects must be explicitly `free()`'d

## Dependencies

- **FreeType** 2.13.2 (statically linked)
- **HarfBuzz** 8.3.0 (statically linked)
- **MSDFgen** v1.13 (statically linked)
- **Java** 8+
- No LWJGL 3.x dependency

## Version Compatibility

| This Library | FreeType | HarfBuzz | MSDFgen | Java | LWJGL 2.x |
|--------------|----------|----------|---------|------|-----------|
| 1.0.0        | 2.13.2   | 8.3.0    | v1.13   | 8+   | 2.9.3+    |

## License

MIT License. FreeType is FTL/GPL2. HarfBuzz is MIT. MSDFgen is MIT.
