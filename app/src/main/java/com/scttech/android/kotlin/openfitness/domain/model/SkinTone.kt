package com.scttech.android.kotlin.openfitness.domain.model

/**
 * The skin tone of the figure in the exercise animations. [linearFactor] is the glTF `baseColorFactor`
 * for the model's `Skin` material (linear RGB); it multiplies a grey skin texture, so shading and detail
 * are kept. [swatchArgb] is the sRGB colour the skin shows on average, for menus.
 */
enum class SkinTone(
    val label: String,
    val swatchArgb: Long,
    val linearFactor: FloatArray,
) {
    ORIGINAL("Original", 0xFFD69D80, floatArrayOf(0.7169f, 0.3594f, 0.2301f)),
    LIGHT("Light", 0xFFF1C2A7, floatArrayOf(0.9378f, 0.5751f, 0.4120f)),
    FAIR("Fair", 0xFFE0AC8C, floatArrayOf(0.7947f, 0.4398f, 0.2796f)),
    MEDIUM("Medium", 0xFFC68662, floatArrayOf(0.6020f, 0.2542f, 0.1302f)),
    TAN("Tan", 0xFFAC704B, floatArrayOf(0.4398f, 0.1727f, 0.0750f)),
    BROWN("Brown", 0xFF805034, floatArrayOf(0.2301f, 0.0855f, 0.0366f)),
    DARK("Dark", 0xFF563424, floatArrayOf(0.0992f, 0.0366f, 0.0188f)),
}
