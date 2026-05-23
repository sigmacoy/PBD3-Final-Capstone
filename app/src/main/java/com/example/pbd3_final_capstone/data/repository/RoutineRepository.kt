package com.example.pbd3_final_capstone.data.repository

import android.content.Context
import com.example.pbd3_final_capstone.data.model.Routine

interface RoutineRepository {
    fun save(context: Context)
    fun load(context: Context)
    fun resetTodayChecks(context: Context)
    fun forceLoad(context: Context)
    fun getByName(name: String): Routine?
}