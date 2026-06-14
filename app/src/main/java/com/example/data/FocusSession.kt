package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val sessionType: String, // e.g. "Focus" or "Break"
    val soundScapeName: String,
    val dateStr: String, // e.g., "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)
