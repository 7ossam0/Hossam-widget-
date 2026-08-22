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

        // Initialize Notification Channels
        try {
            com.example.services.PrayerNotificationHelper.createNotificationChannels(this)
        } catch (e: Throwable) {
            Log.e("WidgetStudio", "Error creating notification channels", e)
        }

        // Global crash guard to prevent unexpected shutdowns
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("WidgetStudio", "Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Warm up database in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance(this@WidgetStudioApplication)
            } catch (e: Throwable) {
                Log.e("WidgetStudio", "Error initializing database", e)
            }
        }
    }
}
