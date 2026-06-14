package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll

interface DailyHabitRepository {
    fun getHabitsForDate(dateStr: String): Flow<List<DailyHabit>>
    fun getAllHabits(): Flow<List<DailyHabit>>
    suspend fun insertHabit(habit: DailyHabit)
    suspend fun updateHabit(habit: DailyHabit)
    suspend fun updateCompletion(id: Int, isCompleted: Boolean)
    suspend fun deleteHabitById(id: Int)
}

class OfflineDailyHabitRepository(
    private val dailyHabitDao: DailyHabitDao
) : DailyHabitRepository {
    override fun getHabitsForDate(dateStr: String): Flow<List<DailyHabit>> =
        dailyHabitDao.getHabitsForDate(dateStr)

    override fun getAllHabits(): Flow<List<DailyHabit>> =
        dailyHabitDao.getAllHabits()

    override suspend fun insertHabit(habit: DailyHabit) {
        dailyHabitDao.insertHabit(habit)
        triggerWidgetUpdate()
    }

    override suspend fun updateHabit(habit: DailyHabit) {
        dailyHabitDao.updateHabit(habit)
        triggerWidgetUpdate()
    }

    override suspend fun updateCompletion(id: Int, isCompleted: Boolean) {
        dailyHabitDao.updateCompletion(id, isCompleted)
        triggerWidgetUpdate()
    }

    override suspend fun deleteHabitById(id: Int) {
        dailyHabitDao.deleteHabitById(id)
        triggerWidgetUpdate()
    }

    private fun triggerWidgetUpdate() {
        try {
            val context = com.example.MindfulApplication.instance
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    com.example.widget.MindfulWidget().updateAll(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (te: Throwable) {
            te.printStackTrace()
        }
    }
}
