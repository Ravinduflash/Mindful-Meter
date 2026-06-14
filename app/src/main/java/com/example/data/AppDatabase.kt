package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MoodLog::class, JournalEntry::class, DailyIntention::class, DailyHabit::class, FocusSession::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun dailyIntentionDao(): DailyIntentionDao
    abstract fun dailyHabitDao(): DailyHabitDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mindful_meter_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { Instance = it }
            }
        }
    }
}
