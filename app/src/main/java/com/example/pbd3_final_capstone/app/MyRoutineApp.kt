package com.example.pbd3_final_capstone.app

import android.app.Application
import android.content.Context

class MyRoutineApp : Application() {
    var currentUser: String? = null

    fun saveUser(name: String) {
        currentUser = name
    }

    fun saveRegisteredUser(username: String, pass: String) {
        val prefs = getSharedPreferences("MyRoutinePrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("REG_USER", username)
            .putString("REG_PASS", pass)
            .apply()
    }

    fun getRegisteredUsername(): String {
        val prefs = getSharedPreferences("MyRoutinePrefs", Context.MODE_PRIVATE)
        return prefs.getString("REG_USER", "") ?: ""
    }

    fun getRegisteredPassword(): String {
        val prefs = getSharedPreferences("MyRoutinePrefs", Context.MODE_PRIVATE)
        return prefs.getString("REG_PASS", "") ?: ""
    }
}