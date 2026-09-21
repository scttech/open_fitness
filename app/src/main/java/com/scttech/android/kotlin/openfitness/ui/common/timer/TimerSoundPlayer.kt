package com.scttech.android.kotlin.openfitness.ui.common.timer

import android.media.AudioManager
import android.media.ToneGenerator

/** Shared beep player for workout/program timers - a thin, reusable wrapper over [ToneGenerator]. */
class TimerSoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    fun playTick() = tone(ToneGenerator.TONE_PROP_BEEP, durationMs = 150)

    fun playPhaseComplete() = tone(ToneGenerator.TONE_PROP_BEEP2, durationMs = 400)

    private fun tone(type: Int, durationMs: Int) {
        val generator = toneGenerator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 100).also { toneGenerator = it }
        generator.startTone(type, durationMs)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
