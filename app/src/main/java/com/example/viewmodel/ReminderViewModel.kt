package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ReminderItem
import com.example.data.ReminderLog
import com.example.data.ReminderRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReminderRepository
    val reminders: StateFlow<List<ReminderItem>>
    val todayLogs: StateFlow<List<ReminderLog>>
    val recentLogs: StateFlow<List<ReminderLog>>

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _editingReminder = MutableStateFlow<ReminderItem?>(null)
    val editingReminder: StateFlow<ReminderItem?> = _editingReminder.asStateFlow()

    private val _isCreatingNew = MutableStateFlow(false)
    val isCreatingNew: StateFlow<Boolean> = _isCreatingNew.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ReminderRepository(db.reminderDao())

        reminders = repository.allReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayLogs = repository.getTodayLogs().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentLogs = repository.recentLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Reset daily counts if new day
        viewModelScope.launch {
            repository.resetDailyCountsIfNewDay()
            // Make sure active reminders are scheduled
            syncAlarmsWithDatabase(application)
        }

        // Clock ticker to smoothly update relative countdowns every 5 seconds
        viewModelScope.launch {
            while (isActive) {
                delay(5000L)
                _currentTimeMillis.value = System.currentTimeMillis()
            }
        }
    }

    val totalCompletedToday: StateFlow<Int> = reminders.combine(todayLogs) { reminderList, logs ->
        logs.count { it.actionType == "COMPLETED" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun openCreateDialog() {
        _editingReminder.value = ReminderItem(
            title = "",
            description = "",
            category = "CUSTOM",
            iconName = "notifications",
            colorHex = "#6366F1",
            intervalMinutes = 30,
            isEnabled = true,
            startHour = 9,
            startMinute = 0,
            endHour = 21,
            endMinute = 0,
            soundEnabled = true,
            vibrateEnabled = true,
            daysOfWeekMask = 127
        )
        _isCreatingNew.value = true
    }

    fun openEditDialog(reminder: ReminderItem) {
        _editingReminder.value = reminder
        _isCreatingNew.value = false
    }

    fun closeDialog() {
        _editingReminder.value = null
        _isCreatingNew.value = false
    }

    fun toggleReminder(context: Context, reminder: ReminderItem) {
        val newStatus = !reminder.isEnabled
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = newStatus)
            if (newStatus) {
                val nextTime = AlarmScheduler.scheduleReminder(context, updated)
                repository.updateReminder(updated.copy(nextTriggerTimeMillis = nextTime))
                showMessage("Enabled ${reminder.title} (every ${reminder.formatInterval()})")
            } else {
                AlarmScheduler.cancelReminder(context, reminder.id)
                NotificationHelper.cancelNotification(context, reminder.id)
                repository.updateReminder(updated.copy(nextTriggerTimeMillis = 0L))
                showMessage("Paused ${reminder.title}")
            }
        }
    }

    fun saveReminder(context: Context, reminder: ReminderItem) {
        viewModelScope.launch {
            if (_isCreatingNew.value) {
                val id = repository.insertReminder(reminder)
                val inserted = reminder.copy(id = id)
                val nextTime = AlarmScheduler.scheduleReminder(context, inserted)
                repository.updateNextTriggerTime(id, nextTime)
                showMessage("Created \"${reminder.title}\" interval")
            } else {
                repository.updateReminder(reminder)
                if (reminder.isEnabled) {
                    val nextTime = AlarmScheduler.scheduleReminder(context, reminder)
                    repository.updateNextTriggerTime(reminder.id, nextTime)
                } else {
                    AlarmScheduler.cancelReminder(context, reminder.id)
                }
                showMessage("Updated \"${reminder.title}\"")
            }
            closeDialog()
        }
    }

    fun deleteReminder(context: Context, reminder: ReminderItem) {
        viewModelScope.launch {
            AlarmScheduler.cancelReminder(context, reminder.id)
            NotificationHelper.cancelNotification(context, reminder.id)
            repository.deleteReminder(reminder)
            showMessage("Deleted \"${reminder.title}\"")
            closeDialog()
        }
    }

    fun logQuickDone(context: Context, reminder: ReminderItem) {
        viewModelScope.launch {
            repository.logCompletion(reminder.id, reminder.title, reminder.category)
            // Reschedule next interval starting from right now
            if (reminder.isEnabled) {
                val nextTime = AlarmScheduler.scheduleReminder(context, reminder)
                repository.updateNextTriggerTime(reminder.id, nextTime)
            }
            showMessage("Great job! Logged \"${reminder.title}\" (+1)")
        }
    }

    fun sendTestNotification(context: Context, reminder: ReminderItem) {
        NotificationHelper.showTestNotification(
            context = context,
            title = reminder.title,
            description = reminder.description
        )
        showMessage("Sent test notification for \"${reminder.title}\"")
    }

    fun restoreDefaults(context: Context) {
        viewModelScope.launch {
            repository.restoreDefaultPresets()
            syncAlarmsWithDatabase(context)
            showMessage("Restored default habit reminders")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            showMessage("Cleared activity history")
        }
    }

    private suspend fun syncAlarmsWithDatabase(context: Context) {
        val active = repository.getActiveReminders()
        for (item in active) {
            val nextTime = AlarmScheduler.scheduleReminder(context, item)
            repository.updateNextTriggerTime(item.id, nextTime)
        }
    }
}
