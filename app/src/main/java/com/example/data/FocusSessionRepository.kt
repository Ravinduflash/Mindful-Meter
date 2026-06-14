package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FocusSessionRepository {
    val allSessions: Flow<List<FocusSession>>
    val totalFocusMinutesFlow: Flow<Int>
    suspend fun insert(session: FocusSession)
    suspend fun deleteById(id: Int)
}

class OfflineFocusSessionRepository(private val focusSessionDao: FocusSessionDao) : FocusSessionRepository {
    override val allSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()

    override val totalFocusMinutesFlow: Flow<Int> = focusSessionDao.getTotalFocusMinutesFlow().map { it ?: 0 }

    override suspend fun insert(session: FocusSession) {
        focusSessionDao.insertSession(session)
    }

    override suspend fun deleteById(id: Int) {
        focusSessionDao.deleteSessionById(id)
    }
}
