package com.example

import android.app.Application
import com.example.data.AppContainer
import com.example.data.DefaultAppContainer
import com.example.worker.WorkScheduler

class MindfulApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
        
        // Schedule periodic habit and 30-min water drink reminders using WorkManager!
        WorkScheduler.scheduleHabitAndWaterReminders(this)
    }

    companion object {
        lateinit var instance: MindfulApplication
            private set
    }
}
