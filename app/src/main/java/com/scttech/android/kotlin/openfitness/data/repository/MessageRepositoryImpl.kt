package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.MessageDao
import com.scttech.android.kotlin.openfitness.data.local.entity.MessageEntity
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.domain.model.AlertDestinationType
import com.scttech.android.kotlin.openfitness.domain.model.AppMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
) : MessageRepository {

    override fun observeMessages(profileId: Long): Flow<List<AppMessage>> =
        messageDao.observeMessages(profileId).map { list -> list.map { it.asDomainModel() } }

    override fun observeUnreadCount(profileId: Long): Flow<Int> = messageDao.observeUnreadCount(profileId)

    override suspend fun record(
        profileId: Long,
        dedupeKey: String,
        title: String,
        body: String,
        destinationType: AlertDestinationType?,
        destinationId: Long?,
    ): Long? {
        purgeExpired()
        val id = messageDao.insert(
            MessageEntity(
                profileId = profileId,
                dedupeKey = dedupeKey,
                title = title,
                body = body,
                createdAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
                isRead = false,
                destinationType = destinationType?.name,
                destinationId = destinationId,
            ),
        )
        return id.takeIf { it != -1L }
    }

    override suspend fun markRead(id: Long) = messageDao.markRead(id)

    override suspend fun markAllRead(profileId: Long) = messageDao.markAllRead(profileId)

    override suspend fun delete(id: Long) = messageDao.delete(id)

    override suspend fun clear(profileId: Long) = messageDao.clear(profileId)

    override suspend fun purgeExpired() {
        val cutoff = Clock.System.now() - MessageRepository.RETENTION_HOURS.hours
        messageDao.deleteOlderThan(cutoff.toEpochMilliseconds())
    }
}
