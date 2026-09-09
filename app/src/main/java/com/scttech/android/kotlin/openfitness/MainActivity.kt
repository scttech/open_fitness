package com.scttech.android.kotlin.openfitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.scttech.android.kotlin.openfitness.ui.OpenFitnessApp
import com.scttech.android.kotlin.openfitness.ui.theme.OpenFitnessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenFitnessTheme {
                OpenFitnessApp()
            }
        }
    }
}
