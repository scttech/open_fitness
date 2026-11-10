package com.scttech.android.kotlin.openfitness.ui.common.animation

/** The equipment meshes bundled in the animation file, named exactly as the GLB names them. */
enum class AnimationProp(val meshName: String) {
    KETTLEBELL("Kettlebell"),
    MACE("Mace"),
    PULLUP_BAR("PullupBar"),
}

/**
 * The GLB holds every prop at once and animates each from its own bone, so a clip shows only the
 * props it uses by hiding the rest. Clip names carry the equipment, which keeps this free of a
 * second table to maintain: add a clip named `MaceFoo` and the mace appears.
 */
fun propsFor(animationName: String): Set<AnimationProp> = buildSet {
    val name = animationName.lowercase()
    if ("kettlebell" in name || "kbswing" in name || "goblet" in name) add(AnimationProp.KETTLEBELL)
    if ("mace" in name) add(AnimationProp.MACE)
    if ("pullup" in name || "chinup" in name || "hangingleg" in name) add(AnimationProp.PULLUP_BAR)
}

/**
 * Seconds to skip at the start of every clip. The exporter writes frame/fps, so frame 1 sits at 1/30 s
 * and nothing happens before it - looping from 0 would stall for a frame at each seam.
 */
internal const val CLIP_LEAD_IN_SECONDS = 1f / 30f

/**
 * Maps a running clock onto a clip so it loops without repeating the seam frame. Each clip's last
 * frame is the same pose as its first (the source checks the loop is seamless), so the loop covers
 * [CLIP_LEAD_IN_SECONDS] up to, but not including, the final frame.
 */
internal fun loopTime(elapsedSeconds: Float, clipDurationSeconds: Float): Float {
    val period = clipDurationSeconds - CLIP_LEAD_IN_SECONDS
    if (period <= 0f) return 0f
    return CLIP_LEAD_IN_SECONDS + (elapsedSeconds % period)
}
