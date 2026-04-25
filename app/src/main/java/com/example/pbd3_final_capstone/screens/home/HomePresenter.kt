package com.example.pbd3_final_capstone.screens.home

import android.content.Context
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.example.pbd3_final_capstone.utils.ReminderScheduler

class HomePresenter(
    private val view: HomeContract.View,
    private val context: Context
) : HomeContract.Presenter {

    private var isSortedByName  = false
    private var isSortedByColor = false

    override fun loadRoutines() {
        RoutineRepository.load(context)
        view.refreshTable(InMemoryDB.routines)
    }

    override fun addRoutine(routine: Routine) {
        InMemoryDB.routines.add(routine)
        RoutineRepository.save(context)
        ReminderScheduler.scheduleReminder(context, routine)
        view.refreshTable(InMemoryDB.routines)
    }

    override fun sortRoutines(byName: Boolean) {
        // Persist current UI state before sorting
        RoutineRepository.save(context)

        if (byName) {
            isSortedByName = !isSortedByName
            if (isSortedByName) InMemoryDB.routines.sortBy { it.name }
            else                InMemoryDB.routines.sortByDescending { it.name }
        } else {
            isSortedByColor = !isSortedByColor
            if (isSortedByColor) InMemoryDB.routines.sortBy { it.color }
            else                 InMemoryDB.routines.sortByDescending { it.color }
        }
        view.refreshTable(InMemoryDB.routines)
    }
}