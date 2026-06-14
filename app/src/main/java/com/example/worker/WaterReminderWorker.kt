package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity

class WaterReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            sendWaterNotification()
            // Store reminder trigger state in shared preferences to animate within the app
            val sharedPrefs = applicationContext.getSharedPreferences("mindful_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putLong("last_water_reminder_time", System.currentTimeMillis())
                .putBoolean("trigger_water_animation", true)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
        return Result.success()
    }

    private fun sendWaterNotification() {
        val context = applicationContext
        val channelId = "water_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Water Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Keeps you hydrated by reminding you to drink water every 30 minutes"
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("action", "drink_water_animation")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_slideshow) // Safe built-in icon
            .setContentTitle("Time to Hydrate! 💧")
            .setContentText("30 minutes have passed! Take a sip, reach your daily water cap, and keep your body refreshed.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(8002, notification)
    }
}
