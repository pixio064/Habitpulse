package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.ReminderRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            val database = AppDatabase.getDatabase(context)
            val repository = ReminderRepository(database.reminderDao())

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val activeReminders = repository.getActiveReminders()
                    for (reminder in activeReminders) {
                        val nextTime = AlarmScheduler.scheduleReminder(context, reminder)
                        repository.updateNextTriggerTime(reminder.id, nextTime)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
