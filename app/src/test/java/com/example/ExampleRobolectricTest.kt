package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ReminderItem
import com.example.notification.AlarmScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HabitPulse", appName)
  }

  @Test
  fun `format interval formats correctly`() {
    val item30 = ReminderItem(
      title = "Drink Water",
      description = "Hydrate",
      category = "WATER",
      iconName = "water_drop",
      colorHex = "#0284C7",
      intervalMinutes = 30
    )
    assertEquals("30m", item30.formatInterval())

    val item60 = item30.copy(intervalMinutes = 60)
    assertEquals("1h", item60.formatInterval())

    val item90 = item30.copy(intervalMinutes = 90)
    assertEquals("1h 30m", item90.formatInterval())
  }

  @Test
  fun `alarm scheduler calculates future trigger`() {
    val item = ReminderItem(
      title = "Stretch",
      description = "Stretch now",
      category = "POSTURE",
      iconName = "self_improvement",
      colorHex = "#10B981",
      intervalMinutes = 45,
      isEnabled = true,
      startHour = 0,
      endHour = 23,
      daysOfWeekMask = 127
    )
    val now = 1700000000000L
    val next = AlarmScheduler.calculateNextTrigger(item, fromTimeMillis = now)
    assertTrue(next >= now + 45 * 60 * 1000L)
  }
}
