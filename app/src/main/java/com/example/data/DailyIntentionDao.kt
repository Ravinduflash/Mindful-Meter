package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyIntentionDao {
    @Query("SELECT * FROM daily_intentions WHERE dateStr = :dateStr ORDER BY id ASC")
    fun getIntentionsForDate(dateStr: String): Flow<List<DailyIntention>>

    @Query("SELECT * FROM daily_intentions ORDER BY dateStr DESC, id ASC")
    fun getAllIntentions(): Flow<List<DailyIntention>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntention(intention: DailyIntention)

    @Update
    suspend fun updateIntention(intention: DailyIntention)

    @Query("UPDATE daily_intentions SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletion(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM daily_intentions WHERE id = :id")
    suspend fun deleteIntentionById(id: Int)
}
