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
    val id: Long = System.currentTimeMillis()
){
    fun shiftValues() {
        if (isMeasurable) {
            // Remove oldest (index 0), add new empty at end (index 3)
            inputValues.removeAt(0)
            inputValues.add("")
        } else {
            // Remove oldest (index 0), add new false at end (index 3)
            checkStates.removeAt(0)
            checkStates.add(false)
        }
    }
}

object InMemoryDB {
    val routines = mutableListOf<Routine>()
}