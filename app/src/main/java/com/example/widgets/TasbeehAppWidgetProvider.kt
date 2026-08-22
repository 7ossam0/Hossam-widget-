package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasbeehAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TASBEEH_INCREMENT -> {
                val db = AppDatabase.getInstance(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val active = db.tasbeehDao().getActiveTasbeeh() ?: db.tasbeehDao().getFirstTasbeeh()
                        val targetId = active?.id ?: 1L
                        db.tasbeehDao().incrementCount(targetId)
                        updateTasbeehWidgets(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ACTION_TASBEEH_RESET -> {
                val db = AppDatabase.getInstance(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val active = db.tasbeehDao().getActiveTasbeeh() ?: db.tasbeehDao().getFirstTasbeeh()
                        val targetId = active?.id ?: 1L
                        db.tasbeehDao().resetCount(targetId)
                        updateTasbeehWidgets(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_TASBEEH_INCREMENT = "com.example.widgets.ACTION_TASBEEH_INCREMENT"
        const val ACTION_TASBEEH_RESET = "com.example.widgets.ACTION_TASBEEH_RESET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_tasbeeh_layout)

            // Open Main App on root click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_root, openAppPendingIntent)

            // Increment count button
            val incrementIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_TASBEEH_INCREMENT
            }
            val incrementPendingIntent = PendingIntent.getBroadcast(
                context, 101, incrementIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_count_btn, incrementPendingIntent)

            // Reset button
            val resetIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_TASBEEH_RESET
            }
            val resetPendingIntent = PendingIntent.getBroadcast(
                context, 102, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_reset_btn, resetPendingIntent)

            // Query database asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val item = db.tasbeehDao().getActiveTasbeeh() ?: db.tasbeehDao().getTasbeehById(1)
                    if (item != null) {
                        views.setTextViewText(R.id.tasbeeh_widget_title, item.title)
                        views.setTextViewText(R.id.tasbeeh_widget_count, "${item.currentCount}")
                        val goalText = if (item.targetCount > 0) "الهدف: ${item.targetCount}" else "عداد مفتوح"
                        views.setTextViewText(R.id.tasbeeh_widget_goal, goalText)
                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun updateTasbeehWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TasbeehAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
