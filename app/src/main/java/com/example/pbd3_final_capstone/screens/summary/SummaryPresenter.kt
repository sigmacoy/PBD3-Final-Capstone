package com.example.pbd3_final_capstone.screens.summary

import com.example.pbd3_final_capstone.data.model.InMemoryDB
import com.example.pbd3_final_capstone.data.model.Routine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SummaryPresenter(private val view: SummaryContract.View) : SummaryContract.Presenter {
    private var currentRoutine: Routine? = null

    override fun loadRoutine(routineId: Long) {
        currentRoutine = InMemoryDB.routines.find { it.id == routineId }

        if (currentRoutine == null) {
            view.closeScreen()
            return
        }

        val frequencyText = buildFrequencyText(currentRoutine!!)
        val reminderTime = buildReminderTime(currentRoutine!!)

        view.displayRoutine(currentRoutine!!, frequencyText, reminderTime)
        view.drawHistoryCalendar(currentRoutine!!)
    }

    override fun onBackClicked() {
        view.closeScreen()
    }

    override fun onEditClicked() {
        currentRoutine?.let {
            if (it.isMeasurable) {
                view.navigateToEditMeasurable(it.id)
            } else {
                view.navigateToEditYesNo(it.id)
            }
        }
    }

    override fun onDeleteClicked() {
        currentRoutine?.let {
            view.showDeleteConfirmation(it.name)
        }
    }

    override fun confirmDelete(routineId: Long) {
        InMemoryDB.routines.removeAll { it.id == routineId }
        view.saveDatabaseUpdates()
        view.closeScreen()
    }

    private fun buildFrequencyText(routine: Routine): String {
        return when (routine.frequencyType) {
            "daily" -> "Every day"
            "every_x_days" -> "Every ${routine.frequencyValue1} days"
            "times_per_week" -> "${routine.frequencyValue1} times per week"
            "times_per_month" -> "${routine.frequencyValue1} times per month"
            "times_in_days" -> "${routine.frequencyValue1} times in ${routine.frequencyValue2} days"
            else -> "Every day"
        }
    }

    private fun buildReminderTime(routine: Routine): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, routine.reminderHour)
            set(Calendar.MINUTE, routine.reminderMinute)
        }
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }
}