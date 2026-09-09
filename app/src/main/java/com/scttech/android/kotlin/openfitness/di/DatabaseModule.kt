package com.scttech.android.kotlin.openfitness.di

import android.content.Context
import androidx.room.Room
import com.scttech.android.kotlin.openfitness.data.local.OpenFitnessDatabase
import com.scttech.android.kotlin.openfitness.data.local.dao.ProfileDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WeightEntryDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OpenFitnessDatabase =
        Room.databaseBuilder(context, OpenFitnessDatabase::class.java, OpenFitnessDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideProfileDao(database: OpenFitnessDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideWorkoutDao(database: OpenFitnessDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideWorkoutSessionDao(database: OpenFitnessDatabase): WorkoutSessionDao = database.workoutSessionDao()

    @Provides
    fun provideWeightEntryDao(database: OpenFitnessDatabase): WeightEntryDao = database.weightEntryDao()
}
