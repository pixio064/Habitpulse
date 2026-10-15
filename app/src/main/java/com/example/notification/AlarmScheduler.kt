package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.ReminderItem
import com.example.receiver.ReminderReceiver
import java.util.Calendar

object AlarmScheduler {

    fun calculateNextTrigger(reminder: ReminderItem, fromTimeMillis: Long = System.currentTimeMillis()): Long {
        if (!reminder.isEnabled) return 0L

        val intervalMillis = reminder.intervalMinutes * 60 * 1000L
        val candidate = Calendar.getInstance().apply {
            timeInMillis = fromTimeMillis + intervalMillis
        }

        // Search up to 7 days ahead for a matching active window
        for (dayOffset in 0..7) {
            val checkDayCal = Calendar.getInstance().apply {
                timeInMillis = fromTimeMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dayOfWeek = checkDayCal.get(Calendar.DAY_OF_WEEK)

            if (!reminder.isDayActive(dayOfWeek)) {
                continue
            }

            // Window start & end for this day
            val windowStart = Calendar.getInstance().apply {
                timeInMillis = checkDayCal.timeInMillis
                set(Calendar.HOUR_OF_DAY, reminder.startHour)
                set(Calendar.MINUTE, reminder.startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val windowEnd = Calendar.getInstance().apply {
                timeInMillis = checkDayCal.timeInMillis
                set(Calendar.HOUR_OF_DAY, reminder.endHour)
                set(Calendar.MINUTE, reminder.endMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (dayOffset == 0) {
                // Today
                if (fromTimeMillis < windowStart.timeInMillis) {
                    // Before start time today -> first trigger is start time
                    return windowStart.timeInMillis
                }
                if (candidate.timeInMillis <= windowEnd.timeInMillis) {
                    // Candidate falls cleanly inside today's active window
                    return candidate.timeInMillis
                }
                // Otherwise candidate is past today's end -> will move to tomorrow
            } else {
                // Future active day -> trigger at start time
                return windowStart.timeInMillis
            }
        }

        // Fallback: simple candidate
        return candidate.timeInMillis
    }

    fun scheduleReminder(context: Context, reminder: ReminderItem): Long {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!reminder.isEnabled) {
            cancelReminder(context, reminder.id)
            return 0L
        }

        val nextTriggerTime = calculateNextTrigger(reminder)
        if (nextTriggerTime <= 0L) return 0L

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER_TRIGGER
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminder.id)
            putExtra(NotificationHelper.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(NotificationHelper.EXTRA_REMINDER_CATEGORY, reminder.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }

        return nextTriggerTime
    }

    fun scheduleSnooze(context: Context, reminderId: Long, title: String, category: String, snoozeMinutes: Int = 10) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + snoozeMinutes * 60 * 1000L

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER_TRIGGER
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
            putExtra(NotificationHelper.EXTRA_REMINDER_TITLE, title)
            putExtra(NotificationHelper.EXTRA_REMINDER_CATEGORY, category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (_: Exception) {}
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
