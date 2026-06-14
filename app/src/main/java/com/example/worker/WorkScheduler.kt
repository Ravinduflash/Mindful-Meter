package com.example.worker

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val HABIT_WORK_NAME = "DailyHabitCheckWork"
    private const val WATER_WORK_NAME = "WaterReminder30MinWork"

    fun scheduleHabitAndWaterReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // 1. Calculate schedule delay for 8 PM checklist reminder
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 20)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = calendar.timeInMillis - now

        val habitRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            HABIT_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            habitRequest
        )

        // 2. Schedule 30-minute water reminders
        val waterRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(30, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WATER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            waterRequest
        )
    }

    // Helper to trigger water reminder immediately for simulation/testing
    fun triggerWaterReminderImmediately(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val oneTimeRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .build()
        workManager.enqueue(oneTimeRequest)
    }
}
