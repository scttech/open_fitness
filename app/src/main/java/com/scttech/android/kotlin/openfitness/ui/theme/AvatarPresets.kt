package com.scttech.android.kotlin.openfitness.ui.theme

import androidx.annotation.DrawableRes
import com.scttech.android.kotlin.openfitness.R

data class AvatarPreset(val key: String, @DrawableRes val drawableRes: Int)

/** The bundled set of preset avatar images a profile can pick instead of a custom photo. */
object AvatarPresets {
    val all: List<AvatarPreset> = (1..41).map { n -> AvatarPreset(key = "preset_%02d".format(n), drawableRes = presetResourceFor(n)) }

    fun resourceFor(key: String): Int? = all.firstOrNull { it.key == key }?.drawableRes

    private fun presetResourceFor(n: Int): Int = when (n) {
        1 -> R.drawable.img_avatar_01
        2 -> R.drawable.img_avatar_02
        3 -> R.drawable.img_avatar_03
        4 -> R.drawable.img_avatar_04
        5 -> R.drawable.img_avatar_05
        6 -> R.drawable.img_avatar_06
        7 -> R.drawable.img_avatar_07
        8 -> R.drawable.img_avatar_08
        9 -> R.drawable.img_avatar_09
        10 -> R.drawable.img_avatar_10
        11 -> R.drawable.img_avatar_11
        12 -> R.drawable.img_avatar_12
        13 -> R.drawable.img_avatar_13
        14 -> R.drawable.img_avatar_14
        15 -> R.drawable.img_avatar_15
        16 -> R.drawable.img_avatar_16
        17 -> R.drawable.img_avatar_17
        18 -> R.drawable.img_avatar_18
        19 -> R.drawable.img_avatar_19
        20 -> R.drawable.img_avatar_20
        21 -> R.drawable.img_avatar_21
        22 -> R.drawable.img_avatar_22
        23 -> R.drawable.img_avatar_23
        24 -> R.drawable.img_avatar_24
        25 -> R.drawable.img_avatar_25
        26 -> R.drawable.img_avatar_26
        27 -> R.drawable.img_avatar_27
        28 -> R.drawable.img_avatar_28
        29 -> R.drawable.img_avatar_29
        30 -> R.drawable.img_avatar_30
        31 -> R.drawable.img_avatar_31
        32 -> R.drawable.img_avatar_32
        33 -> R.drawable.img_avatar_33
        34 -> R.drawable.img_avatar_34
        35 -> R.drawable.img_avatar_35
        36 -> R.drawable.img_avatar_36
        37 -> R.drawable.img_avatar_37
        38 -> R.drawable.img_avatar_38
        39 -> R.drawable.img_avatar_39
        40 -> R.drawable.img_avatar_40
        41 -> R.drawable.img_avatar_41
        else -> error("no preset for index $n")
    }
}
