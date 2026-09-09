package com.scttech.android.kotlin.openfitness.data.repository

import com.scttech.android.kotlin.openfitness.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeEntriesForProfile(profileId: Long): Flow<List<WeightEntry>>
    suspend fun addEntry(entry: WeightEntry): Long
    suspend fun updateEntry(entry: WeightEntry)
    suspend fun deleteEntry(entry: WeightEntry)
}
