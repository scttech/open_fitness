package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.Profile
import kotlinx.datetime.Instant

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val colorIndex: Int,
    val createdAtEpochMillis: Long,
    val timerSoundEnabled: Boolean = true,
    val timerWorkColorArgb: Int? = null,
    val timerRestColorArgb: Int? = null,
)

fun ProfileEntity.asDomainModel() = Profile(
    id = id,
    name = name,
    colorIndex = colorIndex,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
    timerSoundEnabled = timerSoundEnabled,
    timerWorkColorArgb = timerWorkColorArgb,
    timerRestColorArgb = timerRestColorArgb,
)

fun Profile.asEntity() = ProfileEntity(
    id = id,
    name = name,
    colorIndex = colorIndex,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
    timerSoundEnabled = timerSoundEnabled,
    timerWorkColorArgb = timerWorkColorArgb,
    timerRestColorArgb = timerRestColorArgb,
)
