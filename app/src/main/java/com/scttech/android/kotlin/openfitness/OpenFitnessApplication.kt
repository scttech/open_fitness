package com.scttech.android.kotlin.openfitness

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.scttech.android.kotlin.openfitness.data.repository.ExerciseRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.di.ApplicationScope
import com.scttech.android.kotlin.openfitness.notification.RetestReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OpenFitnessApplication : Application(), Configuration.Provider {

    @Inject lateinit var workoutRepository: WorkoutRepository

    @Inject lateinit var exerciseRepository: ExerciseRepository

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            workoutRepository.seedTemplatesIfNeeded()
            exerciseRepository.seedExercisesIfNeeded()
        }
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RETEST_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RetestReminderWorker>(1, TimeUnit.DAYS).build(),
        )
    }

    private companion object {
        const val RETEST_REMINDER_WORK_NAME = "retest_reminder"
    }
}
