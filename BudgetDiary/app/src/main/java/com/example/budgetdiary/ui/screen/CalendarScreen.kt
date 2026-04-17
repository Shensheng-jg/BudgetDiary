package com.example.budgetdiary.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.CalendarCell
import com.example.budgetdiary.model.DaySummary
import com.example.budgetdiary.ui.components.DayCell
import com.example.budgetdiary.ui.components.LegendChip
import com.example.budgetdiary.util.money
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val CalendarGray = Color(0xFFF1F1F3)
private val CalendarGreen = Color(0xFFDDF3E4)
private val CalendarOrange = Color(0xFFF8E8D7)
private val CalendarRed = Color(0xFFF6DCDC)

@Composable
fun CalendarScreen(
    month: YearMonth,
    summaries: List<DaySummary>,
    onDelete: (LocalDate, String) -> Unit,
    onScrollChanged: (Int) -> Unit,
) {
    var selected by remember { mutableStateOf<DaySummary?>(null) }
    val firstDay = month.atDay(1)
    val blanks = firstDay.dayOfWeek.value % 7
    val calendarItems = MutableList<CalendarCell>(blanks) { CalendarCell.Empty }.apply {
        summaries.forEach { add(CalendarCell.Day(it)) }
    }

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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegendChip("未生成预算", CalendarGray)
                LegendChip("当日有结余", CalendarGreen)
                LegendChip("超日预算", CalendarOrange)
                LegendChip("活动资金为负", CalendarRed)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day,
                            color = scheme.onSurface,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(700.dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                gridItems(calendarItems) { cell ->
                    when (cell) {
                        CalendarCell.Empty -> Box(modifier = Modifier.aspectRatio(0.95f))
                        is CalendarCell.Day -> DayCell(
                            summary = cell.summary,
                            onClick = { selected = cell.summary }
                        )
                    }
                }
            }
        }
    }

    selected?.let { summary ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text("关闭") }
            },
            title = { Text(summary.date.toString()) },
            text = {
                androidx.compose.foundation.layout.Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("当日饮食预算：${money(summary.budget?.total ?: 0.0)}")
                    Text("当日消费：${money(summary.spent)}")
                    Text(
                        text = if (summary.dailyDiff >= 0) {
                            "当日结余：${money(summary.dailyDiff)}"
                        } else {
                            "当日超支：${money(-summary.dailyDiff)}"
                        },
                        color = if (summary.dailyDiff >= 0) Color(0xFF4E8A63) else Color(0xFFB06A5F)
                    )
                    Text("活动资金（当天开始）：${money(summary.activityBalanceBefore)}")
                    Text("活动资金（当天结束）：${money(summary.activityBalanceAfter)}")
                    HorizontalDivider()
                    if (summary.expenses.isEmpty()) {
                        Text("当天暂无消费记录")
                    } else {
                        summary.expenses.forEach { record ->
                            androidx.compose.foundation.layout.Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(record.label)
                                    Text(money(record.amount))
                                }
                                Text(
                                    record.dateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (record.note.isNotBlank()) {
                                    Text(record.note, style = MaterialTheme.typography.bodySmall)
                                }
                                TextButton(onClick = { onDelete(summary.date, record.id) }) {
                                    Text("删除这条")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        )
    }
}