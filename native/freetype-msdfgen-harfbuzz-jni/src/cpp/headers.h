#ifndef FREETYPE_HARFBUZZ_JNI_HEADERS_H
#define FREETYPE_HARFBUZZ_JNI_HEADERS_H

#include <jni.h>
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_GLYPH_H
#include FT_ADVANCES_H
#include FT_OUTLINE_H

#include <hb.h>
#include <hb-ft.h>

#ifdef __cplusplus
extern "C" {
#endif

void throwException(JNIEnv *env, const char *className, const char *message);
void throwFreeTypeException(JNIEnv *env, int errorCode, const char *message);

#ifdef __cplusplus
}

#include <string>

// Font bytes in native memory that FreeType faces and msdfgen fonts open without copying. Every holder
// takes a reference and the bytes go with the last, so the Java owners may let go in any order.
struct SharedFontData {
    FT_Byte *bytes;
    FT_Long size;
    int refs;
};

SharedFontData *retainSharedFontData(jlong handle);
void releaseSharedFontData(SharedFontData *data);

// A Java string as UTF-8 proper, which FreeType's file stream takes; JNI's modified UTF-8 differs for
// supplementary characters.
std::string utf8Of(JNIEnv *env, jstring value);
#endif

#endif
