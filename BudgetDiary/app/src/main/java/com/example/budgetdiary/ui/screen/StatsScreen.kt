package com.example.budgetdiary.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.DaySummary
import com.example.budgetdiary.ui.components.EmptyHint
import com.example.budgetdiary.util.money
import com.example.budgetdiary.util.round2
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatsScreen(
    month: YearMonth,
    budgetTotal: Double,
    spentTotal: Double,
    overDays: Int,
    spentByLabel: List<Pair<String, Double>>,
    topDay: DaySummary?,
    activityFundStart: Double,
    activityFundEnd: Double,
    summaries: List<DaySummary>,
) {
    val ratio = if (budgetTotal <= 0.0) 0.0 else (spentTotal / budgetTotal).coerceAtMost(1.0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "${month.format(DateTimeFormatter.ofPattern("yyyy年MM月"))} 统计概览",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("总预算：${money(budgetTotal)}")
                    Text("总消费：${money(spentTotal)}")
                    Text("预算使用率：${(ratio * 100).round2()}%")
                    Text("月初活动资金：${money(activityFundStart)}")
                    Text("月末活动资金：${money(activityFundEnd)}")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio.toFloat())
                                .height(12.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (spentTotal > budgetTotal && budgetTotal > 0) {
                                        Color(0xFFEF4444)
                                    } else {
                                        Color(0xFF10B981)
                                    }
                                )
                        )
                    }

                    Text("超支天数：$overDays 天")
                    topDay?.let {
                        Text("消费最高的一天：${it.date}，共 ${money(it.spent)}")
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("每日支出折线图", style = MaterialTheme.typography.titleMedium)
                    DailyExpenseLineChart(summaries = summaries)
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("标签占比饼状图", style = MaterialTheme.typography.titleMedium)
                    if (spentByLabel.isEmpty()) {
                        EmptyHint("本月暂无消费数据")
                    } else {
                        SpendingPieChart(spentByLabel = spentByLabel)
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("支出类目排行", style = MaterialTheme.typography.titleMedium)
                    if (spentByLabel.isEmpty()) {
                        EmptyHint("本月暂无消费数据")
                    } else {
                        val maxValue = spentByLabel.maxOf { it.second }.coerceAtLeast(1.0)
                        spentByLabel.forEachIndexed { index, (label, amount) ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${index + 1}. $label")
                                    Text(money(amount), fontWeight = FontWeight.SemiBold)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFFE5E7EB))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((amount / maxValue).toFloat())
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color(0xFF6366F1))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyExpenseLineChart(summaries: List<DaySummary>) {
    val values = summaries.map { it.spent }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val w = size.width
            val h = size.height
            val left = 30f
            val right = w - 10f
            val top = 10f
            val bottom = h - 30f

            drawLine(
                color = Color.LightGray,
                start = Offset(left, bottom),
                end = Offset(right, bottom),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.LightGray,
                start = Offset(left, top),
                end = Offset(left, bottom),
                strokeWidth = 3f
            )

            if (values.size > 1) {
                val stepX = (right - left) / (values.size - 1)
                val points = values.mapIndexed { index, value ->
                    Offset(
                        x = left + index * stepX,
                        y = bottom - ((value / maxValue).toFloat() * (bottom - top))
                    )
                }

                for (i in 0 until points.lastIndex) {
                    drawLine(
                        color = Color(0xFF2563EB),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f
                    )
                }

                points.forEach {
                    drawCircle(
                        color = Color(0xFF2563EB),
                        radius = 5f,
                        center = it
                    )
                }
            }
        }

        Text(
            "横轴为日期，纵轴为每日消费金额",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpendingPieChart(spentByLabel: List<Pair<String, Double>>) {
    val colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFF06B6D4),
        Color(0xFF8B5CF6),
        Color(0xFF84CC16),
    )

    val total = spentByLabel.sumOf { it.second }.coerceAtLeast(1.0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(
            modifier = Modifier
                .size(220.dp)
        ) {
            var startAngle = -90f
            spentByLabel.forEachIndexed { index, (_, amount) ->
                val sweep = (amount / total * 360.0).toFloat()
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height)
                )
                startAngle += sweep
            }
        }

        spentByLabel.forEachIndexed { index, (label, amount) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )
                Text(
                    "$label：${money(amount)}（${(amount / total * 100).round2()}%）",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}