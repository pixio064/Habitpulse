package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isEnabled DESC, id ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getActiveReminders(): List<ReminderItem>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderItem?

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderByIdFlow(id: Long): Flow<ReminderItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderItem>)

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE reminders SET nextTriggerTimeMillis = :nextTime WHERE id = :id")
    suspend fun updateNextTriggerTime(id: Long, nextTime: Long)

    @Query("UPDATE reminders SET completedCountToday = completedCountToday + 1, totalCompletedCount = totalCompletedCount + 1, lastCompletedDate = :todayDate WHERE id = :id")
    suspend fun incrementCompletion(id: Long, todayDate: String)

    @Query("UPDATE reminders SET completedCountToday = 0 WHERE lastCompletedDate != :todayDate")
    suspend fun resetDailyCountsIfNotToday(todayDate: String)

    // Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLog): Long

    @Query("SELECT * FROM reminder_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<ReminderLog>>

    @Query("SELECT * FROM reminder_logs WHERE timestamp >= :startOfDayTimestamp ORDER BY timestamp DESC")
    fun getTodayLogs(startOfDayTimestamp: Long): Flow<List<ReminderLog>>

    @Query("DELETE FROM reminder_logs")
    suspend fun clearAllLogs()
}
