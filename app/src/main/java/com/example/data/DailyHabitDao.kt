package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHabitDao {
    @Query("SELECT * FROM daily_habits WHERE dateStr = :dateStr ORDER BY id ASC")
    fun getHabitsForDate(dateStr: String): Flow<List<DailyHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: DailyHabit)

    @Update
    suspend fun updateHabit(habit: DailyHabit)

    @Query("DELETE FROM daily_habits WHERE id = :id")
    suspend fun deleteHabitById(id: Int)
}
