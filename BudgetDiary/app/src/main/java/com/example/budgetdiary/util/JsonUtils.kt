package com.example.budgetdiary.util

import org.json.JSONObject

fun jsonKeys(obj: JSONObject): List<String> = buildList {
    val iterator = obj.keys()
    while (iterator.hasNext()) add(iterator.next())
}