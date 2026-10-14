package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.scttech.android.kotlin.openfitness.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE profileId = :profileId ORDER BY createdAtEpochMillis DESC")
    fun observeMessages(profileId: Long): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE profileId = :profileId AND isRead = 0")
    fun observeUnreadCount(profileId: Long): Flow<Int>

    /** Returns -1 when [message]'s dedupe key already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE messages SET isRead = 1 WHERE profileId = :profileId")
    suspend fun markAllRead(profileId: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM messages WHERE profileId = :profileId")
    suspend fun clear(profileId: Long)

    @Query("DELETE FROM messages WHERE createdAtEpochMillis < :cutoffEpochMillis")
    suspend fun deleteOlderThan(cutoffEpochMillis: Long)
}
