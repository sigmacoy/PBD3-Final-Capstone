package com.example.pbd3_final_capstone.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository

class CheckmarkWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE       = "com.example.pbd3_final_capstone.CHECKMARK_TOGGLE"
        const val ACTION_HOME_REFRESH = "com.example.pbd3_final_capstone.HOME_REFRESH"
        const val EXTRA_ROUTINE_NAME  = "routine_name"

        private val colorMap = mapOf(
            "red"    to "#F44336", "orange" to "#FF9800", "yellow" to "#FFEB3B",
            "green"  to "#4CAF50", "blue"   to "#2196F3", "purple" to "#9C27B0",
            "pink"   to "#E91E63"
        )

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, CheckmarkWidget::class.java))
            ids.forEach { updateWidget(context, mgr, it) }
        }

        fun updateWidget(context: Context, mgr: AppWidgetManager, appWidgetId: Int) {
            val binding = context
                .getSharedPreferences("widget_bindings", Context.MODE_PRIVATE)
                .getString(appWidgetId.toString(), null) ?: return
            if (!binding.startsWith("checkmark:")) return

            val routineName = binding.removePrefix("checkmark:")
            RoutineRepository.load(context)
            val routine = RoutineRepository.getByName(routineName) ?: return

            val checked = routine.checkStates[0]
            val resolvedColor = try {
                Color.parseColor(colorMap[routine.color] ?: "#2196F3")
            } catch (e: Exception) { Color.BLUE }

            val views = RemoteViews(context.packageName, R.layout.widget_checkmark)

            // ── Root background ───────────────────────────────────────────────
            views.setInt(
                R.id.widget_checkmark_root, "setBackgroundColor",
                if (checked) resolvedColor else Color.parseColor("#2A2A2A")
            )

            // ── Ring bitmap — white ring when checked, gray when not ──────────
            val ringColor = if (checked) Color.WHITE else Color.parseColor("#888888")
            views.setImageViewBitmap(
                R.id.widget_checkmark_circle,
                drawRingBitmap(sizePx = 240, strokePx = 18f, color = ringColor)
            )

            // ── Icon ──────────────────────────────────────────────────────────
            views.setImageViewResource(
                R.id.widget_checkmark_icon,
                if (checked) android.R.drawable.checkbox_on_background
                else         android.R.drawable.ic_menu_close_clear_cancel
            )
            views.setInt(R.id.widget_checkmark_icon, "setColorFilter", Color.WHITE)

            // ── Label ─────────────────────────────────────────────────────────
            views.setTextViewText(R.id.widget_checkmark_label, routine.name)

            // ── Toggle PendingIntent — explicit ComponentName is required ─────
            val toggleIntent = Intent().apply {
                action    = ACTION_TOGGLE
                component = ComponentName(context, CheckmarkWidget::class.java)
                putExtra(EXTRA_ROUTINE_NAME, routineName)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pi = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_checkmark_root, pi)

            mgr.updateAppWidget(appWidgetId, views)
        }

        /**
         * Draws a transparent-background circle with only a stroke ring.
         * This matches the reference design: ring outline, no fill.
         */
        private fun drawRingBitmap(sizePx: Int, strokePx: Float, color: Int): Bitmap {
            val bmp    = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val paint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color       = color
                style            = Paint.Style.STROKE
                strokeWidth      = strokePx
                strokeCap        = Paint.Cap.ROUND
            }
            val inset = strokePx / 2f
            canvas.drawArc(
                RectF(inset, inset, sizePx - inset, sizePx - inset),
                -90f,   // start at top
                330f,   // ~almost full circle, leaving a small gap like the reference
                false,
                paint
            )
            return bmp
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, mgr, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME) ?: return
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

            RoutineRepository.load(context)
            val routine = RoutineRepository.getByName(routineName) ?: return

            routine.checkStates[0] = !routine.checkStates[0]
            RoutineRepository.save(context)

            // Redraw this checkmark widget
            updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)

            // Redraw all history widgets
            val histMgr = AppWidgetManager.getInstance(context)
            val histIds = histMgr.getAppWidgetIds(ComponentName(context, HistoryWidget::class.java))
            histIds.forEach { HistoryWidget.updateWidget(context, histMgr, it) }

            // Notify HomeActivity if in foreground
            context.sendBroadcast(Intent(ACTION_HOME_REFRESH).apply {
                setPackage(context.packageName)
            })
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context
            .getSharedPreferences("widget_bindings", Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { editor.remove(it.toString()) }
        editor.apply()
    }
}