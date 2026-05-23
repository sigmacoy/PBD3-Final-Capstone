package com.example.pbd3_final_capstone.data.model

data class Routine(
    val name: String,
    val question: String,
    val isMeasurable: Boolean,
    val color: String,
    val unit: String = "",
    val target: String = "",
    var checkStates: MutableList<Boolean> = MutableList(4) { false },
    var inputValues: MutableList<String> = MutableList(4) { "" },
    var reminderHour: Int = 8,
    var reminderMinute: Int = 0,
    val notes: String = "",
    val frequencyType: String = "daily",
    val frequencyValue1: Int = 1,
    val frequencyValue2: Int = 1,
    val targetType: String = "at_least",
    val id: Long = System.currentTimeMillis()
) {
    fun shiftValues() {
        if (isMeasurable) {
            if (inputValues.isNotEmpty()) {
                inputValues.removeAt(0)
                inputValues.add("")
            }
        } else {
            if (checkStates.isNotEmpty()) {
                checkStates.removeAt(0)
                checkStates.add(false)
            }
        }
    }

    fun getHistorySize(): Int = if (isMeasurable) inputValues.size else checkStates.size

    fun resizeHistory(newSize: Int) {
        when {
            isMeasurable -> {
                while (inputValues.size < newSize) inputValues.add("")
                while (inputValues.size > newSize) inputValues.removeAt(inputValues.size - 1)
            }
            else -> {
                while (checkStates.size < newSize) checkStates.add(false)
                while (checkStates.size > newSize) checkStates.removeAt(checkStates.size - 1)
            }
        }
    }
}