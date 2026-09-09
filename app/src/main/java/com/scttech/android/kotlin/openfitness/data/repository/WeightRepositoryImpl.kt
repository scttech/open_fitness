package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.data.local.dao.WeightEntryDao
import com.scttech.android.kotlin.openfitness.data.local.entity.asDomainModel
import com.scttech.android.kotlin.openfitness.data.local.entity.asEntity
import com.scttech.android.kotlin.openfitness.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val weightEntryDao: WeightEntryDao,
) : WeightRepository {

    override fun observeEntriesForProfile(profileId: Long): Flow<List<WeightEntry>> =
        weightEntryDao.observeEntriesForProfile(profileId).map { entities -> entities.map { it.asDomainModel() } }

    override suspend fun addEntry(entry: WeightEntry): Long = weightEntryDao.insert(entry.asEntity())

    override suspend fun updateEntry(entry: WeightEntry) {
        weightEntryDao.update(entry.asEntity())
    }

    override suspend fun deleteEntry(entry: WeightEntry) {
        weightEntryDao.delete(entry.asEntity())
    }
}
