package com.example.ui

import com.example.data.MoodLog
import com.example.data.MoodRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoodViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: MoodRepository
    private lateinit var aiInsightRepository: com.example.data.AiInsightRepository
    private lateinit var viewModel: MoodViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        aiInsightRepository = mockk(relaxed = true)
        coEvery { aiInsightRepository.getWeeklyInsight() } returns "Great weekly insight"
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allLogs maps logs from repository of flow correctly`() = runTest(testDispatcher) {
        val expectedLogs = listOf(
            MoodLog(id = 1, mood = "Stressed", note = "Lots of work", timestamp = 500L)
        )
        val flow = MutableStateFlow(expectedLogs)
        every { repository.getAllLogs() } returns flow

        viewModel = MoodViewModel(repository, aiInsightRepository)

        val job = backgroundScope.launch(testDispatcher) {
            viewModel.allLogs.collect {}
        }

        advanceUntilIdle()

        assertEquals(expectedLogs, viewModel.allLogs.value)
        job.cancel()
    }

    @Test
    fun `logMood inserts log to repository`() = runTest(testDispatcher) {
        every { repository.getAllLogs() } returns MutableStateFlow(emptyList())
        viewModel = MoodViewModel(repository, aiInsightRepository)

        viewModel.logMood("Anxious", "Important speech soon")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.insertLog(match {
                it.mood == "Anxious" && it.note == "Important speech soon"
            })
        }
    }

    @Test
    fun `deleteLog deletes log from repository`() = runTest(testDispatcher) {
        every { repository.getAllLogs() } returns MutableStateFlow(emptyList())
        viewModel = MoodViewModel(repository, aiInsightRepository)

        viewModel.deleteLog(42)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.deleteLogById(42)
        }
    }
}
