package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.ReminderItem
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditReminderDialog(
    reminder: ReminderItem,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (ReminderItem) -> Unit,
    onDelete: (ReminderItem) -> Unit
) {
    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description) }
    var selectedIcon by remember { mutableStateOf(reminder.iconName) }
    var selectedColorHex by remember { mutableStateOf(reminder.colorHex) }
    var intervalMinutes by remember { mutableIntStateOf(reminder.intervalMinutes) }
    var sliderValue by remember { mutableFloatStateOf(reminder.intervalMinutes.toFloat()) }

    var startHour by remember { mutableIntStateOf(reminder.startHour) }
    var endHour by remember { mutableIntStateOf(reminder.endHour) }

    var soundEnabled by remember { mutableStateOf(reminder.soundEnabled) }
    var vibrateEnabled by remember { mutableStateOf(reminder.vibrateEnabled) }
    var daysMask by remember { mutableIntStateOf(reminder.daysOfWeekMask) }

    val presetIntervals = listOf(10, 15, 20, 30, 45, 60, 90, 120)
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val activeColor = IconMapper.parseColor(selectedColorHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 20.dp)
            .testTag("edit_reminder_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconMapper.getIcon(selectedIcon),
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isNew) "New Interval Activity" else "Customize Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!isNew) {
                    IconButton(
                        onClick = { onDelete(reminder) },
                        modifier = Modifier.testTag("delete_reminder_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Name Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Name") },
                    placeholder = { Text("e.g. Drink Water, Study, Stretch") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notification Message") },
                    placeholder = { Text("e.g. Take a sip of water now!") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // INTERVAL SECTION
                Text(
                    text = "Reminder Frequency",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Current Interval Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = activeColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Triggers every: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = when {
                                intervalMinutes < 60 -> "$intervalMinutes minutes"
                                intervalMinutes % 60 == 0 -> "${intervalMinutes / 60} hour${if (intervalMinutes > 60) "s" else ""}"
                                else -> "${intervalMinutes / 60}h ${intervalMinutes % 60}m"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Interval Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetIntervals.forEach { mins ->
                        val selected = intervalMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = {
                                intervalMinutes = mins
                                sliderValue = mins.toFloat()
                            },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeColor.copy(alpha = 0.2f),
                                selectedLabelColor = activeColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slider for fine tuning
                Text(
                    text = "Fine-tune interval (1 to 180 min):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        intervalMinutes = it.toInt()
                    },
                    valueRange = 1f..180f,
                    steps = 178,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = activeColor,
                        activeTrackColor = activeColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ICON PICKER
                Text(
                    text = "Icon Symbol",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconMapper.availableIcons.forEach { (iconKey, label) ->
                        val isSelected = selectedIcon.equals(iconKey, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) activeColor.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) activeColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconMapper.getIcon(iconKey),
                                contentDescription = label,
                                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // COLOR PICKER
                Text(
                    text = "Accent Theme Color",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconMapper.availableColors.forEach { (hex, _) ->
                        val chipColor = IconMapper.parseColor(hex)
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ACTIVE SCHEDULE HOURS
                Text(
                    text = "Active Schedule Window",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notifications will only buzz during this time:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = {
                            val v = it.toIntOrNull() ?: 0
                            if (v in 0..23) startHour = v
                        },
                        label = { Text("Start (0-23h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = endHour.toString(),
                        onValueChange = {
                            val v = it.toIntOrNull() ?: 23
                            if (v in 0..23) endHour = v
                        },
                        label = { Text("End (0-23h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DAYS OF WEEK
                Text(
                    text = "Active Days",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { index, label ->
                        val isDayActive = (daysMask and (1 shl index)) != 0
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDayActive) activeColor
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable {
                                    daysMask = daysMask xor (1 shl index)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.take(1),
                                color = if (isDayActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SOUND & VIBRATION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Play Sound", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Vibrate on Alert", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = vibrateEnabled,
                        onCheckedChange = { vibrateEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.trim().ifEmpty { "My Reminder" }
                    val finalDesc = description.trim().ifEmpty { "Time for $finalTitle!" }
                    val updated = reminder.copy(
                        title = finalTitle,
                        description = finalDesc,
                        iconName = selectedIcon,
                        colorHex = selectedColorHex,
                        intervalMinutes = intervalMinutes.coerceAtLeast(1),
                        startHour = startHour,
                        endHour = endHour,
                        soundEnabled = soundEnabled,
                        vibrateEnabled = vibrateEnabled,
                        daysOfWeekMask = daysMask.coerceAtLeast(1)
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("save_reminder_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor)
            ) {
                Text("Save Interval", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_dialog_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
