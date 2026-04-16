package com.example.budgetdiary.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.ui.components.EmptyHint
import com.example.budgetdiary.ui.components.LabelDropdown
import com.example.budgetdiary.ui.components.RecordCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AddRecordScreen(
    currentMonth: YearMonth,
    labels: List<String>,
    recentRecords: List<ExpenseRecord>,
    onAdd: (ExpenseRecord) -> Unit,
    onDelete: (LocalDate, String) -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var label by rememberSaveable { mutableStateOf(labels.firstOrNull() ?: "其他") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var time by rememberSaveable { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var note by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(labels) {
        if (!labels.contains(label) && labels.isNotEmpty()) label = labels.first()
    }

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
                    Text("新增消费记录", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    LabelDropdown(labels = labels, selected = label, onSelected = { label = it })
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("日期（yyyy-MM-dd）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("时间（HH:mm）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                date = LocalDate.now().toString()
                                time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("填入当前时间")
                        }
                        Button(
                            onClick = {
                                val amountValue = amount.toDoubleOrNull()
                                val parsed = runCatching {
                                    LocalDateTime.parse("${date}T${time}")
                                }.getOrNull()

                                if (amountValue != null && amountValue > 0 && parsed != null) {
                                    onAdd(
                                        ExpenseRecord(
                                            amount = amountValue,
                                            label = label,
                                            dateTime = parsed,
                                            note = note.trim(),
                                        )
                                    )
                                    amount = ""
                                    note = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("保存记录")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "${currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))} 最近记录",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        if (recentRecords.isEmpty()) {
            item { EmptyHint("当前月份还没有消费记录") }
        } else {
            lazyItems(recentRecords.take(20), key = { it.id }) { record ->
                RecordCard(
                    record = record,
                    onDelete = { onDelete(record.dateTime.toLocalDate(), record.id) }
                )
            }
        }
    }
}