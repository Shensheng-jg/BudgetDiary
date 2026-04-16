package com.example.budgetdiary.model

import com.example.budgetdiary.util.round2
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class BudgetRange(
    val foodMin: Double = 20.0,
    val foodMax: Double = 60.0,
    val monthlyActivityFund: Double = 300.0,
)

data class DailyBudget(
    val food: Double,
) {
    val total: Double get() = food.round2()
}

data class ExpenseRecord(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val label: String,
    val dateTime: LocalDateTime,
    val note: String = "",
)

data class DaySummary(
    val date: LocalDate,
    val budget: DailyBudget?,
    val expenses: List<ExpenseRecord>,
    val activityBalanceBefore: Double,
    val activityBalanceAfter: Double,
) {
    val spent: Double get() = expenses.sumOf { it.amount }.round2()
    val dailyDiff: Double get() = ((budget?.total ?: 0.0) - spent).round2()
    val isOverDailyBudget: Boolean get() = budget != null && dailyDiff < 0
    val isActivityFundNegative: Boolean get() = activityBalanceAfter < 0
}

sealed interface CalendarCell {
    data object Empty : CalendarCell
    data class Day(val summary: DaySummary) : CalendarCell
}