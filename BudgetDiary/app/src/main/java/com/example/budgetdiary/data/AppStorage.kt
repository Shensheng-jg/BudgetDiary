package com.example.budgetdiary.data

import android.content.Context
import com.example.budgetdiary.model.AppState
import com.example.budgetdiary.model.BudgetRange
import com.example.budgetdiary.model.DailyBudget
import com.example.budgetdiary.model.DailyTask
import com.example.budgetdiary.model.DailyTaskProgress
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
                monthlyTasks = readMonthlyTasks(root.optJSONObject("monthlyTasks") ?: JSONObject()),
                taskProgress = readTaskProgress(root.optJSONObject("taskProgress") ?: JSONObject()),
            )
        }.getOrDefault(AppState())
    }

    fun writeState(state: AppState) {
        val root = JSONObject()
        root.put("monthlyBudgetRanges", writeMonthlyRanges(state.monthlyBudgetRanges))
        root.put("dailyBudgets", writeDailyBudgets(state.dailyBudgets))
        root.put("records", writeRecords(state.records))
        root.put("customLabels", writeStringList(state.customLabels))
        root.put("monthlyTasks", writeMonthlyTasks(state.monthlyTasks))
        root.put("taskProgress", writeTaskProgress(state.taskProgress))
        prefs.edit().putString("state", root.toString()).apply()
    }

    private fun readMonthlyRanges(obj: JSONObject): Map<String, BudgetRange> {
        val result = mutableMapOf<String, BudgetRange>()
        for (key in jsonKeys(obj)) {
            val item = obj.getJSONObject(key)

            val monthlyBudgetTotal =
                if (item.has("monthlyBudgetTotal")) {
                    item.optDouble("monthlyBudgetTotal", 900.0)
                } else {
                    val oldFoodMax = item.optDouble("foodMax", 60.0)
                    val oldActivityFund = item.optDouble(
                        "monthlyActivityFund",
                        item.optDouble("activityMax", 300.0)
                    )
                    oldFoodMax * 30 + oldActivityFund
                }

            val dailyRangeDelta =
                if (item.has("dailyRangeDelta")) {
                    item.optDouble("dailyRangeDelta", 10.0)
                } else {
                    val oldFoodMin = item.optDouble("foodMin", 20.0)
                    val oldFoodMax = item.optDouble("foodMax", 60.0)
                    (oldFoodMax - oldFoodMin).coerceAtLeast(0.0)
                }

            result[key] = BudgetRange(
                monthlyBudgetTotal = monthlyBudgetTotal,
                monthlyActivityFund = item.optDouble(
                    "monthlyActivityFund",
                    item.optDouble("activityMax", 300.0)
                ),
                dailyRangeDelta = dailyRangeDelta,
            )
        }
        return result
    }

    private fun writeMonthlyRanges(data: Map<String, BudgetRange>): JSONObject {
        val obj = JSONObject()
        data.forEach { (key, value) ->
            obj.put(key, JSONObject().apply {
                put("monthlyBudgetTotal", value.monthlyBudgetTotal)
                put("monthlyActivityFund", value.monthlyActivityFund)
                put("dailyRangeDelta", value.dailyRangeDelta)
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

    private fun readMonthlyTasks(obj: JSONObject): Map<String, List<DailyTask>> {
        val result = mutableMapOf<String, List<DailyTask>>()
        for (month in jsonKeys(obj)) {
            val arr = obj.getJSONArray(month)
            val tasks = buildList {
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    add(
                        DailyTask(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            title = item.optString("title", ""),
                            reward = item.optDouble("reward", 0.0),
                        )
                    )
                }
            }.filter { it.title.isNotBlank() && it.reward > 0.0 }
            result[month] = tasks
        }
        return result
    }

    private fun writeMonthlyTasks(data: Map<String, List<DailyTask>>): JSONObject {
        val obj = JSONObject()
        data.forEach { (month, tasks) ->
            val arr = JSONArray()
            tasks.forEach { task ->
                arr.put(JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("reward", task.reward)
                })
            }
            obj.put(month, arr)
        }
        return obj
    }

    private fun readTaskProgress(obj: JSONObject): Map<String, DailyTaskProgress> {
        val result = mutableMapOf<String, DailyTaskProgress>()
        for (date in jsonKeys(obj)) {
            val item = obj.getJSONObject(date)
            val ids = readStringList(item.optJSONArray("completedTaskIds") ?: JSONArray())
            result[date] = DailyTaskProgress(completedTaskIds = ids)
        }
        return result
    }

    private fun writeTaskProgress(data: Map<String, DailyTaskProgress>): JSONObject {
        val obj = JSONObject()
        data.forEach { (date, progress) ->
            obj.put(date, JSONObject().apply {
                put("completedTaskIds", writeStringList(progress.completedTaskIds))
            })
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