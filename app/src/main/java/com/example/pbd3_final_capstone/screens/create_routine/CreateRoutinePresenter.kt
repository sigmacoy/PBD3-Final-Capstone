package com.example.pbd3_final_capstone.screens.create_routine

import com.example.pbd3_final_capstone.data.model.InMemoryDB
import com.example.pbd3_final_capstone.data.model.Routine
import java.util.Locale

class CreateRoutinePresenter(private val view: CreateRoutineContract.View) : CreateRoutineContract.Presenter {

    private var reminderHour = 8
    private var reminderMinute = 0
    private var frequencyType = "daily"
    private var frequencyValue1 = 1
    private var frequencyValue2 = 1
    private var selectedColor = "#64B5F6"
    private var targetType = "at_least"

    override fun loadRoutine(routineId: Long) {
        val routine = InMemoryDB.routines.find { it.id == routineId } ?: return

        reminderHour = routine.reminderHour
        reminderMinute = routine.reminderMinute
        frequencyType = routine.frequencyType
        frequencyValue1 = routine.frequencyValue1
        frequencyValue2 = routine.frequencyValue2
        selectedColor = routine.color
        targetType = routine.targetType ?: "at_least"

        view.displayRoutineData(
            name = routine.name,
            question = routine.question,
            notes = routine.notes,
            color = selectedColor,
            targetTypeDisplay = if (targetType == "at_least") "At least" else "At most",
            frequencyDisplay = buildFrequencyText(),
            reminderDisplay = buildReminderText(),
            unit = routine.unit,
            target = routine.target
        )
    }

    override fun setReminder(hour: Int, minute: Int) {
        reminderHour = hour
        reminderMinute = minute
        view.updateReminderDisplay(buildReminderText())
    }

    override fun setColor(colorHex: String) {
        selectedColor = colorHex
        view.updateColorDisplay(selectedColor)
    }

    override fun setTargetType(isAtLeast: Boolean) {
        targetType = if (isAtLeast) "at_least" else "at_most"
        view.updateTargetTypeDisplay(if (isAtLeast) "At least" else "At most")
    }

    override fun setFrequencyMeasurable(index: Int) {
        frequencyType = when (index) {
            0 -> "daily"
            1 -> "weekly"
            else -> "monthly"
        }
        view.updateFrequencyDisplay(buildFrequencyTextMeasurable(index))
    }

    override fun setFrequencyYesNo(type: String, val1: Int, val2: Int) {
        frequencyType = type
        frequencyValue1 = val1
        frequencyValue2 = val2
        view.updateFrequencyDisplay(buildFrequencyText())
    }

    override fun saveRoutine(
        routineId: Long, isMeasurable: Boolean, name: String, question: String,
        notes: String, unit: String, target: String
    ) {
        if (name.isBlank() || (isMeasurable && (unit.isBlank() || target.isBlank()))) {
            view.showError("Please fill required fields")
            return
        }

        val routineData = Routine(
            id = if (routineId != -1L) routineId else System.currentTimeMillis(),
            name = name,
            question = question,
            isMeasurable = isMeasurable,
            color = selectedColor,
            unit = unit,
            target = target,
            notes = notes,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            frequencyType = frequencyType,
            frequencyValue1 = frequencyValue1,
            frequencyValue2 = frequencyValue2,
            targetType = targetType
        )

        if (routineId != -1L) {
            val index = InMemoryDB.routines.indexOfFirst { it.id == routineId }
            if (index != -1) InMemoryDB.routines[index] = routineData
        } else {
            InMemoryDB.routines.add(routineData)
        }

        view.finishAndSave()
    }

    private fun buildReminderText(): String {
        val amPm = if (reminderHour >= 12) "PM" else "AM"
        val formattedHour = if (reminderHour % 12 == 0) 12 else reminderHour % 12
        return String.format(Locale.getDefault(), "%02d:%02d %s", formattedHour, reminderMinute, amPm)
    }

    private fun buildFrequencyTextMeasurable(index: Int): String {
        return arrayOf("Every day", "Every week", "Every month")[index]
    }

    private fun buildFrequencyText(): String {
        return when (frequencyType) {
            "daily" -> "Every day"
            "every_x_days" -> "Every $frequencyValue1 days"
            "times_per_week" -> "$frequencyValue1 times per week"
            "times_per_month" -> "$frequencyValue1 times per month"
            "times_in_days" -> "$frequencyValue1 times in $frequencyValue2 days"
            else -> "Every day"
        }
    }
}