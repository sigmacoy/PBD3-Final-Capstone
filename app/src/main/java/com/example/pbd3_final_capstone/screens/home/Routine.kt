package com.example.pbd3_final_capstone.screens.home

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
    val targetType: String = "at_least",  // Added: "at_least" or "at_most"
    val id: Long = System.currentTimeMillis()
){
    fun shiftValues() {
        if (isMeasurable) {
            // Remove oldest (index 0), add new empty at end
            if (inputValues.isNotEmpty()) {
                inputValues.removeAt(0)
                inputValues.add("")
            }
        } else {
            // Remove oldest (index 0), add new false at end
            if (checkStates.isNotEmpty()) {
                checkStates.removeAt(0)
                checkStates.add(false)
            }
        }
    }

    // Optional: Add method to get history size dynamically
    fun getHistorySize(): Int = if (isMeasurable) inputValues.size else checkStates.size

    // Optional: Add method to resize history if needed
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

object InMemoryDB {
    val routines = mutableListOf<Routine>()
}