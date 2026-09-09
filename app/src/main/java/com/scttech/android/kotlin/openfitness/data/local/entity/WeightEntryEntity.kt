package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.WeightEntry
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "weight_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId")],
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val dateEpochDay: Long,
    val weightKg: Double,
    val note: String,
)

fun WeightEntryEntity.asDomainModel() = WeightEntry(
    id = id,
    profileId = profileId,
    date = LocalDate.fromEpochDays(dateEpochDay.toInt()),
    weightKg = weightKg,
    note = note,
)

fun WeightEntry.asEntity() = WeightEntryEntity(
    id = id,
    profileId = profileId,
    dateEpochDay = date.toEpochDays().toLong(),
    weightKg = weightKg,
    note = note,
)
