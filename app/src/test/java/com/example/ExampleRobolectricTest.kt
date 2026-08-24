package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.prayer.PrayerTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("بيان", appName)
  }

  @Test
  fun `calculate daily prayer schedule`() {
    val schedule = PrayerTimeCalculator.calculateDailySchedule()
    assertNotNull(schedule)
    assertNotNull(schedule.fajr)
    assertNotNull(schedule.dhuhr)
    assertNotNull(schedule.asr)
    assertNotNull(schedule.maghrib)
    assertNotNull(schedule.isha)
  }

  @Test
  fun `verify database instance`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    assertNotNull(db)
  }

  @Test
  fun `verify font resources load properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val f1 = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.app_ui_font)
    val f2 = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.app_ui_font_bold)
    val f3 = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.quran_hafs)
    assertNotNull(f1)
    assertNotNull(f2)
    assertNotNull(f3)
  }
}

