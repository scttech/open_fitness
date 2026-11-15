package com.scttech.android.kotlin.openfitness.ui.common.animation

/** The equipment in the animation file; [meshNames] are the GLB's mesh names, one entry per piece (a dumbbell pair is two). */
enum class AnimationProp(val meshNames: List<String>) {
    KETTLEBELL(listOf("Kettlebell")),
    MACE(listOf("Mace")),
    PULLUP_BAR(listOf("PullupBar")),
    BARBELL(listOf("Barbell")),
    DUMBBELLS(listOf("Dumbbell.L", "Dumbbell.R")),
    BENCH(listOf("Bench")),
}

private val DUMBBELL_CLIPS = setOf("arnoldpress", "hammercurl", "frontraise", "lateralraise", "reardeltfly")
private val BARBELL_CLIPS = setOf(
    "backsquat", "frontsquat", "deadlift", "sumodeadlift", "romaniandeadlift", "bentoverrow", "pendlayrow",
    "goodmorning", "overheadpress", "uprightrow", "skullcrusher",
)
private val BENCH_CLIPS = setOf("dumbbellfly", "skullcrusher")

/**
 * The GLB holds every prop at once and animates each from its own bone, so a clip shows only the
 * props it uses by hiding the rest. Clip names carry the equipment, which keeps this mostly free of a
 * second table to maintain: add a clip named `MaceFoo` and the mace appears. Barbell and dumbbell clips
 * that do not say so in their name are listed above.
 */
fun propsFor(animationName: String): Set<AnimationProp> = buildSet {
    val name = animationName.lowercase()
    if ("kettlebell" in name || "kbswing" in name || "goblet" in name) add(AnimationProp.KETTLEBELL)
    if ("mace" in name) add(AnimationProp.MACE)
    if ("pullup" in name || "chinup" in name || "hangingleg" in name) add(AnimationProp.PULLUP_BAR)
    if ("barbell" in name || "closegrip" in name || name in BARBELL_CLIPS) add(AnimationProp.BARBELL)
    if ("dumbbell" in name || name in DUMBBELL_CLIPS) add(AnimationProp.DUMBBELLS)
    if ("benchpress" in name || name in BENCH_CLIPS) add(AnimationProp.BENCH)
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
