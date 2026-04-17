package com.example.budgetdiary.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.BudgetRange
import com.example.budgetdiary.model.DailyTask
import com.example.budgetdiary.util.formatInput
import com.example.budgetdiary.util.money
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun BudgetSettingsScreen(
    month: YearMonth,
    range: BudgetRange,
    labels: List<String>,
    customLabels: List<String>,
    tasks: List<DailyTask>,
    completedTaskIds: List<String>,
    onSave: (BudgetRange) -> Unit,
    onAddLabel: (String) -> Boolean,
    onRemoveLabel: (String) -> Unit,
    onAddTask: (String, Double) -> Boolean,
    onRemoveTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onScrollChanged: (Int) -> Unit,
) {
    var monthlyBudgetTotal by remember(range) { mutableStateOf(formatInput(range.monthlyBudgetTotal)) }
    var monthlyActivityFund by remember(range) { mutableStateOf(formatInput(range.monthlyActivityFund)) }
    var dailyRangeDelta by remember(range) { mutableStateOf(formatInput(range.dailyRangeDelta)) }
    var newLabel by rememberSaveable { mutableStateOf("") }

    var newTaskTitle by rememberSaveable { mutableStateOf("") }
    var newTaskReward by rememberSaveable { mutableStateOf("") }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset
        }.collect { onScrollChanged(it) }
    }

    val budgetTotalValue = monthlyBudgetTotal.toDoubleOrNull() ?: 0.0
    val activityFundValue = monthlyActivityFund.toDoubleOrNull() ?: 0.0
    val deltaValue = dailyRangeDelta.toDoubleOrNull() ?: 0.0

    val days = month.lengthOfMonth()
    val upperBound = ((budgetTotalValue - activityFundValue).coerceAtLeast(0.0) / days)
    val lowerBound = (upperBound - deltaValue).coerceAtLeast(0.0)

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("每日任务", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("任务名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = newTaskReward,
                        onValueChange = { newTaskReward = it },
                        label = { Text("完成后增加多少当日预算") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    Button(
                        onClick = {
                            val reward = newTaskReward.toDoubleOrNull()
                            if (reward != null && onAddTask(newTaskTitle, reward)) {
                                newTaskTitle = ""
                                newTaskReward = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("添加任务")
                    }

                    if (tasks.isEmpty()) {
                        Text(
                            "本月还没有每日任务",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tasks.forEach { task ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = completedTaskIds.contains(task.id),
                                            onCheckedChange = { onToggleTask(task.id) }
                                        )

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                task.title,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                "完成后 +${money(task.reward)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(onClick = { onRemoveTask(task.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "删除任务",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                    Text(
                        "${month.format(DateTimeFormatter.ofPattern("yyyy年MM月"))} 设置",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = monthlyBudgetTotal,
                        onValueChange = { monthlyBudgetTotal = it },
                        label = { Text("预算总和") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = monthlyActivityFund,
                        onValueChange = { monthlyActivityFund = it },
                        label = { Text("活动资金") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    OutlinedTextField(
                        value = dailyRangeDelta,
                        onValueChange = { dailyRangeDelta = it },
                        label = { Text("上下界差值") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    Text(
                        "自动计算上界：%.2f".format(upperBound),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "自动计算下界：%.2f".format(lowerBound),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        "规则：上界 = (预算总和 - 活动资金) ÷ 当月天数；下界 = max(0, 上界 - 差值)。每天会在这个区间内抽取预算。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        onClick = {
                            val safeBudgetTotal = budgetTotalValue.coerceAtLeast(0.0)
                            val safeActivityFund = activityFundValue.coerceAtLeast(0.0)
                                .coerceAtMost(safeBudgetTotal)
                            val safeDelta = deltaValue.coerceAtLeast(0.0)

                            onSave(
                                BudgetRange(
                                    monthlyBudgetTotal = safeBudgetTotal,
                                    monthlyActivityFund = safeActivityFund,
                                    dailyRangeDelta = safeDelta,
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("保存设置")
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
                    Text("自定义标签", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("新增标签，例如：电影 / 社团 / 学习") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    Button(
                        onClick = {
                            if (onAddLabel(newLabel)) newLabel = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("添加标签")
                    }

                    Text("当前可用标签", style = MaterialTheme.typography.labelLarge)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        labels.forEach { label ->
                            AssistChip(
                                onClick = {},
                                label = { Text(label) },
                                trailingIcon = if (customLabels.contains(label)) {
                                    { Icon(Icons.Default.Delete, contentDescription = null) }
                                } else null
                            )
                        }
                    }

                    if (customLabels.isNotEmpty()) {
                        Text("点击下方按钮删除自定义标签", style = MaterialTheme.typography.bodySmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customLabels.forEach { label ->
                                TextButton(onClick = { onRemoveLabel(label) }) {
                                    Text("删除 $label")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}