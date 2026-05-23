package com.example.pbd3_final_capstone.screens.summary

import com.example.pbd3_final_capstone.data.model.Routine

interface SummaryContract {
    interface View {
        fun displayRoutine(routine: Routine, frequencyText: String, reminderTime: String)
        fun closeScreen()
        fun navigateToEditMeasurable(routineId: Long)
        fun navigateToEditYesNo(routineId: Long)
        fun showDeleteConfirmation(routineName: String)
        fun drawHistoryCalendar(routine: Routine)
        fun saveDatabaseUpdates()
    }

    interface Presenter {
        fun loadRoutine(routineId: Long)
        fun onBackClicked()
        fun onEditClicked()
        fun onDeleteClicked()
        fun confirmDelete(routineId: Long)
    }
}