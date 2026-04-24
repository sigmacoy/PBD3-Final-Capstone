package com.example.pbd3_final_capstone.screens.home

class HomePresenter(private val homeView: HomeContract.View) : HomeContract.Presenter {
    private var isSortedByName = false
    private var isSortedByColor = false

    override fun loadRoutines() {
        homeView.refreshTable(InMemoryDB.routines)
    }

    override fun addRoutine(routine: Routine) {
        InMemoryDB.routines.add(routine)
        homeView.refreshTable(InMemoryDB.routines)
    }

    override fun sortRoutines(byName: Boolean) {
        if (byName) {
            InMemoryDB.routines.sortBy { it.name.lowercase() }
        } else {
            InMemoryDB.routines.sortBy { it.color.lowercase() }
        }

        homeView.refreshTable(InMemoryDB.routines)
    }
}