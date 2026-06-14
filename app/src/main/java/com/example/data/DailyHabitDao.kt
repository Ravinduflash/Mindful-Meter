package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyHabitDao {
    @Query("SELECT * FROM daily_habits WHERE dateStr = :dateStr ORDER BY id ASC")
    fun getHabitsForDate(dateStr: String): Flow<List<DailyHabit>>

    @Query("SELECT * FROM daily_habits ORDER BY dateStr DESC, id ASC")
    fun getAllHabits(): Flow<List<DailyHabit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: DailyHabit)

    @Update
    suspend fun updateHabit(habit: DailyHabit)

    @Query("UPDATE daily_habits SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletion(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM daily_habits WHERE id = :id")
    suspend fun deleteHabitById(id: Int)
}
