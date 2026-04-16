package com.example.budgetdiary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.util.money
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.font.FontWeight

@Composable
fun RecordCard(record: ExpenseRecord, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(onClick = {}, label = { Text(record.label) })
                Text(money(record.amount), fontWeight = FontWeight.Bold)
            }
            Text(
                record.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                style = MaterialTheme.typography.bodySmall
            )
            if (record.note.isNotBlank()) Text(record.note)
            TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.size(4.dp))
                Text("删除")
            }
        }
    }
}