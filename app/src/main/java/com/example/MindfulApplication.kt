package com.example

import android.app.Application
import com.example.data.AppContainer
import com.example.data.DefaultAppContainer
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MindfulApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(this)
                } catch (t: Throwable) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:660978317608:android:a6ae2ec8ef421370bda43f")
                        .setApiKey("AIzaSyD-fakeApiKeyForMindfulMeterDynamicSetup")
                        .setProjectId("mindfulmeter-cloud")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

        container = DefaultAppContainer(this)
    }

    companion object {
        lateinit var instance: MindfulApplication
            private set
    }
}
