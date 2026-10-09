package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A badge a profile has earned. [badgeKey] is the [com.scttech.android.kotlin.openfitness.domain.model.Badge] name. */
@Entity(
    tableName = "badges",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["profileId", "badgeKey"], unique = true)],
)
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val badgeKey: String,
    val earnedAtEpochMillis: Long,
)
