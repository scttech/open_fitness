package com.scttech.android.kotlin.openfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.scttech.android.kotlin.openfitness.data.local.entity.ProgramDaySkipEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.ScheduledEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM scheduled_events WHERE profileId = :profileId ORDER BY dateEpochDay, startMinutes")
    fun observeEventsForProfile(profileId: Long): Flow<List<ScheduledEventEntity>>

    @Query("SELECT * FROM scheduled_events WHERE id = :id")
    suspend fun getEvent(id: Long): ScheduledEventEntity?

    @Query("SELECT * FROM scheduled_events WHERE profileId = :profileId")
    suspend fun getEventsForProfile(profileId: Long): List<ScheduledEventEntity>

    @Insert
    suspend fun insert(event: ScheduledEventEntity): Long

    @Update
    suspend fun update(event: ScheduledEventEntity)

    @Query("DELETE FROM scheduled_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("SELECT * FROM program_day_skips WHERE profileId = :profileId")
    fun observeProgramDaySkips(profileId: Long): Flow<List<ProgramDaySkipEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProgramDaySkip(skip: ProgramDaySkipEntity)
}
