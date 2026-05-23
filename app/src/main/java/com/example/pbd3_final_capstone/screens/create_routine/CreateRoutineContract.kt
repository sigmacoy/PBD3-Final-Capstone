package com.example.pbd3_final_capstone.screens.create_routine

interface CreateRoutineContract {
    interface View {
        fun displayRoutineData(
            name: String, question: String, notes: String, color: String,
            targetTypeDisplay: String, frequencyDisplay: String, reminderDisplay: String,
            unit: String, target: String
        )
        fun updateReminderDisplay(text: String)
        fun updateFrequencyDisplay(text: String)
        fun updateColorDisplay(colorHex: String)
        fun updateTargetTypeDisplay(text: String)
        fun showError(message: String)
        fun finishAndSave()
    }

    interface Presenter {
        fun loadRoutine(routineId: Long)
        fun setReminder(hour: Int, minute: Int)
        fun setColor(colorHex: String)
        fun setTargetType(isAtLeast: Boolean)
        fun setFrequencyMeasurable(index: Int)
        fun setFrequencyYesNo(type: String, val1: Int, val2: Int)
        fun saveRoutine(
            routineId: Long, isMeasurable: Boolean,
            name: String, question: String, notes: String,
            unit: String = "", target: String = ""
        )
    }
}