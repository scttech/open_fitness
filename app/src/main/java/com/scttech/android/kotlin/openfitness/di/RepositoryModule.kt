package com.scttech.android.kotlin.openfitness.di

import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepository
import com.scttech.android.kotlin.openfitness.data.repository.ProfileRepositoryImpl
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepository
import com.scttech.android.kotlin.openfitness.data.repository.SessionRepositoryImpl
import com.scttech.android.kotlin.openfitness.data.repository.WeightRepository
import com.scttech.android.kotlin.openfitness.data.repository.WeightRepositoryImpl
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepository
import com.scttech.android.kotlin.openfitness.data.repository.WorkoutRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    abstract fun bindWeightRepository(impl: WeightRepositoryImpl): WeightRepository
}
