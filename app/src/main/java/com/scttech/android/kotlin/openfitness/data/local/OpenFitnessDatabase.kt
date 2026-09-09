package com.scttech.android.kotlin.openfitness.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scttech.android.kotlin.openfitness.data.local.dao.ExerciseDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProfileDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramSessionDao
import com.scttech.android.kotlin.openfitness.data.local.dao.ProgramTestDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WeightEntryDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutSessionDao
import com.scttech.android.kotlin.openfitness.data.local.entity.ExerciseEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.ProfileEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramSessionEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramTestEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.WeightEntryEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.WorkoutEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        ProfileEntity::class,
        WorkoutEntity::class,
        WorkoutSessionEntity::class,
        WeightEntryEntity::class,
        ExerciseEntity::class,
        ProgramEntity::class,
        ProgramTestEntity::class,
        ProgramSessionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class OpenFitnessDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao
    abstract fun programTestDao(): ProgramTestDao
    abstract fun programSessionDao(): ProgramSessionDao

    companion object {
        const val DATABASE_NAME = "openfitness.db"
    }
}
