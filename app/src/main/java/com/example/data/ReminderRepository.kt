package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<ReminderItem>> = reminderDao.getAllReminders()
    val recentLogs: Flow<List<ReminderLog>> = reminderDao.getRecentLogs()

    fun getTodayLogs(): Flow<List<ReminderLog>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return reminderDao.getTodayLogs(cal.timeInMillis)
    }

    suspend fun getActiveReminders(): List<ReminderItem> = reminderDao.getActiveReminders()

    suspend fun getReminderById(id: Long): ReminderItem? = reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: ReminderItem): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderItem) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderItem) = reminderDao.deleteReminder(reminder)

    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)

    suspend fun updateEnabled(id: Long, isEnabled: Boolean) = reminderDao.updateEnabled(id, isEnabled)

    suspend fun updateNextTriggerTime(id: Long, nextTime: Long) = reminderDao.updateNextTriggerTime(id, nextTime)

    suspend fun logCompletion(reminderId: Long, reminderTitle: String, category: String, note: String = "") {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        reminderDao.incrementCompletion(reminderId, todayStr)
        reminderDao.insertLog(
            ReminderLog(
                reminderId = reminderId,
                reminderTitle = reminderTitle,
                category = category,
                actionType = "COMPLETED",
                note = note
            )
        )
    }

    suspend fun logSnooze(reminderId: Long, reminderTitle: String, category: String) {
        reminderDao.insertLog(
            ReminderLog(
                reminderId = reminderId,
                reminderTitle = reminderTitle,
                category = category,
                actionType = "SNOOZED",
                note = "Snoozed for 10 minutes"
            )
        )
    }

    suspend fun resetDailyCountsIfNewDay() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        reminderDao.resetDailyCountsIfNotToday(todayStr)
    }

    suspend fun clearLogs() = reminderDao.clearAllLogs()

    suspend fun restoreDefaultPresets() {
        reminderDao.insertReminders(AppDatabase.defaultPresets)
    }
}
