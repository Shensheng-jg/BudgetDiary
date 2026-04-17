package com.example.budgetdiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("今日概览", style = MaterialTheme.typography.titleMedium)

            if (!hasDrawnToday) {
                Text("今天还没有抽取预算")
                Button(onClick = onDrawToday) {
                    Text("抽取今日预算")
                }
            } else {
                Text("今日抽取预算：${money(summary.budget?.total ?: 0.0)}")
                Text("今日消费：${money(summary.spent)}")
                Text(
                    text = if (summary.dailyDiff >= 0) {
                        "今日结余：${money(summary.dailyDiff)}"
                    } else {
                        "今日超支：${money(-summary.dailyDiff)}"
                    },
                    color = if (summary.dailyDiff >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                )
                Text("当前活动资金：${money(summary.activityBalanceAfter)}")
            }
        }
    }
}