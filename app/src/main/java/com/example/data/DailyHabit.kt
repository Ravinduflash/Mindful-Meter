package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_habits")
data class DailyHabit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isCompleted: Boolean = false,
    val dateStr: String, // e.g., "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)
