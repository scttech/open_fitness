package com.scttech.android.kotlin.openfitness.domain.model

/**
 * The goal or health concern a starter template is aimed at, used to group and filter the
 * template browser. Declaration order is the display order.
 */
enum class TemplateCategory(val displayName: String, val description: String) {
    BONE_HEALTH(
        displayName = "Bone Health & Menopause",
        description = "Weight-bearing, resistance and gentle-impact work to help maintain bone density and muscle " +
            "through menopause and beyond. If you have osteoporosis or a fracture history, check with your doctor " +
            "first and avoid loaded spinal twisting or forward bending.",
    ),
    FAT_LOSS(
        displayName = "Fat Loss",
        description = "Higher-intensity circuits and intervals that burn a lot in a short time. Pair with " +
            "strength work and good nutrition - the workout alone rarely does it all.",
    ),
    MUSCLE_GAIN(
        displayName = "Muscle Gain",
        description = "Higher-volume sessions that target muscle growth. Use a weight you can control for the " +
            "target reps, and add a little over time.",
    ),
    STRENGTH(
        displayName = "Strength",
        description = "Heavy, low-rep progressions on the big lifts. Warm up first and stop a rep or two shy of failing.",
    ),
    CONDITIONING(
        displayName = "Cardio & Conditioning",
        description = "Build heart and lung fitness, from gentle walk-jog intervals to all-out sprints.",
    ),
    MOBILITY(
        displayName = "Mobility & Recovery",
        description = "Stretching and movement prep for warm-ups, cool-downs and rest days.",
    ),
    BACK_POSTURE(
        displayName = "Back, Posture & Core",
        description = "Counter the effects of sitting and build a stable trunk. Not a substitute for care if you " +
            "have ongoing back pain.",
    ),
    BALANCE_AGING(
        displayName = "Balance & Healthy Aging",
        description = "Balance, leg strength and low-impact conditioning to stay independent and reduce fall risk.",
    ),
    GENERAL(
        displayName = "General Fitness",
        description = "Well-rounded full-body routines, including ones built around a single piece of equipment.",
    ),
}
