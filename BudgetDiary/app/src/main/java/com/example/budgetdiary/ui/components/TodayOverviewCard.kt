package com.example.budgetdiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.DaySummary
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.util.money

@Composable
fun TodayOverviewCard(
    summary: DaySummary,
    todayRecords: List<ExpenseRecord>,
    hasDrawnToday: Boolean,
    onDrawToday: () -> Unit,
    onDeleteRecord: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "今日概览",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface
            )

            if (!hasDrawnToday) {
                Text(
                    "今天还没有抽取预算",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                Button(
                    onClick = onDrawToday,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.primary,
                        contentColor = scheme.onPrimary
                    )
                ) {
                    Text("抽取今日预算")
                }
            } else {
                Text("今日基础预算：${money(summary.budget?.total ?: 0.0)}", color = scheme.onSurface)
                Text("今日任务加成：${money(summary.completedTaskReward)}", color = scheme.onSurface)
                Text("今日可用预算：${money(summary.totalAvailableBudget)}", color = scheme.onSurface)
                Text("今日消费：${money(summary.spent)}", color = scheme.onSurface)

                Text(
                    text = if (summary.dailyDiff >= 0) {
                        "今日结余：${money(summary.dailyDiff)}"
                    } else {
                        "今日超支：${money(-summary.dailyDiff)}"
                    },
                    color = if (summary.dailyDiff >= 0) scheme.tertiary else scheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "当前活动资金：${money(summary.activityBalanceAfter)}",
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}