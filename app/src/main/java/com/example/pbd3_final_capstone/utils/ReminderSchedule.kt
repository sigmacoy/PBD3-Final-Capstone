package com.example.pbd3_final_capstone.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.example.pbd3_final_capstone.screens.home.Routine
import java.util.Calendar

object ReminderScheduler {

    private const val CHANNEL_ID    = "routine_reminders"
    private const val MIDNIGHT_CODE = 99999   // unique request code for midnight reset

    // ── Schedule a daily reminder notification for one routine ──────────────
    fun scheduleReminder(context: Context, routine: Routine) {
        val am     = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("routine_name", routine.name)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            routine.name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, routine.reminderHour)
            set(Calendar.MINUTE,      routine.reminderMinute)
            set(Calendar.SECOND,      0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        // On Android 12+ use setExactAndAllowWhileIdle for reliable delivery
        // setRepeating is inexact on modern Android — reschedule in the receiver instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        }
    }

    // ── Cancel a reminder ───────────────────────────────────────────────────
    fun cancelReminder(context: Context, routine: Routine) {
        val am     = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi     = PendingIntent.getBroadcast(
            context,
            routine.name.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    // ── Schedule midnight reset (resets all today checkStates to false) ─────
    fun scheduleMidnightReset(context: Context) {
        val am     = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MidnightResetReceiver::class.java)
        val pi     = PendingIntent.getBroadcast(
            context,
            MIDNIGHT_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE,      0)
            set(Calendar.SECOND,      0)
            add(Calendar.DAY_OF_YEAR, 1)   // next midnight
        }

        am.setRepeating(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
    }

    // ── Create notification channel (call once on app start) ────────────────
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Routine Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily routine check-in reminders" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}