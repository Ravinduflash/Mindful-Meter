package com.example.data

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.example.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AiInsightRepository {
    suspend fun getWeeklyInsight(): String
}

class OfflineAiInsightRepository(
    private val moodRepository: MoodRepository
) : AiInsightRepository {

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    override suspend fun getWeeklyInsight(): String = withContext(Dispatchers.IO) {
        try {
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L

            // Fetch logs and filter to last 7 days
            val allLogs = moodRepository.getAllLogs().first()
            val recentLogs = allLogs.filter { it.timestamp >= sevenDaysAgo }

            // Fetch journal entries and filter to last 7 days
            val allJournalEntries = moodRepository.getAllJournalEntries().first()
            val recentJournalEntries = allJournalEntries.filter { it.timestamp >= sevenDaysAgo }

            if (recentLogs.isEmpty() && recentJournalEntries.isEmpty()) {
                return@withContext "You have not recorded any mood logs or journal entries in the last 7 days. Start logging your thoughts and emotions to receive personalized mindfulness coaching insights!"
            }

            // Build formatting prompt
            val logsSummary = recentLogs.joinToString("\n") { log ->
                "- Mood: ${log.mood}, Note: ${log.note}"
            }
            val journalSummary = recentJournalEntries.joinToString("\n") { entry ->
                "- Title: ${entry.title}, Content: ${entry.content}"
            }

            val systemPrompt = """
                You are a compassionate mindfulness coach.
                Your task is to identify emotional patterns in the user's data from the last 7 days and suggest one specific app feature to help them achieve emotional equilibrium (e.g., "Cosmic Noise soundscape", "Box Breathing", "Relaxing Breathing (4-7-8)", or "Gratitude Journal").
                Keep the tone warm, modern, and empathetic. Address the user directly and be highly concise (under 120 words). Ensure that you refer to actual app features like "Cosmic Noise soundscape", "Box Breathing", or "Relaxing Breathing" naturally in your advice.
            """.trimIndent()

            val userMessage = """
                Here is my mindful data from the last 7 days:
                
                Mood Logs:
                $logsSummary
                
                Journal Entries:
                $journalSummary
                
                Provide my personalized weekly insight and compassionate recommendation. Keep the coaching practical and short.
            """.trimIndent()

            val response = model.generateContent(
                content {
                    text(systemPrompt)
                    text(userMessage)
                }
            )

            response.text ?: "Could not generate weekly insight. Please try again later."
        } catch (e: Exception) {
            Log.e("AiInsightRepository", "Error generating weekly insight", e)
            "Error: ${e.localizedMessage ?: "Unable to connect to AI server. Please check your network and API key settings."}"
        }
    }
}
