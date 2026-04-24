package com.example.pbd3_final_capstone.screens.home

interface HomeContract {
    interface View {
        fun refreshTable(routines: List<Routine>)
        fun showTypeDialog()
        fun showCreateDialog(isMeasurable: Boolean)
        fun showSortDialog()
    }

    interface Presenter {
        fun loadRoutines()
        fun addRoutine(routine: Routine)
        fun sortRoutines(byName: Boolean)
    }
}