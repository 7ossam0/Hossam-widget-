package com.example.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.audio.AudioStorageManager
import com.example.data.prayer.CalculationMethod
import com.example.data.prayer.PrayerTimeCalculator
import java.io.File
import java.util.Calendar

object PrayerNotificationHelper {
    const val CHANNEL_PRAYER_ID = "channel_prayer_times_v2"
    const val CHANNEL_AZKAR_ID = "channel_azkar_reminders_v2"
    const val CHANNEL_TASBEEH_ID = "channel_tasbeeh_v2"

    const val ACTION_PRAYER_ALARM = "com.example.ACTION_PRAYER_ALARM"
    const val ACTION_PRE_PRAYER_ALARM = "com.example.ACTION_PRE_PRAYER_ALARM"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME = "extra_prayer_time"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // 1. Prayer Channel (High importance, Sound & Vibration)
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                "مواقيت الصلاة والأذان 🕌",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات دخول وقت الصلاة والأذان الشريف بصوت واضح"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setSound(defaultSoundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // 2. Azkar & Dua Channel
            val azkarChannel = NotificationChannel(
                CHANNEL_AZKAR_ID,
                "الأذكار والأدعية اليومية 📿",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات أذكار الصباح والمساء والأدعية المختارة"
                enableLights(true)
                enableVibration(true)
            }

            // 3. Tasbeeh Channel
            val tasbeehChannel = NotificationChannel(
                CHANNEL_TASBEEH_ID,
                "تذكيرات المسبحة والورد اليومي ✨",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "تذكيرات خفيفة بالاستغفار والتسبيح"
            }

            notificationManager.createNotificationChannels(listOf(prayerChannel, azkarChannel, tasbeehChannel))
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun sendTestNotification(context: Context, title: String = "حان الآن موعد الأذان 🕌", body: String = "صلاة الظهر - الله أكبر، حي على الصلاة") {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check active adhan audio
        val selectedAdhanId = AudioStorageManager.getSelectedAdhanId(context)
        val audioList = AudioStorageManager.getAudioList(context)
        val activeAudio = audioList.find { it.id == selectedAdhanId } ?: audioList.firstOrNull()

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\nالصوت المخصص: ${activeAudio?.title ?: "الأذان الافتراضي"}\n✨ تقبل الله منا ومنكم صالح الأعمال"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(9999, notification)

            // Also play sound preview if test is initiated
            activeAudio?.let {
                AudioStorageManager.playAudio(context, it)
            }
        } catch (e: SecurityException) {
            Log.e("PrayerNotificationHelper", "Notification permission denied", e)
        } catch (e: Exception) {
            Log.e("PrayerNotificationHelper", "Error posting notification", e)
        }
    }

    fun sendPrayerNotification(context: Context, prayerName: String, prayerTime: String) {
        if (!AudioStorageManager.isPrayerNotificationsEnabled(context)) return

        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1002, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val selectedAdhanId = AudioStorageManager.getSelectedAdhanId(context)
        val audioList = AudioStorageManager.getAudioList(context)
        val activeAudio = audioList.find { it.id == selectedAdhanId } ?: audioList.firstOrNull()

        val title = "🕌 حان الآن وقت صلاة $prayerName"
        val body = "الساعة: $prayerTime - الله أكبر، حي على الصلاة، حي على الفلاح."

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n${activeAudio?.title ?: ""}\nقال رسول الله ﷺ: «الصلاة على وقتها»"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(prayerName.hashCode(), notification)

            // Play the active Adhan sound
            activeAudio?.let {
                AudioStorageManager.playAudio(context, it)
            }
        } catch (e: SecurityException) {
            Log.e("PrayerNotificationHelper", "Notification permission denied", e)
        } catch (e: Exception) {
            Log.e("PrayerNotificationHelper", "Error posting prayer notification", e)
        }
    }

    fun scheduleNextPrayerAlarms(context: Context) {
        try {
            val schedule = PrayerTimeCalculator.calculateDailySchedule()
            val nextPrayer = schedule.nextPrayer
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, nextPrayer.hour)
                set(Calendar.MINUTE, nextPrayer.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If time is in the past, add 1 day
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
                putExtra(EXTRA_PRAYER_NAME, nextPrayer.nameArabic)
                putExtra(EXTRA_PRAYER_TIME, nextPrayer.timeFormatted)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (se: SecurityException) {
                Log.w("PrayerNotificationHelper", "Cannot set exact alarm, falling back to inexact alarm", se)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            }

            Log.d("PrayerNotificationHelper", "Scheduled alarm for ${nextPrayer.nameArabic} at ${calendar.time}")
        } catch (e: Exception) {
            Log.e("PrayerNotificationHelper", "Error scheduling prayer alarm", e)
        }
    }
}

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
    }
}
