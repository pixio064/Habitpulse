package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "WATER", "POSTURE", "SCREEN_BREAK", "STUDY", "WALK", "BREATH", "CUSTOM"
    val iconName: String, // "water_drop", "self_improvement", "visibility", "school", "directions_walk", "air", "alarm"
    val colorHex: String, // "#0284C7", etc.
    val intervalMinutes: Int, // e.g. 30 (or any customized value: 15, 20, 30, 45, 60, etc.)
    val isEnabled: Boolean = true,
    val startHour: Int = 8, // 08:00
    val startMinute: Int = 0,
    val endHour: Int = 22, // 22:00
    val endMinute: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val nextTriggerTimeMillis: Long = 0L,
    val completedCountToday: Int = 0,
    val totalCompletedCount: Int = 0,
    val streakDays: Int = 0,
    val lastCompletedDate: String = "", // "YYYY-MM-DD"
    val daysOfWeekMask: Int = 127 // 7 bits: bit 0 = Sunday (or Mon-Sun)
) {
    fun formatInterval(): String {
        return when {
            intervalMinutes < 60 -> "${intervalMinutes}m"
            intervalMinutes % 60 == 0 -> "${intervalMinutes / 60}h"
            else -> "${intervalMinutes / 60}h ${intervalMinutes % 60}m"
        }
    }

    fun formatActiveHours(): String {
        fun formatTime(h: Int, m: Int): String {
            val period = if (h >= 12) "PM" else "AM"
            val hour12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            return String.format(Locale.US, "%d:%02d %s", hour12, m, period)
        }
        return "${formatTime(startHour, startMinute)} - ${formatTime(endHour, endMinute)}"
    }

    fun isDayActive(calendarDayOfWeek: Int): Boolean {
        // Calendar.SUNDAY = 1, MONDAY = 2, ... SATURDAY = 7
        val bitIndex = calendarDayOfWeek - 1
        return (daysOfWeekMask and (1 shl bitIndex)) != 0
    }

    fun isWithinActiveHours(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!isDayActive(calendar.get(Calendar.DAY_OF_WEEK))) {
            return false
        }
        val currentMinutesOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinutes = startHour * 60 + startMinute
        val endMinutes = endHour * 60 + endMinute

        return if (endMinutes >= startMinutes) {
            currentMinutesOfDay in startMinutes..endMinutes
        } else {
            // Overnights (e.g. 20:00 to 02:00)
            currentMinutesOfDay >= startMinutes || currentMinutesOfDay <= endMinutes
        }
    }
}
