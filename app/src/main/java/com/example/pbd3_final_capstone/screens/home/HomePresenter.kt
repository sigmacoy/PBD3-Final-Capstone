package com.example.pbd3_final_capstone.screens.home

import android.content.Context
import com.example.pbd3_final_capstone.data.model.InMemoryDB
import com.example.pbd3_final_capstone.data.model.Routine
import com.example.pbd3_final_capstone.data.repository.RoutineRepository
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.example.pbd3_final_capstone.utils.ReminderScheduler

class HomePresenter(
    private val view: HomeContract.View,
    private val context: Context
) : HomeContract.Presenter {

    private val repository: RoutineRepository = RoutineRepositoryImpl()
    var isSortedByName  = false
    var isSortedByColor = false

    override fun loadRoutines() {
        repository.load(context)
        view.refreshTable(InMemoryDB.routines)
    }

    override fun addRoutine(routine: Routine) {
        InMemoryDB.routines.add(routine)
        repository.save(context)
        ReminderScheduler.scheduleReminder(context, routine)
        view.refreshTable(InMemoryDB.routines)
    }

    override fun sortRoutines(byName: Boolean) {
        if (byName) {
            isSortedByColor = false
            isSortedByName = true
            InMemoryDB.routines.sortBy { it.name.lowercase() }
        } else {
            isSortedByName = false
            isSortedByColor = true
            InMemoryDB.routines.sortBy { it.color }
        }

        // Move save down here so it saves the SORTED list!
        repository.save(context)
        view.refreshTable(InMemoryDB.routines.toList())
    }
}