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

@Composable
fun DayCell(summary: DaySummary, onClick: () -> Unit) {
    val bg = when {
        summary.budget == null -> Color(0xFFF3F4F6)
        summary.isActivityFundNegative -> Color(0xFFFECACA)
        summary.isOverDailyBudget -> Color(0xFFFFEDD5)
        else -> Color(0xFFD1FAE5)
    }

    val valueText = when {
        summary.budget == null -> "--"
        summary.dailyDiff > 0 -> "+${formatCalendarNumber(summary.dailyDiff)}"
        summary.dailyDiff < 0 -> "-${formatCalendarNumber(abs(summary.dailyDiff))}"
        else -> "0"
    }

    val valueColor = when {
        summary.budget == null -> Color(0xFF6B7280)
        summary.dailyDiff > 0 -> Color(0xFF047857)
        summary.dailyDiff < 0 -> Color(0xFFB91C1C)
        else -> Color(0xFF374151)
    }

    Card(
        modifier = Modifier
            .aspectRatio(0.82f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = summary.date.dayOfMonth.toString(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
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