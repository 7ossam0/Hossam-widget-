package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        try {
            when (intent.action) {
                PrayerNotificationHelper.ACTION_PRAYER_ALARM -> {
                    val prayerName = intent.getStringExtra(PrayerNotificationHelper.EXTRA_PRAYER_NAME) ?: "الصلاة"
                    val prayerTime = intent.getStringExtra(PrayerNotificationHelper.EXTRA_PRAYER_TIME) ?: ""
                    PrayerNotificationHelper.sendPrayerNotification(context, prayerName, prayerTime)
                    PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
                }
                Intent.ACTION_BOOT_COMPLETED -> {
                    PrayerNotificationHelper.createNotificationChannels(context)
                    PrayerNotificationHelper.scheduleNextPrayerAlarms(context)
                }
            }
        } catch (e: Throwable) {
            Log.e("PrayerAlarmReceiver", "Error receiving prayer alarm broadcast: ${e.message}", e)
        }
    }
}
