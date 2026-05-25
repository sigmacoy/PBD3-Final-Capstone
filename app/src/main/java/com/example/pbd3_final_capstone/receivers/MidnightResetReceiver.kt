package com.example.pbd3_final_capstone.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.example.pbd3_final_capstone.widget.WidgetUpdater

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RoutineRepositoryImpl().resetTodayChecks(context)

        // Safely updates BOTH Checkmark and History widgets
        WidgetUpdater.updateAllWidgets(context)
    }
}