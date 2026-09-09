package com.scttech.android.kotlin.openfitness.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.scttech.android.kotlin.openfitness.data.local.dao.ProfileDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WeightEntryDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutDao
import com.scttech.android.kotlin.openfitness.data.local.dao.WorkoutSessionDao
import com.scttech.android.kotlin.openfitness.data.local.entity.ProfileEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.WeightEntryEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.WorkoutEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.WorkoutSessionEntity

@Database(
    entities = [
        ProfileEntity::class,
        WorkoutEntity::class,
        WorkoutSessionEntity::class,
        WeightEntryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class OpenFitnessDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        const val DATABASE_NAME = "openfitness.db"
    }
}
