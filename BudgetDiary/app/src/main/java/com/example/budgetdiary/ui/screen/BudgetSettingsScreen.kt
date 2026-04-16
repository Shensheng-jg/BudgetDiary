package com.example.budgetdiary.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.BudgetRange
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.ui.components.RecordCard
import com.example.budgetdiary.util.formatInput
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun BudgetSettingsScreen(
    month: YearMonth,
    range: BudgetRange,
    labels: List<String>,
    customLabels: List<String>,
    allRecords: List<ExpenseRecord>,
    onSave: (BudgetRange) -> Unit,
    onAddLabel: (String) -> Boolean,
    onRemoveLabel: (String) -> Unit,
    onDeleteRecord: (LocalDate, String) -> Unit,
) {
    var foodMin by remember(range) { mutableStateOf(formatInput(range.foodMin)) }
    var foodMax by remember(range) { mutableStateOf(formatInput(range.foodMax)) }
    var monthlyActivityFund by remember(range) { mutableStateOf(formatInput(range.monthlyActivityFund)) }
    var newLabel by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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

                    BudgetRangeFields(
                        title = "饮食预算抽取范围",
                        minValue = foodMin,
                        maxValue = foodMax,
                        onMinChange = { foodMin = it },
                        onMaxChange = { foodMax = it },
                    )

                    OutlinedTextField(
                        value = monthlyActivityFund,
                        onValueChange = { monthlyActivityFund = it },
                        label = { Text("每月总活动资金") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )

                    Button(
                        onClick = {
                            onSave(
                                BudgetRange(
                                    foodMin = foodMin.toDoubleOrNull() ?: 0.0,
                                    foodMax = foodMax.toDoubleOrNull() ?: 0.0,
                                    monthlyActivityFund = monthlyActivityFund.toDoubleOrNull() ?: 0.0,
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("保存设置")
                    }

                    Text(
                        "规则：每天点击一次“抽取今日预算”后生成当天预算；每日只能抽取一次。当日结余会加入活动资金，当日超支会从活动资金扣除。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                                    {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null
                                        )
                                    }
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

        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("记录管理", style = MaterialTheme.typography.titleMedium)

                    if (allRecords.isEmpty()) {
                        Text("暂无记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allRecords.take(20).forEach { record ->
                                RecordCard(
                                    record = record,
                                    onDelete = {
                                        onDeleteRecord(record.dateTime.toLocalDate(), record.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetRangeFields(
    title: String,
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = minValue,
                onValueChange = onMinChange,
                label = { Text("最小值") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            OutlinedTextField(
                value = maxValue,
                onValueChange = onMaxChange,
                label = { Text("最大值") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
        }
    }
}