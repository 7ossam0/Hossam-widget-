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
import com.example.data.prayer.PrayerTimeCalculator
import java.util.Calendar

class PrayerTimesAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_PRAYER_REFRESH) {
            updatePrayerWidgets(context)
        }
    }

    companion object {
        const val ACTION_PRAYER_REFRESH = "com.example.widgets.ACTION_PRAYER_REFRESH"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times_layout)

            // Open Main App on root click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.prayer_widget_root, openAppPendingIntent)

            // Calculate schedule
            val schedule = PrayerTimeCalculator.calculateDailySchedule(
                calendar = Calendar.getInstance()
            )

            views.setTextViewText(R.id.prayer_widget_city, "${schedule.cityName} 🕌")
            views.setTextViewText(R.id.prayer_widget_sky_phase, schedule.skyPhase.phaseNameArabic)
            views.setTextViewText(R.id.prayer_widget_next_title, "الصلاة القادمة: صلاة ${schedule.nextPrayer.nameArabic}")
            views.setTextViewText(R.id.prayer_widget_next_countdown, "متبقي ${schedule.remainingFormatted}")

            views.setTextViewText(R.id.prayer_fajr_time, schedule.fajr.timeFormatted)
            views.setTextViewText(R.id.prayer_dhuhr_time, schedule.dhuhr.timeFormatted)
            views.setTextViewText(R.id.prayer_asr_time, schedule.asr.timeFormatted)
            views.setTextViewText(R.id.prayer_maghrib_time, schedule.maghrib.timeFormatted)
            views.setTextViewText(R.id.prayer_isha_time, schedule.isha.timeFormatted)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updatePrayerWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PrayerTimesAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}
