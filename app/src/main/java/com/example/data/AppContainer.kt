package com.example.data

import android.content.Context

interface AppContainer {
    val moodRepository: MoodRepository
    val preferencesRepository: PreferencesRepository
    val libraryRepository: LibraryRepository
    val communityRepository: CommunityRepository
    val coachingRepository: CoachingRepository
    val dailyIntentionRepository: DailyIntentionRepository
    val dailyHabitRepository: DailyHabitRepository
    val aiInsightRepository: AiInsightRepository
    val focusSessionRepository: FocusSessionRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val moodRepository: MoodRepository by lazy {
        val database = AppDatabase.getDatabase(context)
        OfflineMoodRepository(database.moodDao(), database.journalDao())
    }

    override val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(context.dataStore)
    }

    override val libraryRepository: LibraryRepository by lazy {
        OfflineLibraryRepository()
    }

    override val communityRepository: CommunityRepository by lazy {
        OfflineCommunityRepository()
    }

    override val coachingRepository: CoachingRepository by lazy {
        OfflineCoachingRepository()
    }

    override val dailyIntentionRepository: DailyIntentionRepository by lazy {
        val database = AppDatabase.getDatabase(context)
        OfflineDailyIntentionRepository(database.dailyIntentionDao())
    }

    override val dailyHabitRepository: DailyHabitRepository by lazy {
        val database = AppDatabase.getDatabase(context)
        OfflineDailyHabitRepository(database.dailyHabitDao())
    }

    override val aiInsightRepository: AiInsightRepository by lazy {
        OfflineAiInsightRepository(moodRepository)
    }

    override val focusSessionRepository: FocusSessionRepository by lazy {
        val database = AppDatabase.getDatabase(context)
        OfflineFocusSessionRepository(database.focusSessionDao())
    }
}
