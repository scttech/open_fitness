package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Query("SELECT * FROM weight_entries WHERE profileId = :profileId ORDER BY dateEpochDay ASC")
    fun observeEntriesForProfile(profileId: Long): Flow<List<WeightEntryEntity>>

    @Insert
    suspend fun insert(entry: WeightEntryEntity): Long

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Delete
    suspend fun delete(entry: WeightEntryEntity)
}
