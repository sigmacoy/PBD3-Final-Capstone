package com.example.pbd3_final_capstone.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.pbd3_final_capstone.data.model.InMemoryDB
import com.example.pbd3_final_capstone.data.model.Routine
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoutineRepositoryImpl : RoutineRepository {

    companion object {
        private const val PREFS_NAME  = "routine_prefs"
        private const val KEY_ROUTINES = "routines_json"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
    }

    override fun save(context: Context) {
        val arr = JSONArray()
        InMemoryDB.routines.forEach { r ->
            val obj = JSONObject().apply {
                put("name",          r.name)
                put("question",      r.question)
                put("isMeasurable",  r.isMeasurable)
                put("color",         r.color)
                put("unit",          r.unit)
                put("target",        r.target)
                put("reminderHour",  r.reminderHour)
                put("reminderMinute",r.reminderMinute)
                put("checkStates", r.checkStates.joinToString(",") { if (it) "1" else "0" })
                put("inputValues", r.inputValues.joinToString(","))
                put("notes", r.notes)
                put("frequencyType", r.frequencyType)
                put("frequencyValue1", r.frequencyValue1)
                put("frequencyValue2", r.frequencyValue2)
                put("targetType", r.targetType)
                put("id", r.id)
            }
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_ROUTINES, arr.toString()).apply()
        prefs(context).edit().putString(KEY_LAST_RESET_DATE, getCurrentDate()).apply()
    }

    override fun load(context: Context) {
        InMemoryDB.routines.clear()
        val json = prefs(context).getString(KEY_ROUTINES, "[]") ?: "[]"
        val arr  = JSONArray(json)

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val checkRaw = obj.optString("checkStates", "0,0,0,0").split(",")
            val checks = mutableListOf<Boolean>()
            for (idx in 0 until 4) {
                val value = checkRaw.getOrNull(idx)
                checks.add(value == "1" || value == "true")
            }

            val inputRaw = obj.optString("inputValues", ",,,").split(",")
            val inputs = mutableListOf<String>()
            for (idx in 0 until 4) {
                inputs.add(inputRaw.getOrElse(idx) { "" })
            }

            InMemoryDB.routines.add(Routine(
                name          = obj.getString("name"),
                question      = obj.optString("question", ""),
                isMeasurable  = obj.getBoolean("isMeasurable"),
                color         = obj.getString("color"),
                unit          = obj.optString("unit", ""),
                target        = obj.optString("target", ""),
                reminderHour  = obj.optInt("reminderHour", 8),
                reminderMinute= obj.optInt("reminderMinute", 0),
                checkStates   = checks.toMutableList(),
                inputValues   = inputs.toMutableList(),
                notes         = obj.optString("notes", ""),
                frequencyType = obj.optString("frequencyType", "daily"),
                frequencyValue1 = obj.optInt("frequencyValue1", 1),
                frequencyValue2 = obj.optInt("frequencyValue2", 1),
                targetType    = obj.optString("targetType", "at_least"),
                id            = obj.optLong("id", System.currentTimeMillis())
            ))
        }
        checkAndShiftDays(context)
    }

    private fun shiftAllRoutines() {
        InMemoryDB.routines.forEach { routine ->
            if (routine.isMeasurable) {
                if (routine.inputValues.isNotEmpty()) {
                    routine.inputValues.removeAt(0)
                    routine.inputValues.add("")
                }
            } else {
                if (routine.checkStates.isNotEmpty()) {
                    routine.checkStates.removeAt(0)
                    routine.checkStates.add(false)
                }
            }
        }
    }

    private fun checkAndShiftDays(context: Context) {
        val today = getCurrentDate()
        val lastReset = prefs(context).getString(KEY_LAST_RESET_DATE, today) ?: today

        if (lastReset != today) {
            val daysDiff = calculateDaysDifference(lastReset, today)
            val shifts = daysDiff.coerceIn(1, 4)
            repeat(shifts) { shiftAllRoutines() }

            save(context)
            prefs(context).edit().putString(KEY_LAST_RESET_DATE, today).apply()
        }
    }

    private fun getCurrentDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Calendar.getInstance().time)
    }

    private fun calculateDaysDifference(date1: String, date2: String): Int {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d1 = format.parse(date1)
            val d2 = format.parse(date2)
            val diff = d2.time - d1.time
            (diff / (24 * 60 * 60 * 1000)).toInt()
        } catch (e: Exception) {
            1
        }
    }

    override fun resetTodayChecks(context: Context) {
        load(context)
    }

    override fun forceLoad(context: Context) {
        InMemoryDB.routines.clear()
        load(context)
    }

    override fun getByName(name: String): Routine? = InMemoryDB.routines.find { it.name == name }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}