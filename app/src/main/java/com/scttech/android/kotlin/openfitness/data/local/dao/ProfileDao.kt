package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY createdAtEpochMillis ASC")
    fun observeProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    fun observeProfile(id: Long): Flow<ProfileEntity?>

    @Insert
    suspend fun insert(profile: ProfileEntity): Long

    @Update
    suspend fun update(profile: ProfileEntity)

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("UPDATE profiles SET timerSoundEnabled = :enabled WHERE id = :profileId")
    suspend fun updateTimerSoundEnabled(profileId: Long, enabled: Boolean)

    @Query("UPDATE profiles SET timerWorkColorArgb = :argb WHERE id = :profileId")
    suspend fun updateTimerWorkColorArgb(profileId: Long, argb: Int)

    @Query("UPDATE profiles SET timerRestColorArgb = :argb WHERE id = :profileId")
    suspend fun updateTimerRestColorArgb(profileId: Long, argb: Int)
}
