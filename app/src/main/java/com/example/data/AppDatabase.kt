package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ReminderItem::class, ReminderLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_pulse_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial default reminder activities
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).reminderDao()
                    dao.insertReminders(defaultPresets)
                }
            }
        }

        val defaultPresets = listOf(
            ReminderItem(
                title = "Drink Water",
                description = "Time to hydrate! Drink a glass of fresh water 💧",
                category = "WATER",
                iconName = "water_drop",
                colorHex = "#0284C7",
                intervalMinutes = 30, // 30 mins as requested
                isEnabled = true,
                startHour = 8,
                startMinute = 0,
                endHour = 22,
                endMinute = 0,
                soundEnabled = true,
                vibrateEnabled = true,
                daysOfWeekMask = 127
            ),
            ReminderItem(
                title = "Stop Scrolling & Eye Rest",
                description = "20-20-20 rule! Look 20ft away and take a pause 👁️",
                category = "SCREEN_BREAK",
                iconName = "visibility",
                colorHex = "#8B5CF6",
                intervalMinutes = 20, // 20 mins
                isEnabled = true,
                startHour = 9,
                startMinute = 0,
                endHour = 21,
                endMinute = 0,
                soundEnabled = true,
                vibrateEnabled = true,
                daysOfWeekMask = 127
            ),
            ReminderItem(
                title = "Stretch & Posture",
                description = "Roll your shoulders back, stand up, and stretch 🧘",
                category = "POSTURE",
                iconName = "self_improvement",
                colorHex = "#10B981",
                intervalMinutes = 45, // 45 mins
                isEnabled = true,
                startHour = 9,
                startMinute = 0,
                endHour = 20,
                endMinute = 0,
                soundEnabled = true,
                vibrateEnabled = true,
                daysOfWeekMask = 127
            ),
            ReminderItem(
                title = "Study & Focus Sprint",
                description = "Pomodoro check-in! Keep the focus flow or take 5m 📚",
                category = "STUDY",
                iconName = "school",
                colorHex = "#F59E0B",
                intervalMinutes = 50, // 50 mins
                isEnabled = false,
                startHour = 10,
                startMinute = 0,
                endHour = 22,
                endMinute = 0,
                soundEnabled = true,
                vibrateEnabled = true,
                daysOfWeekMask = 127
            ),
            ReminderItem(
                title = "Quick Walk & Movement",
                description = "Stand up, take 100 steps, or take a quick lap 🚶",
                category = "WALK",
                iconName = "directions_walk",
                colorHex = "#EC4899",
                intervalMinutes = 60, // 60 mins
                isEnabled = false,
                startHour = 9,
                startMinute = 0,
                endHour = 19,
                endMinute = 0,
                soundEnabled = true,
                vibrateEnabled = true,
                daysOfWeekMask = 127
            )
        )
    }
}
