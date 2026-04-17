package com.example.budgetdiary.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.DaySummary
import kotlin.math.abs

private val CalendarGray = Color(0xFFF1F1F3)
private val CalendarGreen = Color(0xFFDDF3E4)
private val CalendarOrange = Color(0xFFF8E8D7)
private val CalendarRed = Color(0xFFF6DCDC)

@Composable
fun DayCell(summary: DaySummary, onClick: () -> Unit) {
    val bg = when {
        summary.budget == null -> CalendarGray
        summary.isActivityFundNegative -> CalendarRed
        summary.isOverDailyBudget -> CalendarOrange
        else -> CalendarGreen
    }

    val valueText = when {
        summary.budget == null -> "--"
        summary.dailyDiff > 0 -> "+${formatCalendarNumber(summary.dailyDiff)}"
        summary.dailyDiff < 0 -> "-${formatCalendarNumber(abs(summary.dailyDiff))}"
        else -> "0"
    }

    val valueColor = when {
        summary.budget == null -> Color(0xFF8A8791)
        summary.dailyDiff > 0 -> Color(0xFF4E8A63)
        summary.dailyDiff < 0 -> Color(0xFFB06A5F)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .aspectRatio(0.84f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = summary.date.dayOfMonth.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatCalendarNumber(value: Double): String {
    val text = String.format("%.1f", value)
    return if (text.endsWith(".0")) text.dropLast(2) else text
}