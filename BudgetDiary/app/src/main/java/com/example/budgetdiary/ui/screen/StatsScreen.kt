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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
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
    onScrollChanged: (Int) -> Unit,
) {
    val ratio = if (budgetTotal <= 0.0) 0.0 else (spentTotal / budgetTotal).coerceAtMost(1.0)
    val listState = rememberLazyListState()
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset
        }.collect { onScrollChanged(it) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
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
                            .background(scheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio.toFloat())
                                .height(12.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    if (spentTotal > budgetTotal && budgetTotal > 0) {
                                        Color(0xFFE9B7B7)
                                    } else {
                                        Color(0xFFCBE7D5)
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
                                        .background(scheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((amount / maxValue).toFloat())
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color(0xFFDCCFF5))
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
    val scheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val w = size.width
            val h = size.height
            val left = 28f
            val right = w - 8f
            val top = 10f
            val bottom = h - 24f

            drawLine(
                color = scheme.outline.copy(alpha = 0.6f),
                start = Offset(left, bottom),
                end = Offset(right, bottom),
                strokeWidth = 2.5f
            )
            drawLine(
                color = scheme.outline.copy(alpha = 0.6f),
                start = Offset(left, top),
                end = Offset(left, bottom),
                strokeWidth = 2.5f
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
                        color = Color(0xFF8B82D9),
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3f
                    )
                }

                points.forEach {
                    drawCircle(
                        color = Color(0xFF8B82D9),
                        radius = 4f,
                        center = it
                    )
                }
            }
        }

        Text(
            "横轴为日期，纵轴为每日消费金额",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpendingPieChart(spentByLabel: List<Pair<String, Double>>) {
    val scheme = MaterialTheme.colorScheme
    val colors = listOf(
        Color(0xFFDCCFF5),
        Color(0xFFD7E8F5),
        Color(0xFFCFE8D8),
        Color(0xFFF5E2CF),
        Color(0xFFF2D6D9),
        Color(0xFFE7DDF7),
        Color(0xFFE8E5EC),
    )

    val total = spentByLabel.sumOf { it.second }.coerceAtLeast(1.0)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            modifier = Modifier.size(170.dp)
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors[index % colors.size])
                )
                Text(
                    "$label：${money(amount)}（${(amount / total * 100).round2()}%）",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}