package com.example.pbd3_final_capstone.screens.home

data class Routine(
    val name: String,
    val question: String = "",
    val isMeasurable: Boolean,
    val color: String,
    val unit: String = "",
    val target: String = "",
    // index 0 = today … 3 = three days ago
    val checkStates: BooleanArray = BooleanArray(4) { false },
    val inputValues: Array<String>  = Array(4) { "" }
)

object InMemoryDB {
    val routines = mutableListOf<Routine>()
}