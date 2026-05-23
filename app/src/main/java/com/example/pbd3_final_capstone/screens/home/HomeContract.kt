package com.example.pbd3_final_capstone.screens.home

import com.example.pbd3_final_capstone.data.model.Routine

interface HomeContract {
    interface View {
        fun showTypeDialog()
        fun showCreateDialog(isMeasurable: Boolean)
        fun showSortDialog()
        fun refreshTable(routines: List<Routine>)
    }
    interface Presenter {
        fun loadRoutines()
        fun addRoutine(routine: Routine)
        fun sortRoutines(byName: Boolean)
    }
}