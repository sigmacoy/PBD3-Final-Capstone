package com.example.pbd3_final_capstone.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.example.pbd3_final_capstone.widget.CheckmarkWidget

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Load and shift if needed
        RoutineRepository.resetTodayChecks(context)

        // Refresh all widgets
        CheckmarkWidget.refreshAll(context)

        // Optionally refresh History widgets too
        try {
            val historyIntent = Intent("REFRESH_HISTORY_WIDGET")
            context.sendBroadcast(historyIntent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}