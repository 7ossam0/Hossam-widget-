package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
        val db = AppDatabase.getInstance(context)

        when (intent.action) {
            ACTION_TASBEEH_INCREMENT -> {
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
            ACTION_TASBEEH_NEXT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val all = db.tasbeehDao().getAllTasbeehList()
                        if (all.isNotEmpty()) {
                            val active = db.tasbeehDao().getActiveTasbeeh() ?: all.first()
                            val currentIndex = all.indexOfFirst { it.id == active.id }
                            val nextIndex = if (currentIndex != -1 && currentIndex + 1 < all.size) currentIndex + 1 else 0
                            val nextItem = all[nextIndex]
                            db.tasbeehDao().selectActiveTasbeeh(nextItem.id)
                            updateTasbeehWidgets(context)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            ACTION_TASBEEH_PREV -> {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val all = db.tasbeehDao().getAllTasbeehList()
                        if (all.isNotEmpty()) {
                            val active = db.tasbeehDao().getActiveTasbeeh() ?: all.first()
                            val currentIndex = all.indexOfFirst { it.id == active.id }
                            val prevIndex = if (currentIndex > 0) currentIndex - 1 else all.size - 1
                            val prevItem = all[prevIndex]
                            db.tasbeehDao().selectActiveTasbeeh(prevItem.id)
                            updateTasbeehWidgets(context)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    companion object {
        const val PREFS_NAME = "tasbeeh_widget_prefs"
        const val PREF_LOCK_OPEN_APP = "tasbeeh_lock_open_app" // Default true = stay on home screen

        const val ACTION_TASBEEH_INCREMENT = "com.example.widgets.ACTION_TASBEEH_INCREMENT"
        const val ACTION_TASBEEH_RESET = "com.example.widgets.ACTION_TASBEEH_RESET"
        const val ACTION_TASBEEH_NEXT = "com.example.widgets.ACTION_TASBEEH_NEXT"
        const val ACTION_TASBEEH_PREV = "com.example.widgets.ACTION_TASBEEH_PREV"

        fun isLockOpenAppEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_LOCK_OPEN_APP, true)
        }

        fun setLockOpenAppEnabled(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_LOCK_OPEN_APP, enabled).apply()
            updateTasbeehWidgets(context)
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_tasbeeh_layout)
            val isLocked = isLockOpenAppEnabled(context)

            // 1. Increment PendingIntent (for +1 button, big count number, and title)
            val incrementIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_TASBEEH_INCREMENT
            }
            val incrementPendingIntent = PendingIntent.getBroadcast(
                context, 101, incrementIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_count, incrementPendingIntent)
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_count_btn, incrementPendingIntent)
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_title, incrementPendingIntent)

            // 2. Open App PendingIntent (or redirect to increment if strictly locked)
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (isLocked) {
                // If locked, root click also increments count so user never exits home screen accidentally!
                views.setOnClickPendingIntent(R.id.tasbeeh_widget_root, incrementPendingIntent)
                // App settings button explicitly allows entering app if pressed intentionally
                views.setOnClickPendingIntent(R.id.tasbeeh_widget_open_app_btn, openAppPendingIntent)
            } else {
                views.setOnClickPendingIntent(R.id.tasbeeh_widget_root, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.tasbeeh_widget_open_app_btn, openAppPendingIntent)
            }

            // 3. Reset PendingIntent
            val resetIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_TASBEEH_RESET
            }
            val resetPendingIntent = PendingIntent.getBroadcast(
                context, 102, resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_reset_btn, resetPendingIntent)

            // 4. Next / Prev Dhikr PendingIntents
            val nextIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_TASBEEH_NEXT
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context, 103, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_next_btn, nextPendingIntent)

            val prevIntent = Intent(context, TasbeehAppWidgetProvider::class.java).apply {
                action = ACTION_TASBEEH_PREV
            }
            val prevPendingIntent = PendingIntent.getBroadcast(
                context, 104, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_widget_prev_btn, prevPendingIntent)

            // Query database asynchronously and update view
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val item = db.tasbeehDao().getActiveTasbeeh() ?: db.tasbeehDao().getFirstTasbeeh()
                    if (item != null) {
                        views.setTextViewText(R.id.tasbeeh_widget_title, item.title)
                        views.setTextViewText(R.id.tasbeeh_widget_count, "${item.currentCount}")
                        val goalText = if (item.targetCount > 0) "الهدف: ${item.targetCount} (المجموع: ${item.totalLifetimeCount})" else "عداد مفتوح"
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
