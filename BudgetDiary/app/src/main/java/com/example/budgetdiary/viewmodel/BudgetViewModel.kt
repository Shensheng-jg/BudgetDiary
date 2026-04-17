package com.example.budgetdiary.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.budgetdiary.data.AppStorage
import com.example.budgetdiary.model.AppState
import com.example.budgetdiary.model.BudgetRange
import com.example.budgetdiary.model.DailyBudget
import com.example.budgetdiary.model.DailyTask
import com.example.budgetdiary.model.DailyTaskProgress
import com.example.budgetdiary.model.DaySummary
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.util.round2
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.round
import kotlin.random.Random

class BudgetViewModel : ViewModel() {
    private val defaultLabels = listOf("饮食", "活动", "交通", "购物", "其他")

    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var monthlyBudgetRanges by mutableStateOf<Map<String, BudgetRange>>(emptyMap())
        private set

    var dailyBudgets by mutableStateOf<Map<String, Map<String, DailyBudget>>>(emptyMap())
        private set

    var records by mutableStateOf<Map<String, List<ExpenseRecord>>>(emptyMap())
        private set

    var customLabels by mutableStateOf<List<String>>(emptyList())
        private set

    var monthlyTasks by mutableStateOf<Map<String, List<DailyTask>>>(emptyMap())
        private set

    var taskProgress by mutableStateOf<Map<String, DailyTaskProgress>>(emptyMap())
        private set

    fun load(context: Context) {
        val state = AppStorage(context).readState()
        monthlyBudgetRanges = state.monthlyBudgetRanges
        dailyBudgets = state.dailyBudgets
        records = state.records
        customLabels = state.customLabels.distinct().filter { it.isNotBlank() }
        monthlyTasks = state.monthlyTasks
        taskProgress = state.taskProgress
    }

    fun persist(context: Context) {
        AppStorage(context).writeState(
            AppState(
                monthlyBudgetRanges = monthlyBudgetRanges,
                dailyBudgets = dailyBudgets,
                records = records,
                customLabels = customLabels,
                monthlyTasks = monthlyTasks,
                taskProgress = taskProgress,
            )
        )
    }

    fun allLabels(): List<String> = (defaultLabels + customLabels).distinct()

    fun addCustomLabel(label: String): Boolean {
        val normalized = label.trim()
        if (normalized.isBlank() || allLabels().contains(normalized)) return false
        customLabels = (customLabels + normalized).sorted()
        return true
    }

    fun removeCustomLabel(label: String) {
        customLabels = customLabels.filterNot { it == label }
    }

    fun moveMonth(offset: Long) {
        currentMonth = currentMonth.plusMonths(offset)
    }

    fun jumpToMonth(target: YearMonth) {
        currentMonth = target
    }

    fun getRange(month: YearMonth): BudgetRange =
        monthlyBudgetRanges[month.toString()] ?: BudgetRange()

    fun updateRange(month: YearMonth, range: BudgetRange) {
        monthlyBudgetRanges = monthlyBudgetRanges + (month.toString() to range)
    }

    private fun dailyBudgetUpperBound(month: YearMonth): Double {
        val range = getRange(month)
        val days = month.lengthOfMonth()
        val distributable = (range.monthlyBudgetTotal - range.monthlyActivityFund).coerceAtLeast(0.0)
        return (distributable / days).round2()
    }

    private fun dailyBudgetLowerBound(month: YearMonth): Double {
        val range = getRange(month)
        val upper = dailyBudgetUpperBound(month)
        return (upper - range.dailyRangeDelta).coerceAtLeast(0.0).round2()
    }

    fun monthBudgetTotal(): Double =
        getRange(currentMonth).monthlyBudgetTotal.round2()

    fun drawTodayBudget() {
        val today = LocalDate.now()
        val month = YearMonth.from(today)

        if (month != currentMonth) return
        if (hasDrawnToday()) return

        val lower = dailyBudgetLowerBound(month)
        val upper = dailyBudgetUpperBound(month)

        val drawn = if (upper <= lower) upper else Random.nextDouble(lower, upper)
        val rounded = (round(drawn * 100) / 100.0).round2()

        val monthKey = month.toString()
        val dayKey = today.toString()
        val monthMap = dailyBudgets[monthKey].orEmpty()

        dailyBudgets = dailyBudgets + (
                monthKey to (monthMap + (dayKey to DailyBudget(food = rounded)))
                )
    }

    fun addRecord(record: ExpenseRecord) {
        val key = record.dateTime.toLocalDate().toString()
        val newList = (records[key].orEmpty() + record).sortedByDescending { it.dateTime }
        records = records + (key to newList)
    }

    fun deleteRecord(date: LocalDate, id: String) {
        val key = date.toString()
        records = records + (key to records[key].orEmpty().filterNot { it.id == id })
    }

    fun monthTasks(month: YearMonth): List<DailyTask> =
        monthlyTasks[month.toString()].orEmpty()

    fun dayCompletedTaskIds(date: LocalDate): List<String> =
        taskProgress[date.toString()]?.completedTaskIds.orEmpty()

    fun addDailyTask(month: YearMonth, title: String, reward: Double): Boolean {
        val normalized = title.trim()
        if (normalized.isBlank() || reward <= 0.0) return false

        val key = month.toString()
        val current = monthlyTasks[key].orEmpty()
        monthlyTasks = monthlyTasks + (key to (current + DailyTask(title = normalized, reward = reward.round2())))
        return true
    }

    fun removeDailyTask(month: YearMonth, taskId: String) {
        val key = month.toString()
        val current = monthlyTasks[key].orEmpty().filterNot { it.id == taskId }
        monthlyTasks = monthlyTasks + (key to current)

        taskProgress = taskProgress.mapValues { (_, progress) ->
            DailyTaskProgress(
                completedTaskIds = progress.completedTaskIds.filterNot { it == taskId }
            )
        }
    }

    fun toggleTaskCompleted(date: LocalDate, taskId: String) {
        val key = date.toString()
        val current = taskProgress[key]?.completedTaskIds.orEmpty()

        val updated = if (current.contains(taskId)) {
            current - taskId
        } else {
            current + taskId
        }

        taskProgress = taskProgress + (key to DailyTaskProgress(updated))
    }

    fun daySummary(date: LocalDate): DaySummary {
        val month = YearMonth.from(date)
        val monthKey = month.toString()
        val monthMap = dailyBudgets[monthKey].orEmpty()
        val range = getRange(month)
        val tasks = monthTasks(month)
        val completedTaskIds = dayCompletedTaskIds(date)

        var activityBalance = range.monthlyActivityFund.round2()

        for (day in 1 until date.dayOfMonth) {
            val d = month.atDay(day)
            val budget = monthMap[d.toString()]
            val spent = records[d.toString()].orEmpty().sumOf { it.amount }.round2()
            val reward = tasks
                .filter { dayCompletedTaskIds(d).contains(it.id) }
                .sumOf { it.reward }
                .round2()

            val effectiveBudget = ((budget?.total ?: 0.0) + reward).round2()
            val diff = (effectiveBudget - spent).round2()
            activityBalance = (activityBalance - reward + diff).round2()
        }

        val todayExpenses = records[date.toString()].orEmpty().sortedByDescending { it.dateTime }
        val todayBudget = monthMap[date.toString()]
        val todaySpent = todayExpenses.sumOf { it.amount }.round2()
        val todayReward = tasks
            .filter { completedTaskIds.contains(it.id) }
            .sumOf { it.reward }
            .round2()

        val effectiveTodayBudget = ((todayBudget?.total ?: 0.0) + todayReward).round2()
        val todayDiff = (effectiveTodayBudget - todaySpent).round2()

        return DaySummary(
            date = date,
            budget = todayBudget,
            expenses = todayExpenses,
            activityBalanceBefore = activityBalance,
            activityBalanceAfter = (activityBalance - todayReward + todayDiff).round2(),
            tasks = tasks,
            completedTaskIds = completedTaskIds,
        )
    }

    fun monthSummaries(): List<DaySummary> =
        (1..currentMonth.lengthOfMonth()).map { daySummary(currentMonth.atDay(it)) }

    fun monthRecords(): List<ExpenseRecord> =
        monthSummaries().flatMap { it.expenses }.sortedByDescending { it.dateTime }

    fun todayDate(): LocalDate = LocalDate.now()

    fun todaySummary(): DaySummary = daySummary(todayDate())

    fun hasDrawnToday(): Boolean {
        val today = LocalDate.now()
        val monthKey = YearMonth.from(today).toString()
        return dailyBudgets[monthKey].orEmpty().containsKey(today.toString())
    }

    fun todayRecords(): List<ExpenseRecord> =
        records[LocalDate.now().toString()].orEmpty().sortedByDescending { it.dateTime }

    fun monthFoodBudgetTotal(): Double =
        monthSummaries().sumOf { it.budget?.total ?: 0.0 }.round2()

    fun monthTotalAvailable(): Double =
        (monthFoodBudgetTotal() + getRange(currentMonth).monthlyActivityFund).round2()

    fun monthSpentTotal(): Double =
        monthSummaries().sumOf { it.spent }.round2()

    fun overspentDays(): Int =
        monthSummaries().count { it.isOverDailyBudget }

    fun monthEndActivityBalance(): Double =
        monthSummaries().lastOrNull()?.activityBalanceAfter
            ?: getRange(currentMonth).monthlyActivityFund.round2()

    fun spentByLabel(): List<Pair<String, Double>> {
        return monthRecords()
            .groupBy { it.label }
            .mapValues { (_, list) -> list.sumOf { it.amount }.round2() }
            .toList()
            .sortedByDescending { it.second }
    }

    fun highestSpendDay(): DaySummary? =
        monthSummaries().maxByOrNull { it.spent }
}