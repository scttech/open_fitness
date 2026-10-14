package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.ProgramDaySkip
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEvent
import com.scttech.android.kotlin.openfitness.domain.model.ScheduledEventKind
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Sets are stored as comma-separated strings - weekday ordinals for [repeatDays], epoch days for
 * the date sets - since they're only ever read and written alongside their parent event.
 */
@Entity(
    tableName = "scheduled_events",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId"), Index("workoutId"), Index("programId")],
)
data class ScheduledEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val kind: String,
    val title: String,
    val workoutId: Long?,
    val programId: Long?,
    val dateEpochDay: Long,
    val startMinutes: Int?,
    val notes: String,
    val reminderMinutesBefore: Int?,
    val repeatDays: String,
    val repeatUntilEpochDay: Long?,
    val completedEpochDays: String,
    val skippedEpochDays: String,
)

private fun String.toEpochDaySet(): Set<LocalDate> =
    split(',').filter { it.isNotBlank() }.map { LocalDate.fromEpochDays(it.toInt()) }.toSet()

private fun Set<LocalDate>.toStorage(): String = joinToString(",") { it.toEpochDays().toString() }

fun ScheduledEventEntity.asDomainModel() = ScheduledEvent(
    id = id,
    profileId = profileId,
    kind = ScheduledEventKind.valueOf(kind),
    title = title,
    workoutId = workoutId,
    programId = programId,
    date = LocalDate.fromEpochDays(dateEpochDay.toInt()),
    startMinutes = startMinutes,
    notes = notes,
    reminderMinutesBefore = reminderMinutesBefore,
    repeatDays = repeatDays.split(',').filter { it.isNotBlank() }.map { DayOfWeek.entries[it.toInt()] }.toSet(),
    repeatUntil = repeatUntilEpochDay?.let { LocalDate.fromEpochDays(it.toInt()) },
    completedDates = completedEpochDays.toEpochDaySet(),
    skippedDates = skippedEpochDays.toEpochDaySet(),
)

fun ScheduledEvent.asEntity() = ScheduledEventEntity(
    id = id,
    profileId = profileId,
    kind = kind.name,
    title = title,
    workoutId = workoutId,
    programId = programId,
    dateEpochDay = date.toEpochDays().toLong(),
    startMinutes = startMinutes,
    notes = notes,
    reminderMinutesBefore = reminderMinutesBefore,
    repeatDays = repeatDays.sortedBy { it.ordinal }.joinToString(",") { it.ordinal.toString() },
    repeatUntilEpochDay = repeatUntil?.toEpochDays()?.toLong(),
    completedEpochDays = completedDates.toStorage(),
    skippedEpochDays = skippedDates.toStorage(),
)

@Entity(
    tableName = "program_day_skips",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId"), Index(value = ["programId", "dateEpochDay"], unique = true)],
)
data class ProgramDaySkipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val programId: Long,
    val dateEpochDay: Long,
)

fun ProgramDaySkipEntity.asDomainModel() = ProgramDaySkip(
    profileId = profileId,
    programId = programId,
    date = LocalDate.fromEpochDays(dateEpochDay.toInt()),
)
