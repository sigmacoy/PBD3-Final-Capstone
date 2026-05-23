package com.example.pbd3_final_capstone.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.example.pbd3_final_capstone.widget.CheckmarkWidget

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RoutineRepositoryImpl().resetTodayChecks(context)
        CheckmarkWidget.refreshAll(context)

        try {
            val historyIntent = Intent("REFRESH_HISTORY_WIDGET")
            context.sendBroadcast(historyIntent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}