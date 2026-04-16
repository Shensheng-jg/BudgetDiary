package com.example.budgetdiary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.runtime.Composable
import com.example.budgetdiary.model.DaySummary
import com.example.budgetdiary.model.ExpenseRecord

@Composable
fun CollapsibleTodayOverview(
    expanded: Boolean,
    summary: DaySummary,
    todayRecords: List<ExpenseRecord>,
    hasDrawnToday: Boolean,
    onDrawToday: () -> Unit,
    onDeleteRecord: (String) -> Unit,
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        TodayOverviewCard(
            summary = summary,
            todayRecords = todayRecords,
            hasDrawnToday = hasDrawnToday,
            onDrawToday = onDrawToday,
            onDeleteRecord = onDeleteRecord
        )
    }
}