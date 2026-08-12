package com.example.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

object WidgetManagerHelper {
    const val ACTION_REFRESH_ALL = "com.example.widgets.ACTION_REFRESH_ALL"
    const val ACTION_NEXT = "com.example.widgets.ACTION_NEXT"
    const val ACTION_PREV = "com.example.widgets.ACTION_PREV"
    const val EXTRA_WIDGET_ID = "appWidgetId"

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, WidgetStudioAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        val intent = Intent(context, WidgetStudioAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)

        // Notify list data changed for all scrollable widgets
        if (appWidgetIds.isNotEmpty()) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, com.example.R.id.widget_list_view)
        }
    }

    fun updateSingleWidget(context: Context, appWidgetId: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val intent = Intent(context, WidgetStudioAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        context.sendBroadcast(intent)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, com.example.R.id.widget_list_view)
    }

    fun scheduleRotationWorker(context: Context, intervalMinutes: Long) {
        val workManager = WorkManager.getInstance(context)
        val safeInterval = if (intervalMinutes < 15) 15L else intervalMinutes // WorkManager minimum 15 mins

        val rotationWorkRequest = PeriodicWorkRequestBuilder<WidgetRotationWorker>(
            safeInterval, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder().build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "WidgetRotationWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            rotationWorkRequest
        )
    }
}
