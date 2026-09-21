package com.scttech.android.kotlin.openfitness.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/** Keeps the device screen on for as long as the caller stays in composition - e.g. during a workout timer, so a long rest period doesn't let the screen lock mid-session. */
@Composable
fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
