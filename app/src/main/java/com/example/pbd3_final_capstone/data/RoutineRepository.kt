package com.example.pbd3_final_capstone.data

import android.content.Context
import android.content.SharedPreferences
import com.example.pbd3_final_capstone.screens.home.InMemoryDB
import com.example.pbd3_final_capstone.screens.home.Routine
import org.json.JSONArray
import org.json.JSONObject

/**
 * Single source of truth for routines.
 * Persists to SharedPreferences so data survives process death.
 * InMemoryDB is kept in sync so the rest of the app code is unchanged.
 */
object RoutineRepository {

    private const val PREFS_NAME  = "routine_prefs"
    private const val KEY_ROUTINES = "routines_json"

    // ── Save ────────────────────────────────────────────────────────────────
    fun save(context: Context) {
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

                // checkStates as "0,1,0,0"
//                put("checkStates", r.checkStates.joinToString(","))
                put("checkStates", r.checkStates.joinToString(",") { if (it) "1" else "0" })
                // inputValues as "3,,5,"
                put("inputValues", r.inputValues.joinToString(","))
            }
            arr.put(obj)
        }
        prefs(context).edit().putString(KEY_ROUTINES, arr.toString()).apply()
    }

    // ── Load ────────────────────────────────────────────────────────────────
    fun load(context: Context) {
        InMemoryDB.routines.clear()
        val json = prefs(context).getString(KEY_ROUTINES, "[]") ?: "[]"
        val arr  = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)

            val checkRaw = obj.optString("checkStates", "0,0,0,0").split(",")
//            val checks   = BooleanArray(4) { idx -> checkRaw.getOrNull(idx) == "1" }
            val checks = BooleanArray(4) { idx ->
                val value = checkRaw.getOrNull(idx)
                value == "1" || value == "true"
            }

            val inputRaw = obj.optString("inputValues", ",,,").split(",")
            val inputs   = Array(4) { idx -> inputRaw.getOrElse(idx) { "" } }

            InMemoryDB.routines.add(Routine(
                name          = obj.getString("name"),
                question      = obj.optString("question", ""),
                isMeasurable  = obj.getBoolean("isMeasurable"),
                color         = obj.getString("color"),
                unit          = obj.optString("unit", ""),
                target        = obj.optString("target", ""),
                reminderHour  = obj.optInt("reminderHour", 8),
                reminderMinute= obj.optInt("reminderMinute", 0),
                checkStates   = checks,
                inputValues   = inputs
            ))
        }
    }

    // ── Reset today's checkStates (called at midnight) ──────────────────────
    fun resetTodayChecks(context: Context) {
        InMemoryDB.routines.forEach { it.checkStates[0] = false }
        save(context)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    fun getByName(name: String): Routine? = InMemoryDB.routines.find { it.name == name }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}