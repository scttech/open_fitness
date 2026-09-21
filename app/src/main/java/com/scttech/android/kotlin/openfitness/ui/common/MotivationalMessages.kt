package com.scttech.android.kotlin.openfitness.ui.common

/** Shown once a session (program or workout) is completed, to send the user off on a high note. */
object MotivationalMessages {
    private val messages = listOf(
        "Great work! Every set brings you closer to your goal.",
        "Session complete — that's how progress is made.",
        "Nice work. Consistency like this is what gets results.",
        "Done and done. Your future self says thanks.",
        "That's a wrap! Recovery starts now.",
        "Solid session. Stack enough of these and anything is possible.",
        "You showed up and got it done. That's the whole game.",
        "Crushed it. Rest up and come back stronger.",
    )

    fun random(): String = messages.random()
}
