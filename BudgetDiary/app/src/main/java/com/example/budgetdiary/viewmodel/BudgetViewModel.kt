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
import com.example.budgetdiary.model.DaySummary
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.util.randomInRange
import com.example.budgetdiary.util.round2
import java.time.LocalDate
import java.time.YearMonth

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

    fun load(context: Context) {
        val state = AppStorage(context).readState()
        monthlyBudgetRanges = state.monthlyBudgetRanges
        dailyBudgets = state.dailyBudgets
        records = state.records
        customLabels = state.customLabels.distinct().filter { it.isNotBlank() }
    }

    fun persist(context: Context) {
        AppStorage(context).writeState(
            AppState(
                monthlyBudgetRanges = monthlyBudgetRanges,
                dailyBudgets = dailyBudgets,
                records = records,
                customLabels = customLabels,
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

    fun getRange(month: YearMonth): BudgetRange =
        monthlyBudgetRanges[month.toString()] ?: BudgetRange()

    fun updateRange(month: YearMonth, range: BudgetRange) {
        monthlyBudgetRanges = monthlyBudgetRanges + (month.toString() to range)
    }

    fun drawTodayBudget(today: LocalDate = LocalDate.now()): Boolean {
        val month = YearMonth.from(today)
        val monthKey = month.toString()
        val dateKey = today.toString()
        val monthMap = dailyBudgets[monthKey].orEmpty()

        if (monthMap.containsKey(dateKey)) return false

        val range = getRange(month)
        val budget = DailyBudget(
            food = randomInRange(range.foodMin, range.foodMax)
        )

        dailyBudgets = dailyBudgets + (monthKey to (monthMap + (dateKey to budget)))
        return true
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

    fun daySummary(date: LocalDate): DaySummary {
        val month = YearMonth.from(date)
        val monthKey = month.toString()
        val monthMap = dailyBudgets[monthKey].orEmpty()
        val range = getRange(month)

        var activityBalance = range.monthlyActivityFund.round2()

        for (day in 1 until date.dayOfMonth) {
            val d = month.atDay(day)
            val budget = monthMap[d.toString()]
            val spent = records[d.toString()].orEmpty().sumOf { it.amount }
            val diff = ((budget?.total ?: 0.0) - spent).round2()
            activityBalance = (activityBalance + diff).round2()
        }

        val todayExpenses = records[date.toString()].orEmpty().sortedByDescending { it.dateTime }
        val todayBudget = monthMap[date.toString()]
        val todaySpent = todayExpenses.sumOf { it.amount }.round2()
        val todayDiff = ((todayBudget?.total ?: 0.0) - todaySpent).round2()

        return DaySummary(
            date = date,
            budget = todayBudget,
            expenses = todayExpenses,
            activityBalanceBefore = activityBalance,
            activityBalanceAfter = (activityBalance + todayDiff).round2(),
        )
    }

    fun monthSummaries(): List<DaySummary> =
        (1..currentMonth.lengthOfMonth()).map { daySummary(currentMonth.atDay(it)) }

    fun monthRecords(): List<ExpenseRecord> =
        monthSummaries().flatMap { it.expenses }.sortedByDescending { it.dateTime }

    fun allRecordsSorted(): List<ExpenseRecord> =
        records.values.flatten().sortedByDescending { it.dateTime }

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

    fun monthBudgetTotal(): Double = monthTotalAvailable()

    fun monthSpentTotal(): Double = monthSummaries().sumOf { it.spent }.round2()

    fun overspentDays(): Int = monthSummaries().count { it.isOverDailyBudget }

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

    fun highestSpendDay(): DaySummary? = monthSummaries().maxByOrNull { it.spent }
}