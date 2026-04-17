package com.example.budgetdiary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.budgetdiary.ui.components.LabelDropdown
import com.example.budgetdiary.ui.components.MonthHeader
import com.example.budgetdiary.ui.screen.AddRecordScreen
import com.example.budgetdiary.ui.screen.BudgetSettingsScreen
import com.example.budgetdiary.ui.screen.CalendarScreen
import com.example.budgetdiary.ui.screen.StatsScreen
import com.example.budgetdiary.viewmodel.BudgetViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDiaryApp(viewModel: BudgetViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var loaded by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(BudgetTab.Record) }

    var todayExpanded by rememberSaveable { mutableStateOf(true) }
    var lastScrollPosition by remember { mutableIntStateOf(0) }

    var showMonthPicker by rememberSaveable { mutableStateOf(false) }

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
        viewModel.monthlyTasks,
        viewModel.taskProgress,
        loaded,
    ) {
        if (loaded) viewModel.persist(context)
    }

    val todaySummary = viewModel.todaySummary()
    val hasDrawnToday = viewModel.hasDrawnToday()
    val todayRecords = viewModel.todayRecords()

    fun handleScroll(position: Int) {
        if (position <= 0) {
            todayExpanded = true
        } else if (position > lastScrollPosition) {
            todayExpanded = false
        }
        lastScrollPosition = position
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Diary") },
                actions = {
                    IconButton(onClick = { showMonthPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "选择年月"
                        )
                    }
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
                expanded = todayExpanded,
                onPrevious = { viewModel.moveMonth(-1) },
                onNext = { viewModel.moveMonth(1) },
            )

            CollapsibleTodayOverview(
                expanded = todayExpanded,
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

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    BudgetTab.Budget -> BudgetSettingsScreen(
                        month = viewModel.currentMonth,
                        range = viewModel.getRange(viewModel.currentMonth),
                        labels = viewModel.allLabels(),
                        customLabels = viewModel.customLabels,
                        tasks = viewModel.monthTasks(viewModel.currentMonth),
                        completedTaskIds = viewModel.dayCompletedTaskIds(viewModel.todayDate()),
                        onSave = { viewModel.updateRange(viewModel.currentMonth, it) },
                        onAddLabel = { viewModel.addCustomLabel(it) },
                        onRemoveLabel = { viewModel.removeCustomLabel(it) },
                        onAddTask = { title, reward ->
                            viewModel.addDailyTask(viewModel.currentMonth, title, reward)
                        },
                        onRemoveTask = { taskId ->
                            viewModel.removeDailyTask(viewModel.currentMonth, taskId)
                        },
                        onToggleTask = { taskId ->
                            viewModel.toggleTaskCompleted(viewModel.todayDate(), taskId)
                        },
                        onScrollChanged = ::handleScroll,
                    )

                    BudgetTab.Record -> AddRecordScreen(
                        currentMonth = viewModel.currentMonth,
                        labels = viewModel.allLabels(),
                        recentRecords = viewModel.monthRecords(),
                        onAdd = { record -> viewModel.addRecord(record) },
                        onDelete = { date, id -> viewModel.deleteRecord(date, id) },
                        onScrollChanged = ::handleScroll,
                    )

                    BudgetTab.Calendar -> CalendarScreen(
                        month = viewModel.currentMonth,
                        summaries = viewModel.monthSummaries(),
                        onDelete = { date, id -> viewModel.deleteRecord(date, id) },
                        onScrollChanged = ::handleScroll,
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
                        onScrollChanged = ::handleScroll,
                    )
                }
            }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            initialMonth = viewModel.currentMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { targetMonth ->
                showMonthPicker = false
                viewModel.jumpToMonth(targetMonth)
            }
        )
    }
}

@Composable
private fun MonthPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    var selectedYear by rememberSaveable { mutableStateOf(initialMonth.year.toString()) }
    var selectedMonth by rememberSaveable { mutableStateOf(initialMonth.monthValue.toString()) }

    val yearOptions = (2020..2035).map { it.toString() }
    val monthOptions = (1..12).map { it.toString().padStart(2, '0') }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择年月") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        LabelDropdown(
                            labels = yearOptions,
                            selected = selectedYear,
                            onSelected = { selectedYear = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        LabelDropdown(
                            labels = monthOptions,
                            selected = selectedMonth.padStart(2, '0'),
                            onSelected = { selectedMonth = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val year = selectedYear.toIntOrNull() ?: initialMonth.year
                    val month = selectedMonth.toIntOrNull()?.coerceIn(1, 12) ?: initialMonth.monthValue
                    onConfirm(YearMonth.of(year, month))
                }
            ) {
                Text("跳转")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}