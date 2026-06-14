package com.example.data

import kotlinx.coroutines.flow.Flow

interface DailyIntentionRepository {
    fun getIntentionsForDate(dateStr: String): Flow<List<DailyIntention>>
    fun getAllIntentions(): Flow<List<DailyIntention>>
    suspend fun insertIntention(intention: DailyIntention)
    suspend fun updateIntention(intention: DailyIntention)
    suspend fun updateCompletion(id: Int, isCompleted: Boolean)
    suspend fun deleteIntentionById(id: Int)
}

class OfflineDailyIntentionRepository(
    private val dailyIntentionDao: DailyIntentionDao
) : DailyIntentionRepository {
    override fun getIntentionsForDate(dateStr: String): Flow<List<DailyIntention>> =
        dailyIntentionDao.getIntentionsForDate(dateStr)

    override fun getAllIntentions(): Flow<List<DailyIntention>> =
        dailyIntentionDao.getAllIntentions()

    override suspend fun insertIntention(intention: DailyIntention) =
        dailyIntentionDao.insertIntention(intention)

    override suspend fun updateIntention(intention: DailyIntention) =
        dailyIntentionDao.updateIntention(intention)

    override suspend fun updateCompletion(id: Int, isCompleted: Boolean) =
        dailyIntentionDao.updateCompletion(id, isCompleted)

    override suspend fun deleteIntentionById(id: Int) =
        dailyIntentionDao.deleteIntentionById(id)
}
