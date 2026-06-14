package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WaterHydrationManager(context: Context) {

    private val sharedPrefs = context.getSharedPreferences("mindful_prefs", Context.MODE_PRIVATE)

    private val _currentWaterProgress = MutableStateFlow(0)
    val currentWaterProgress: StateFlow<Int> = _currentWaterProgress

    private val _dailyWaterCap = MutableStateFlow(2500) // Default 2500ml cap
    val dailyWaterCap: StateFlow<Int> = _dailyWaterCap

    private val _showRainBorderAnimation = MutableStateFlow(false)
    val showRainBorderAnimation: StateFlow<Boolean> = _showRainBorderAnimation

    init {
        val todayStr = getTodayStr()
        val currentDrank = sharedPrefs.getInt("water_drank_$todayStr", 0)
        val savedCap = sharedPrefs.getInt("water_cap_$todayStr", 2500)
        _currentWaterProgress.value = currentDrank
        _dailyWaterCap.value = savedCap
    }

    fun recordWaterIntake(amountMl: Int) {
        val todayStr = getTodayStr()
        val nextAmount = _currentWaterProgress.value + amountMl
        _currentWaterProgress.value = nextAmount
        sharedPrefs.edit().putInt("water_drank_$todayStr", nextAmount).apply()

        // Trigger screen rain animation for water drink feedback
        triggerRainBorderAnimation()
    }

    fun updateDailyCap(newCapMl: Int) {
        if (newCapMl <= 0) return
        val todayStr = getTodayStr()
        _dailyWaterCap.value = newCapMl
        sharedPrefs.edit().putInt("water_cap_$todayStr", newCapMl).apply()
    }

    fun triggerRainBorderAnimation() {
        _showRainBorderAnimation.value = true
        // Store in shared prefs for worker parity
        sharedPrefs.edit().putBoolean("trigger_water_animation", true).apply()
    }

    fun dismissRainBorderAnimation() {
        _showRainBorderAnimation.value = false
        sharedPrefs.edit().putBoolean("trigger_water_animation", false).apply()
    }

    private fun getTodayStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        @Volatile
        private var INSTANCE: WaterHydrationManager? = null

        fun getInstance(context: Context): WaterHydrationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = WaterHydrationManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
