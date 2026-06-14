package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE sessionType = 'Focus'")
    fun getTotalFocusMinutesFlow(): Flow<Int?>
}
