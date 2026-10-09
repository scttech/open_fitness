package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scttech.android.kotlin.openfitness.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges WHERE profileId = :profileId ORDER BY earnedAtEpochMillis DESC")
    fun observeBadgesForProfile(profileId: Long): Flow<List<BadgeEntity>>

    @Query("SELECT badgeKey FROM badges WHERE profileId = :profileId")
    suspend fun getBadgeKeys(profileId: Long): List<String>

    /** Returns -1 for a badge the profile already has (the unique index makes re-awarding a no-op). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: BadgeEntity): Long
}
