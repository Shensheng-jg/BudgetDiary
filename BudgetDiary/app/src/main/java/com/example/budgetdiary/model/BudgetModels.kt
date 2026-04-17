package com.example.budgetdiary.model

import com.example.budgetdiary.util.round2
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class BudgetRange(
    val monthlyBudgetTotal: Double = 1800.0,
    val monthlyActivityFund: Double = 300.0,
    val dailyRangeDelta: Double = 10.0,
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
    val tasks: List<DailyTask> = emptyList(),
    val completedTaskIds: List<String> = emptyList(),
) {
    val spent: Double get() = expenses.sumOf { it.amount }.round2()

    val completedTaskReward: Double
        get() = tasks
            .filter { completedTaskIds.contains(it.id) }
            .sumOf { it.reward }
            .round2()

    val totalAvailableBudget: Double
        get() = ((budget?.total ?: 0.0) + completedTaskReward).round2()

    val dailyDiff: Double get() = (totalAvailableBudget - spent).round2()
    val isOverDailyBudget: Boolean get() = budget != null && dailyDiff < 0
    val isActivityFundNegative: Boolean get() = activityBalanceAfter < 0
}

sealed interface CalendarCell {
    data object Empty : CalendarCell
    data class Day(val summary: DaySummary) : CalendarCell
}

data class DailyTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val reward: Double,
)

data class DailyTaskProgress(
    val completedTaskIds: List<String> = emptyList(),
)