package com.example.budgetdiary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgetdiary.model.BudgetTab
import com.example.budgetdiary.ui.components.CollapsibleTodayOverview
import com.example.budgetdiary.ui.components.MonthHeader
import com.example.budgetdiary.ui.screen.AddRecordScreen
import com.example.budgetdiary.ui.screen.BudgetSettingsScreen
import com.example.budgetdiary.ui.screen.CalendarScreen
import com.example.budgetdiary.ui.screen.StatsScreen
import com.example.budgetdiary.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDiaryApp(viewModel: BudgetViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(BudgetTab.Record) }

    LaunchedEffect(Unit) {
        viewModel.load(context)
        loaded = true
    }

    LaunchedEffect(
        viewModel.currentMonth,
        viewModel.monthlyBudgetRanges,
        viewModel.dailyBudgets,
        viewModel.records,
        viewModel.customLabels,
        loaded,
    ) {
        if (loaded) viewModel.persist(context)
    }

    val todaySummary = viewModel.todaySummary()
    val hasDrawnToday = viewModel.hasDrawnToday()
    val todayRecords = viewModel.todayRecords()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Diary") },
                actions = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MonthHeader(
                currentMonth = viewModel.currentMonth,
                budgetTotal = viewModel.monthBudgetTotal(),
                spentTotal = viewModel.monthSpentTotal(),
                overspentDays = viewModel.overspentDays(),
                onPrevious = { viewModel.moveMonth(-1) },
                onNext = { viewModel.moveMonth(1) },
            )

            CollapsibleTodayOverview(
                expanded = true,
                summary = todaySummary,
                todayRecords = todayRecords,
                hasDrawnToday = hasDrawnToday,
                onDrawToday = { viewModel.drawTodayBudget() },
                onDeleteRecord = { id ->
                    viewModel.deleteRecord(todaySummary.date, id)
                }
            )

            TabRow(selectedTabIndex = selectedTab.ordinal) {
                BudgetTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    BudgetTab.Budget -> Icons.Default.Settings
                                    BudgetTab.Record -> Icons.Default.Add
                                    BudgetTab.Calendar -> Icons.Default.CalendarMonth
                                    BudgetTab.Stats -> Icons.Default.Analytics
                                },
                                contentDescription = null,
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (selectedTab) {
                    BudgetTab.Budget -> BudgetSettingsScreen(
                        month = viewModel.currentMonth,
                        range = viewModel.getRange(viewModel.currentMonth),
                        labels = viewModel.allLabels(),
                        customLabels = viewModel.customLabels,
                        allRecords = viewModel.allRecordsSorted(),
                        onSave = { viewModel.updateRange(viewModel.currentMonth, it) },
                        onAddLabel = { viewModel.addCustomLabel(it) },
                        onRemoveLabel = { viewModel.removeCustomLabel(it) },
                        onDeleteRecord = { date, id -> viewModel.deleteRecord(date, id) },
                    )

                    BudgetTab.Record -> AddRecordScreen(
                        currentMonth = viewModel.currentMonth,
                        labels = viewModel.allLabels(),
                        recentRecords = viewModel.monthRecords(),
                        onAdd = { record -> viewModel.addRecord(record) },
                        onDelete = { date, id -> viewModel.deleteRecord(date, id) },
                    )

                    BudgetTab.Calendar -> CalendarScreen(
                        month = viewModel.currentMonth,
                        summaries = viewModel.monthSummaries(),
                        onDelete = { date, id -> viewModel.deleteRecord(date, id) }
                    )

                    BudgetTab.Stats -> StatsScreen(
                        month = viewModel.currentMonth,
                        budgetTotal = viewModel.monthBudgetTotal(),
                        spentTotal = viewModel.monthSpentTotal(),
                        overDays = viewModel.overspentDays(),
                        spentByLabel = viewModel.spentByLabel(),
                        topDay = viewModel.highestSpendDay(),
                        activityFundStart = viewModel.getRange(viewModel.currentMonth).monthlyActivityFund,
                        activityFundEnd = viewModel.monthEndActivityBalance(),
                        summaries = viewModel.monthSummaries(),
                    )
                }
            }
        }
    }
}