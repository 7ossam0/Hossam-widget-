package com.example

import android.app.Application
import android.util.Log
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WidgetStudioApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Global safety exception handler to shield against background/thread crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("WidgetStudio", "Caught unhandled exception in thread ${thread.name}: ${throwable.message}", throwable)
            // Prevent fatal exit on non-main threads
            if (thread.name.contains("main", ignoreCase = true)) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        // Initialize Notification Channels safely
        try {
            com.example.services.PrayerNotificationHelper.createNotificationChannels(this)
        } catch (e: Throwable) {
            Log.e("WidgetStudio", "Error creating notification channels", e)
        }

        // Warm up database safely in background
        applicationScope.launch(Dispatchers.IO) {
            try {
                AppDatabase.getInstance(this@WidgetStudioApplication)
            } catch (e: Throwable) {
                Log.e("WidgetStudio", "Error initializing database", e)
            }
        }
    }
}

