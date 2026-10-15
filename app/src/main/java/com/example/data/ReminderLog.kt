package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "reminder_logs")
data class ReminderLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reminderId: Long,
    val reminderTitle: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String = "COMPLETED", // "COMPLETED", "SNOOZED", "TRIGGERED"
    val note: String = ""
) {
    fun formattedTime(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
