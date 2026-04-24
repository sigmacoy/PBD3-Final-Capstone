package com.example.pbd3_final_capstone.app

import android.app.Application

class MyRoutineApp : Application() {
    var currentUser: String? = null
    fun saveUser(name: String) {
        currentUser = name
    }
}