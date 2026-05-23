package com.example.pbd3_final_capstone.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.pbd3_final_capstone.data.repository.RoutineRepository
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.example.pbd3_final_capstone.utils.ReminderScheduler

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("routine_name") ?: return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "routine_reminders")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Routine Reminder")
            .setContentText("Don't forget: $name")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(name.hashCode(), notification)

        // Reschedule for tomorrow
        val repository = RoutineRepositoryImpl()
        repository.load(context)
        val routine = repository.getByName(name) ?: return
        ReminderScheduler.scheduleReminder(context, routine)
    }
}