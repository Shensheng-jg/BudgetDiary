package com.example.budgetdiary.model

data class AppState(
    val monthlyBudgetRanges: Map<String, BudgetRange> = emptyMap(),
    val dailyBudgets: Map<String, Map<String, DailyBudget>> = emptyMap(),
    val records: Map<String, List<ExpenseRecord>> = emptyMap(),
    val customLabels: List<String> = emptyList(),
    val monthlyTasks: Map<String, List<DailyTask>> = emptyMap(),
    val taskProgress: Map<String, DailyTaskProgress> = emptyMap(),
)