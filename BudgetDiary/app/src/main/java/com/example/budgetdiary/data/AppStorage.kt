package com.example.budgetdiary.data

import android.content.Context
import com.example.budgetdiary.model.AppState
import com.example.budgetdiary.model.BudgetRange
import com.example.budgetdiary.model.DailyBudget
import com.example.budgetdiary.model.ExpenseRecord
import com.example.budgetdiary.util.jsonKeys
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.UUID

class AppStorage(context: Context) {
    private val prefs = context.getSharedPreferences("budget_diary", Context.MODE_PRIVATE)

    fun readState(): AppState {
        val raw = prefs.getString("state", null) ?: return AppState()
        return runCatching {
            val root = JSONObject(raw)
            AppState(
                monthlyBudgetRanges = readMonthlyRanges(root.optJSONObject("monthlyBudgetRanges") ?: JSONObject()),
                dailyBudgets = readDailyBudgets(root.optJSONObject("dailyBudgets") ?: JSONObject()),
                records = readRecords(root.optJSONObject("records") ?: JSONObject()),
                customLabels = readStringList(root.optJSONArray("customLabels") ?: JSONArray()),
            )
        }.getOrDefault(AppState())
    }

    fun writeState(state: AppState) {
        val root = JSONObject()
        root.put("monthlyBudgetRanges", writeMonthlyRanges(state.monthlyBudgetRanges))
        root.put("dailyBudgets", writeDailyBudgets(state.dailyBudgets))
        root.put("records", writeRecords(state.records))
        root.put("customLabels", writeStringList(state.customLabels))
        prefs.edit().putString("state", root.toString()).apply()
    }

    private fun readMonthlyRanges(obj: JSONObject): Map<String, BudgetRange> {
        val result = mutableMapOf<String, BudgetRange>()
        for (key in jsonKeys(obj)) {
            val item = obj.getJSONObject(key)
            result[key] = BudgetRange(
                foodMin = item.optDouble("foodMin", 20.0),
                foodMax = item.optDouble("foodMax", 60.0),
                monthlyActivityFund = item.optDouble(
                    "monthlyActivityFund",
                    item.optDouble("activityMax", 300.0)
                ),
            )
        }
        return result
    }

    private fun writeMonthlyRanges(data: Map<String, BudgetRange>): JSONObject {
        val obj = JSONObject()
        data.forEach { (key, value) ->
            obj.put(key, JSONObject().apply {
                put("foodMin", value.foodMin)
                put("foodMax", value.foodMax)
                put("monthlyActivityFund", value.monthlyActivityFund)
            })
        }
        return obj
    }

    private fun readDailyBudgets(obj: JSONObject): Map<String, Map<String, DailyBudget>> {
        val result = mutableMapOf<String, Map<String, DailyBudget>>()
        for (month in jsonKeys(obj)) {
            val monthObj = obj.getJSONObject(month)
            val dayMap = mutableMapOf<String, DailyBudget>()
            for (date in jsonKeys(monthObj)) {
                val item = monthObj.getJSONObject(date)
                dayMap[date] = DailyBudget(
                    food = item.optDouble("food", item.optDouble("total", 0.0)),
                )
            }
            result[month] = dayMap
        }
        return result
    }

    private fun writeDailyBudgets(data: Map<String, Map<String, DailyBudget>>): JSONObject {
        val obj = JSONObject()
        data.forEach { (month, map) ->
            val monthObj = JSONObject()
            map.forEach { (date, value) ->
                monthObj.put(date, JSONObject().apply {
                    put("food", value.food)
                })
            }
            obj.put(month, monthObj)
        }
        return obj
    }

    private fun readRecords(obj: JSONObject): Map<String, List<ExpenseRecord>> {
        val result = mutableMapOf<String, List<ExpenseRecord>>()
        for (date in jsonKeys(obj)) {
            val arr = obj.getJSONArray(date)
            val items = buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    add(
                        ExpenseRecord(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            amount = item.optDouble("amount", 0.0),
                            label = item.optString("label", "其他"),
                            dateTime = LocalDateTime.parse(item.getString("dateTime")),
                            note = item.optString("note", ""),
                        )
                    )
                }
            }
            result[date] = items
        }
        return result
    }

    private fun writeRecords(data: Map<String, List<ExpenseRecord>>): JSONObject {
        val obj = JSONObject()
        data.forEach { (date, list) ->
            val arr = JSONArray()
            list.forEach { item ->
                arr.put(JSONObject().apply {
                    put("id", item.id)
                    put("amount", item.amount)
                    put("label", item.label)
                    put("dateTime", item.dateTime.toString())
                    put("note", item.note)
                })
            }
            obj.put(date, arr)
        }
        return obj
    }

    private fun readStringList(arr: JSONArray): List<String> = buildList {
        for (i in 0 until arr.length()) add(arr.optString(i))
    }.filter { it.isNotBlank() }

    private fun writeStringList(list: List<String>): JSONArray = JSONArray().apply {
        list.forEach { put(it) }
    }
}