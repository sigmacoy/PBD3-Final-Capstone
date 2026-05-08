package com.example.pbd3_final_capstone.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.RemoteViews
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryWidget : AppWidgetProvider() {

    companion object {
        private val colorMap = mapOf(
            "red" to "#F44336", "orange" to "#FF9800", "yellow" to "#FFEB3B",
            "green" to "#4CAF50", "blue" to "#2196F3", "purple" to "#9C27B0"
        )

        fun updateWidget(context: Context, mgr: AppWidgetManager, appWidgetId: Int) {
            val binding = context.getSharedPreferences("widget_bindings", Context.MODE_PRIVATE)
                .getString(appWidgetId.toString(), null) ?: return

            // Only handle history bindings
            if (!binding.startsWith("history:")) return
            val routineName = binding.removePrefix("history:")

            RoutineRepository.load(context)
            val routine = RoutineRepository.getByName(routineName) ?: return

            val resolvedColor = try {
                Color.parseColor(colorMap[routine.color] ?: routine.color)
            } catch (e: Exception) { Color.BLUE }

            val views  = RemoteViews(context.packageName, R.layout.widget_history)
            val bitmap = buildHistoryBitmap(routine, resolvedColor)

            views.setImageViewBitmap(R.id.widget_history_canvas, bitmap)
            views.setTextViewText(R.id.widget_history_label, routine.name)

            mgr.updateAppWidget(appWidgetId, views)
        }

        /**
         * Fix 2 & 3: Correct calendar grid
         * - Bitmap sized to 3:2 ratio to avoid vertical stretching
         * - Columns anchor to real Sun–Sat weeks so today always appears
         * - Today's cell gets a white outline ring even if unchecked
         */
        private fun buildHistoryBitmap(
            routine: com.example.pbd3_final_capstone.screens.home.Routine,
            accentColor: Int
        ): Bitmap {
            // 1. High-res canvas for crisp scaling
            val W = 1000
            val H = 500
            val bmp    = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            canvas.drawColor(Color.parseColor("#1E1E1E"))

            val weeks   = 10
            val numDays = 7

            // 2. MINIMIZE PADDING to stretch the grid to the very edges
            val padL    = 60f  // Just enough for day names (Sun, Mon)
            val padT    = 45f  // Just enough for month names (Jan, Feb)
            val padB    = 10f  // Almost zero bottom margin

            val cellW   = (W - padL) / weeks
            val cellH   = (H - padT - padB) / numDays

            // 3. MAXIMIZE TEXT SIZE for readability
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.LTGRAY; textSize = 32f; typeface = Typeface.DEFAULT
            }
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; textSize = 28f; textAlign = Paint.Align.CENTER
            }


            val filledPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
            val emptyPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3A3A3A") }
            val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
            }

            val fmt      = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthFmt = SimpleDateFormat("MMM", Locale.getDefault())
            val dayFmt   = SimpleDateFormat("d", Locale.getDefault())

            // Build checked date set from stored checkStates
            val checkedDates = mutableSetOf<String>()
            for (offset in 0..3) {
                val isDone = if (routine.isMeasurable) {
                    val currentCount = routine.inputValues[offset].toIntOrNull() ?: 0
                    val target = routine.target.toIntOrNull() ?: 1
                    currentCount >= target
                } else routine.checkStates[offset]

                if (isDone) {
                    val cal = Calendar.getInstance()

                    // index 0 = 3 days ago
                    // index 1 = 2 days ago
                    // index 2 = yesterday
                    // index 3 = today
                    cal.add(Calendar.DAY_OF_YEAR, offset - 3)

                    checkedDates.add(fmt.format(cal.time))
                }
            }

            val todayStr = fmt.format(Calendar.getInstance().time)

            // Anchor: find the Sunday of the current week
            val anchorSunday = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                // go back (weeks-1) weeks so last column = current week
                add(Calendar.WEEK_OF_YEAR, -(weeks - 1))
            }

            // Draw month labels (top of first column in each new month)
            var lastMonth = ""
            for (col in 0 until weeks) {
                val weekStart = (anchorSunday.clone() as Calendar).apply {
                    add(Calendar.WEEK_OF_YEAR, col)
                }
                val month = monthFmt.format(weekStart.time)
                if (month != lastMonth) {
                    canvas.drawText(month, padL + col * cellW + 15f, padT - 10f, labelPaint)
                    lastMonth = month
                }
            }

            // Draw day-of-week labels (Sun–Sat)
            val dayLabels = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
            dayLabels.forEachIndexed { row, label ->
                val y = padT + row * cellH + cellH * 0.66f
                canvas.drawText(label, 0f, y, labelPaint)
            }

            val today = Calendar.getInstance()
            val radius = minOf(cellW, cellH) * 0.4f

            // Draw cells
            for (col in 0 until weeks) {
                for (row in 0 until numDays) {  // row 0 = Sunday
                    val cellCal = (anchorSunday.clone() as Calendar).apply {
                        add(Calendar.WEEK_OF_YEAR, col)
                        add(Calendar.DAY_OF_YEAR, row)
                    }

                    // Skip future dates
                    if (cellCal.after(today)) continue

                    val dateStr   = fmt.format(cellCal.time)
                    val isChecked = checkedDates.contains(dateStr)
                    val isToday   = dateStr == todayStr

                    val cx = padL + col * cellW + cellW / 2
                    val cy = padT + row * cellH + cellH / 2
                    val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

                    canvas.drawRoundRect(rect, 8f, 8f, if (isChecked) filledPaint else emptyPaint)

                    // White ring on today's cell
                    if (isToday) canvas.drawRoundRect(rect, 8f, 8f, todayRingPaint)

                    // Date number
                    datePaint.color = if (isChecked) Color.WHITE else Color.GRAY
                    canvas.drawText(dayFmt.format(cellCal.time), cx, cy + 6f, datePaint)
                }
            }

            return bmp
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        // Must load the fresh database before drawing!
        RoutineRepository.load(context)
        ids.forEach { updateWidget(context, mgr, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return
            ids.forEach { updateWidget(context, mgr, it) }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("widget_bindings", Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { prefs.remove(it.toString()) }
        prefs.apply()
    }
}

