package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.ReminderItem
import com.example.ui.components.EditReminderDialog
import com.example.ui.components.HistoryScreen
import com.example.ui.components.ReminderCard
import com.example.ui.components.StatsScreen
import com.example.ui.components.SettingsScreen
import com.example.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ReminderViewModel) {
    val context = LocalContext.current
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val totalCompletedToday by viewModel.totalCompletedToday.collectAsStateWithLifecycle()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val editingReminder by viewModel.editingReminder.collectAsStateWithLifecycle()
    val isCreatingNew by viewModel.isCreatingNew.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var filterMode by remember { mutableStateOf(0) } // 0: All, 1: Active only, 2: Paused only

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_logo),
                                    contentDescription = "HabitPulse",
                                    modifier = Modifier.size(28.dp).clip(CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HabitPulse",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            val activeCount = reminders.count { it.isEnabled }
                            Text(
                                text = "$activeCount active intervals",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick test notification button in top bar
                    IconButton(
                        onClick = {
                            val activeItem = reminders.firstOrNull { it.isEnabled } ?: reminders.firstOrNull()
                            if (activeItem != null) {
                                viewModel.sendTestNotification(context, activeItem)
                            } else {
                                viewModel.showMessage("No reminder available to test")
                            }
                        },
                        modifier = Modifier.testTag("top_test_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Quick Test Alert",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Reminders") },
                    label = { Text("Intervals") },
                    modifier = Modifier.testTag("tab_reminders")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
                    label = { Text("Progress") },
                    modifier = Modifier.testTag("tab_stats")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    modifier = Modifier.testTag("tab_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openCreateDialog() },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    text = { Text("New Interval") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_reminder_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    // Reminders List Screen
                    RemindersListTab(
                        reminders = reminders,
                        currentTimeMillis = currentTimeMillis,
                        filterMode = filterMode,
                        onFilterChanged = { filterMode = it },
                        onToggle = { viewModel.toggleReminder(context, it) },
                        onEdit = { viewModel.openEditDialog(it) },
                        onQuickLog = { viewModel.logQuickDone(context, it) },
                        onTest = { viewModel.sendTestNotification(context, it) },
                        onCreateNew = { viewModel.openCreateDialog() }
                    )
                }
                1 -> {
                    StatsScreen(
                        reminders = reminders,
                        todayLogs = todayLogs,
                        totalCompletedToday = totalCompletedToday
                    )
                }
                2 -> {
                    HistoryScreen(
                        logs = recentLogs,
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
                3 -> {
                    SettingsScreen(
                        onRestoreDefaults = { viewModel.restoreDefaults(context) }
                    )
                }
            }

            // Edit / Create Dialog Overlay
            editingReminder?.let { currentEditing ->
                EditReminderDialog(
                    reminder = currentEditing,
                    isNew = isCreatingNew,
                    onDismiss = { viewModel.closeDialog() },
                    onSave = { updated -> viewModel.saveReminder(context, updated) },
                    onDelete = { toDelete -> viewModel.deleteReminder(context, toDelete) }
                )
            }
        }
    }
}

@Composable
fun RemindersListTab(
    reminders: List<ReminderItem>,
    currentTimeMillis: Long,
    filterMode: Int,
    onFilterChanged: (Int) -> Unit,
    onToggle: (ReminderItem) -> Unit,
    onEdit: (ReminderItem) -> Unit,
    onQuickLog: (ReminderItem) -> Unit,
    onTest: (ReminderItem) -> Unit,
    onCreateNew: () -> Unit
) {
    val filteredReminders = when (filterMode) {
        1 -> reminders.filter { it.isEnabled }
        2 -> reminders.filter { !it.isEnabled }
        else -> reminders
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reminders_list"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterMode == 0,
                    onClick = { onFilterChanged(0) },
                    label = { Text("All (${reminders.size})") }
                )
                FilterChip(
                    selected = filterMode == 1,
                    onClick = { onFilterChanged(1) },
                    label = { Text("Active (${reminders.count { it.isEnabled }})") }
                )
                FilterChip(
                    selected = filterMode == 2,
                    onClick = { onFilterChanged(2) },
                    label = { Text("Paused (${reminders.count { !it.isEnabled }})") }
                )
            }
        }

        if (filteredReminders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No intervals in this view",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap '+ New Interval' below to add a reminder for water, study, screen breaks, or anything else!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(
                items = filteredReminders,
                key = { it.id }
            ) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    currentTimeMillis = currentTimeMillis,
                    onToggle = { onToggle(reminder) },
                    onEdit = { onEdit(reminder) },
                    onQuickLog = { onQuickLog(reminder) },
                    onTestNotification = { onTest(reminder) }
                )
            }
        }
    }
}
