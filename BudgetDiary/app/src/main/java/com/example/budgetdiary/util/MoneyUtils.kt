package com.example.budgetdiary.util

import kotlin.math.round
import kotlin.random.Random

fun Double.round2(): Double = round(this * 100) / 100.0

fun money(value: Double): String = "¥" + String.format("%.2f", value)

fun formatInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

fun randomInRange(min: Double, max: Double): Double {
    val low = min.coerceAtMost(max)
    val high = min.coerceAtLeast(max)
    return if (low == high) low.round2() else Random.nextDouble(low, high).round2()
}