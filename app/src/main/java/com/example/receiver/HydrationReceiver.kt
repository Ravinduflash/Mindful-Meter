package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlin.random.Random

class HydrationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HydrationReceiver", "Hydration reminder alarm received!")
        
        val messages = listOf(
            "Time for a refreshing glass of water! 💧",
            "Be kind to your body. Take a cool sip of water now. 🌊",
            "Hydration boosts focus. Sip some water to stay radiant! ✨",
            "Your brain is 75% water. Replenish it with a fresh glass! 🧠💧",
            "Sip, swallow, smile. Time to hydrate! 😊🥤",
            "Track your water goal! Take a quick drink break now. 🏆"
        )
        val message = messages[Random.nextInt(messages.size)]
        
        showHydrationNotification(context, message)

        // Reschedule next alarm based on saved interval
        val sharedPrefs = context.getSharedPreferences("mindful_meter_hydration", Context.MODE_PRIVATE)
        val intervalMin = sharedPrefs.getInt("hydration_interval_min", 0)
        if (intervalMin > 0) {
            scheduleHydrationAlarm(context, intervalMin)
        }
    }

    private fun showHydrationNotification(context: Context, message: String) {
        val channelId = "hydration_reminders_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Periodic water intake reminders and logging triggers"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("trigger_hydration_animation", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            1201, // unique request code
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 Hydration Moment")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }

    companion object {
        fun scheduleHydrationAlarm(context: Context, intervalMinutes: Int) {
            if (intervalMinutes <= 0) {
                cancelHydrationAlarm(context)
                return
            }

            // Save interval in SharedPreferences for easy Access on receive (datastore is suspend-only)
            val sharedPrefs = context.getSharedPreferences("mindful_meter_hydration", Context.MODE_PRIVATE)
            sharedPrefs.edit().putInt("hydration_interval_min", intervalMinutes).apply()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HydrationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1202,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate trigger time
            val triggerTimeMs = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L)
            Log.d("HydrationReceiver", "Scheduling next hydration alarm in $intervalMinutes minutes ($triggerTimeMs)")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
        }

        fun cancelHydrationAlarm(context: Context) {
            val sharedPrefs = context.getSharedPreferences("mindful_meter_hydration", Context.MODE_PRIVATE)
            sharedPrefs.edit().putInt("hydration_interval_min", 0).apply()

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HydrationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1202,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("HydrationReceiver", "Cancelled hydration alarm safely.")
            }
        }
    }
}
