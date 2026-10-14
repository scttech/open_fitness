package com.scttech.android.kotlin.openfitness

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.domain.model.ThemeMode
import com.scttech.android.kotlin.openfitness.domain.model.WeightUnit
import com.scttech.android.kotlin.openfitness.ui.OpenFitnessApp
import com.scttech.android.kotlin.openfitness.ui.common.LocalWeightUnit
import com.scttech.android.kotlin.openfitness.ui.messages.AppAlertsViewModel
import com.scttech.android.kotlin.openfitness.ui.theme.OpenFitnessTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var profileRepository: ProfileRepository

    private val alertsViewModel: AppAlertsViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way - reminders simply won't show if denied */ }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        alertsViewModel.onIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Only on a fresh launch: a recreated activity (rotation) has already handled its intent.
        if (savedInstanceState == null) alertsViewModel.onIntent(intent)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val themeMode by profileRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            val weightUnit by profileRepository.weightUnit.collectAsStateWithLifecycle(initialValue = WeightUnit.KG)
            OpenFitnessTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalWeightUnit provides weightUnit) {
                    OpenFitnessApp()
                }
            }
        }
    }
}
