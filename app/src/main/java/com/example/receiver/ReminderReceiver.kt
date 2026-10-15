package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.ReminderRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_REMINDER_TRIGGER = "com.example.ACTION_REMINDER_TRIGGER"
        const val ACTION_REMINDER_DONE = "com.example.ACTION_REMINDER_DONE"
        const val ACTION_REMINDER_SNOOZE = "com.example.ACTION_REMINDER_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_TITLE) ?: "Reminder"
        val category = intent.getStringExtra(NotificationHelper.EXTRA_REMINDER_CATEGORY) ?: "GENERAL"

        if (reminderId == -1L) return

        val pendingResult = goAsync()
        val database = AppDatabase.getDatabase(context)
        val repository = ReminderRepository(database.reminderDao())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_REMINDER_TRIGGER -> {
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null && reminder.isEnabled) {
                            if (reminder.isWithinActiveHours()) {
                                NotificationHelper.showReminderNotification(
                                    context = context,
                                    reminderId = reminder.id,
                                    title = reminder.title,
                                    description = reminder.description,
                                    category = reminder.category,
                                    soundEnabled = reminder.soundEnabled,
                                    vibrateEnabled = reminder.vibrateEnabled
                                )
                            }
                            // Schedule the next interval occurrence
                            val nextTime = AlarmScheduler.scheduleReminder(context, reminder)
                            repository.updateNextTriggerTime(reminder.id, nextTime)
                        }
                    }

                    ACTION_REMINDER_DONE -> {
                        NotificationHelper.cancelNotification(context, reminderId)
                        repository.logCompletion(reminderId, title, category)
                        val reminder = repository.getReminderById(reminderId)
                        if (reminder != null && reminder.isEnabled) {
                            val nextTime = AlarmScheduler.scheduleReminder(context, reminder)
                            repository.updateNextTriggerTime(reminder.id, nextTime)
                        }
                    }

                    ACTION_REMINDER_SNOOZE -> {
                        NotificationHelper.cancelNotification(context, reminderId)
                        repository.logSnooze(reminderId, title, category)
                        AlarmScheduler.scheduleSnooze(context, reminderId, title, category, snoozeMinutes = 10)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
