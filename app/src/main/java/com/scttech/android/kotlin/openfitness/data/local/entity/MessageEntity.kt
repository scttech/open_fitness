package com.scttech.android.kotlin.openfitness.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.domain.model.AppMessage
import kotlinx.datetime.Instant

/** [dedupeKey] is unique, so the same alert for the same day is only ever recorded once. */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("profileId"), Index(value = ["dedupeKey"], unique = true)],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    val dedupeKey: String,
    val title: String,
    val body: String,
    val createdAtEpochMillis: Long,
    val isRead: Boolean,
    val destinationType: String?,
    val destinationId: Long?,
)

fun MessageEntity.asDomainModel() = AppMessage(
    id = id,
    profileId = profileId,
    title = title,
    body = body,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis),
    isRead = isRead,
    destinationType = destinationType?.let { name -> AlertDestinationType.entries.firstOrNull { it.name == name } },
    destinationId = destinationId,
)
