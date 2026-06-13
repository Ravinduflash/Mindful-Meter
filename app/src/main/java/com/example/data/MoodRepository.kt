package com.example.data

import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    fun getAllLogs(): Flow<List<MoodLog>>
    suspend fun insertLog(log: MoodLog)
    suspend fun deleteLogById(id: Int)

    // Journaling Operations
    fun getAllJournalEntries(): Flow<List<JournalEntry>>
    suspend fun insertJournalEntry(entry: JournalEntry)
    suspend fun deleteJournalEntryById(id: Int)
}

class OfflineMoodRepository(
    private val moodDao: MoodDao,
    private val journalDao: JournalDao
) : MoodRepository {
    override fun getAllLogs(): Flow<List<MoodLog>> = moodDao.getAllLogs()

    override suspend fun insertLog(log: MoodLog) = moodDao.insertLog(log)

    override suspend fun deleteLogById(id: Int) = moodDao.deleteLogById(id)

    // Journaling Implementations
    override fun getAllJournalEntries(): Flow<List<JournalEntry>> = journalDao.getAllEntries()

    override suspend fun insertJournalEntry(entry: JournalEntry) = journalDao.insertEntry(entry)

    override suspend fun deleteJournalEntryById(id: Int) = journalDao.deleteEntryById(id)
}
