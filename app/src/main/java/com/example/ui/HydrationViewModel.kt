package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.MindfulApplication
import com.example.data.PreferencesRepository
import com.example.receiver.HydrationReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HydrationViewModel(
    private val repository: PreferencesRepository,
    private val context: Context
) : ViewModel() {

    val hydrationGoalMl: StateFlow<Int> = repository.hydrationGoalMl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 2000
        )

    val hydrationCurrentMl: StateFlow<Int> = repository.hydrationCurrentMl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val hydrationIntervalMin: StateFlow<Int> = repository.hydrationIntervalMin
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val hydrationNotifsEnabled: StateFlow<Boolean> = repository.hydrationNotifsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Controls whether the majestic rain/bubble animation border should be playing
    private val _isWaterAnimationActive = MutableStateFlow(false)
    val isWaterAnimationActive: StateFlow<Boolean> = _isWaterAnimationActive.asStateFlow()

    init {
        // Daily reset check
        viewModelScope.launch {
            val today = getTodayDateString()
            val lastLoggedDay = repository.hydrationLastLoggedDay.first()
            if (lastLoggedDay.isNotEmpty() && lastLoggedDay != today) {
                // Day changed, auto reset water
                repository.saveHydrationCurrent(0)
                repository.saveHydrationLastLoggedDay(today)
            } else if (lastLoggedDay.isEmpty()) {
                repository.saveHydrationLastLoggedDay(today)
            }
        }
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            val today = getTodayDateString()
            repository.saveHydrationLastLoggedDay(today)
            val current = hydrationCurrentMl.value
            val target = current + amountMl
            repository.saveHydrationCurrent(target)
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            repository.saveHydrationCurrent(0)
            repository.saveHydrationLastLoggedDay(getTodayDateString())
        }
    }

    fun updateGoal(goalMl: Int) {
        viewModelScope.launch {
            repository.saveHydrationGoal(goalMl)
        }
    }

    fun updateInterval(intervalMin: Int) {
        viewModelScope.launch {
            repository.saveHydrationInterval(intervalMin)
            if (intervalMin > 0 && hydrationNotifsEnabled.value) {
                HydrationReceiver.scheduleHydrationAlarm(context, intervalMin)
            } else {
                HydrationReceiver.cancelHydrationAlarm(context)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveHydrationNotifsEnabled(enabled)
            val interval = hydrationIntervalMin.value
            if (enabled && interval > 0) {
                HydrationReceiver.scheduleHydrationAlarm(context, interval)
            } else {
                HydrationReceiver.cancelHydrationAlarm(context)
            }
        }
    }

    fun setWaterAnimationActive(active: Boolean) {
        _isWaterAnimationActive.value = active
    }

    // Direct helper to trigger a test notification/alarm immediately
    fun triggerSimulatedNotification() {
        viewModelScope.launch {
            // Trigger the animation directly in the app
            setWaterAnimationActive(true)
            
            // Send standard immediate notification block
            val msg = "💧 Instant Hydration Tip: Stay hydrated to keep your mind clear and composed!"
            val channelId = "hydration_reminders_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Hydration Reminders",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Periodic water intake reminders and logging triggers"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = android.content.Intent(context, com.example.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("trigger_hydration_animation", true)
            }
            
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                1203,
                openAppIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💧 Hydration Moment")
                .setContentText(msg)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1003, notification)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MindfulApplication)
                HydrationViewModel(application.container.preferencesRepository, application)
            }
        }
    }
}
