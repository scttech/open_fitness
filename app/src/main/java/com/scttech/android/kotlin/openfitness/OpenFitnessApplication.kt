package com.scttech.android.kotlin.openfitness

import android.app.Application
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class OpenFitnessApplication : Application() {

    @Inject lateinit var workoutRepository: WorkoutRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            workoutRepository.seedTemplatesIfNeeded()
        }
    }
}
