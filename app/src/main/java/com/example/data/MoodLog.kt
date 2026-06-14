package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: String, // Happy, Calm, Stressed, Anxious, Sad
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
