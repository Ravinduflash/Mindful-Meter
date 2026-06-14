package com.example.data

import android.content.Context

interface AppContainer {
    val dailyHabitRepository: DailyHabitRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val dailyHabitRepository: DailyHabitRepository by lazy {
        OfflineDailyHabitRepository(AppDatabase.getDatabase(context).dailyHabitDao())
    }
}

interface DailyHabitRepository {
    fun getHabitsForDate(dateStr: String): kotlinx.coroutines.flow.Flow<List<DailyHabit>>
    suspend fun insertHabit(habit: DailyHabit)
    suspend fun updateHabit(habit: DailyHabit)
    suspend fun deleteHabit(id: Int)
}

class OfflineDailyHabitRepository(private val dailyHabitDao: DailyHabitDao) : DailyHabitRepository {
    override fun getHabitsForDate(dateStr: String) = dailyHabitDao.getHabitsForDate(dateStr)
    override suspend fun insertHabit(habit: DailyHabit) = dailyHabitDao.insertHabit(habit)
    override suspend fun updateHabit(habit: DailyHabit) = dailyHabitDao.updateHabit(habit)
    override suspend fun deleteHabit(id: Int) = dailyHabitDao.deleteHabitById(id)
}
