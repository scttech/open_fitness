package com.scttech.android.kotlin.openfitness.di

import android.content.Context
import androidx.room.Room
import com.scttech.android.kotlin.openfitness.data.local.OpenFitnessDatabase
import com.scttech.android.kotlin.openfitness.data.local.dao.ExerciseDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProfileDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramSessionDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramTestDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WeightEntryDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutSessionDao
import com.scttech.android.kotlin.openfitness.data.local.migration.MIGRATION_1_2
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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideProfileDao(database: OpenFitnessDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideWorkoutDao(database: OpenFitnessDatabase): WorkoutDao = database.workoutDao()

    @Provides
    fun provideWorkoutSessionDao(database: OpenFitnessDatabase): WorkoutSessionDao = database.workoutSessionDao()

    @Provides
    fun provideWeightEntryDao(database: OpenFitnessDatabase): WeightEntryDao = database.weightEntryDao()

    @Provides
    fun provideExerciseDao(database: OpenFitnessDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideProgramDao(database: OpenFitnessDatabase): ProgramDao = database.programDao()

    @Provides
    fun provideProgramTestDao(database: OpenFitnessDatabase): ProgramTestDao = database.programTestDao()

    @Provides
    fun provideProgramSessionDao(database: OpenFitnessDatabase): ProgramSessionDao = database.programSessionDao()
}
