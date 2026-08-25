package com.example

import android.app.Application
import android.util.Log
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetStudioApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Global safety exception handler to prevent unhandled background crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("WidgetStudio", "Caught unhandled exception in thread ${thread.name}", throwable)
            try {
                // If it's a non-fatal background crash, log it rather than killing UI
                defaultHandler?.uncaughtException(thread, throwable)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // Initialize Notification Channels safely
        try {
            com.example.services.PrayerNotificationHelper.createNotificationChannels(this)
        } catch (e: Throwable) {
            Log.e("WidgetStudio", "Error creating notification channels", e)
        }

        // Warm up database safely in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance(this@WidgetStudioApplication)
            } catch (e: Throwable) {
                Log.e("WidgetStudio", "Error initializing database", e)
            }
        }
    }
}

