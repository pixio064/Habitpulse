package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun getIcon(name: String): ImageVector {
        return when (name.lowercase()) {
            "water_drop" -> Icons.Default.WaterDrop
            "self_improvement" -> Icons.Default.SelfImprovement
            "visibility", "eye" -> Icons.Default.Visibility
            "visibility_off", "screen_break" -> Icons.Default.VisibilityOff
            "school" -> Icons.Default.School
            "book", "study" -> Icons.Default.MenuBook
            "directions_walk", "walk" -> Icons.Default.DirectionsWalk
            "directions_run", "run" -> Icons.Default.DirectionsRun
            "air", "breath" -> Icons.Default.Air
            "fitness_center", "gym" -> Icons.Default.FitnessCenter
            "coffee" -> Icons.Default.Coffee
            "timer" -> Icons.Default.Timer
            else -> Icons.Default.Notifications
        }
    }

    val availableIcons = listOf(
        "water_drop" to "Water",
        "visibility" to "Eye Rest",
        "self_improvement" to "Stretch",
        "school" to "Study",
        "directions_walk" to "Walk",
        "air" to "Breathing",
        "fitness_center" to "Fitness",
        "coffee" to "Break",
        "timer" to "Timer",
        "notifications" to "Alert"
    )

    val availableColors = listOf(
        "#0284C7" to "Blue",
        "#10B981" to "Green",
        "#8B5CF6" to "Purple",
        "#F59E0B" to "Amber",
        "#EC4899" to "Pink",
        "#06B6D4" to "Cyan",
        "#6366F1" to "Indigo",
        "#EF4444" to "Red"
    )

    fun parseColor(hex: String, fallback: Color = Color(0xFF6366F1)): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            fallback
        }
    }
}
