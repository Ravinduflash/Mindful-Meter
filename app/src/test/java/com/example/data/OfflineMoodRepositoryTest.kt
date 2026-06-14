package com.example.data

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OfflineMoodRepositoryTest {

    private lateinit var moodDao: MoodDao
    private lateinit var journalDao: JournalDao
    private lateinit var repository: OfflineMoodRepository

    @BeforeEach
    fun setUp() {
        moodDao = mockk(relaxed = true)
        journalDao = mockk(relaxed = true)
        repository = OfflineMoodRepository(moodDao, journalDao)
    }

    @Test
    fun `getAllLogs returns flow of logs from moodDao`() = runTest {
        val expectedLogs = listOf(
            MoodLog(id = 1, mood = "Happy", note = "Great day!", timestamp = 1000L),
            MoodLog(id = 2, mood = "Calm", note = "Quiet evening", timestamp = 2000L)
        )
        every { moodDao.getAllLogs() } returns flowOf(expectedLogs)

        val result = repository.getAllLogs().first()

        assertEquals(expectedLogs, result)
        verify(exactly = 1) { moodDao.getAllLogs() }
    }

    @Test
    fun `insertLog inserts log to moodDao`() = runTest {
        val log = MoodLog(id = 1, mood = "Energetic", note = "Workout done", timestamp = 3000L)
        coEvery { moodDao.insertLog(log) } returns Unit

        repository.insertLog(log)

        coVerify(exactly = 1) { moodDao.insertLog(log) }
    }

    @Test
    fun `deleteLogById deletes log from moodDao`() = runTest {
        val id = 42
        coEvery { moodDao.deleteLogById(id) } returns Unit

        repository.deleteLogById(id)

        coVerify(exactly = 1) { moodDao.deleteLogById(id) }
    }

    @Test
    fun `getAllJournalEntries returns flow of entries from journalDao`() = runTest {
        val expectedEntries = listOf(
            JournalEntry(id = 10, title = "Morning Meditation", content = "Felt relaxed", timestamp = 5000L),
            JournalEntry(id = 11, title = "Reflections", content = "Productive day", timestamp = 6000L)
        )
        every { journalDao.getAllEntries() } returns flowOf(expectedEntries)

        val result = repository.getAllJournalEntries().first()

        assertEquals(expectedEntries, result)
        verify(exactly = 1) { journalDao.getAllEntries() }
    }

    @Test
    fun `insertJournalEntry inserts entry to journalDao`() = runTest {
        val entry = JournalEntry(id = 12, title = "Night routine", content = "Reading book", timestamp = 7000L)
        coEvery { journalDao.insertEntry(entry) } returns Unit

        repository.insertJournalEntry(entry)

        coVerify(exactly = 1) { journalDao.insertEntry(entry) }
    }

    @Test
    fun `deleteJournalEntryById deletes entry from journalDao`() = runTest {
        val id = 99
        coEvery { journalDao.deleteEntryById(id) } returns Unit

        repository.deleteJournalEntryById(id)

        coVerify(exactly = 1) { journalDao.deleteEntryById(id) }
    }
}
