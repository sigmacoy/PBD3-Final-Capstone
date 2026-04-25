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
import android.util.Log
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
            "green"  to "#4CAF50", "blue"   to "#2196F3", "purple" to "#9C27B0"
        )

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, CheckmarkWidget::class.java))

            // Load once before updating all
            RoutineRepository.load(context)
            ids.forEach { updateWidget(context, mgr, it) }
        }

        fun updateWidget(context: Context, mgr: AppWidgetManager, appWidgetId: Int) {
            Log.d("WIDGET_DEBUG", "updateWidget called ID=$appWidgetId")

            val binding = context
                .getSharedPreferences("widget_bindings", Context.MODE_PRIVATE)
                .getString(appWidgetId.toString(), null) ?: return

            if (!binding.startsWith("checkmark:")) return

            val routineName = binding.removePrefix("checkmark:")

            // ⚠️ REMOVED RoutineRepository.load(context) from here so it doesn't overwrite your toggled state!
            val routine = RoutineRepository.getByName(routineName) ?: return

            val checked = routine.checkStates[0]

            Log.d("WIDGET_DEBUG", "Rendering checked=$checked")

            val resolvedColor = try {
                Color.parseColor(colorMap[routine.color] ?: "#2196F3")
            } catch (e: Exception) { Color.BLUE }

            val views = RemoteViews(context.packageName, R.layout.widget_checkmark)

            // Background
            views.setInt(
                R.id.widget_checkmark_root,
                "setBackgroundColor",
                if (checked) resolvedColor else Color.parseColor("#2A2A2A")
            )

            // Force icon refresh
            views.setImageViewResource(R.id.widget_checkmark_icon, 0)
            views.setImageViewResource(
                R.id.widget_checkmark_icon,
                if (checked) R.drawable.widget_icon_check else R.drawable.widget_icon_close
            )
            views.setInt(R.id.widget_checkmark_icon, "setColorFilter", Color.WHITE)

            // Ring redraw
            val ringColor = if (checked) Color.WHITE else Color.parseColor("#888888")
            views.setImageViewBitmap(
                R.id.widget_checkmark_circle,
                drawRingBitmap(240, 18f, ringColor)
            )

            // Label
            views.setTextViewText(R.id.widget_checkmark_label, routine.name)

            // PendingIntent
            val toggleIntent = Intent(context, CheckmarkWidget::class.java).apply {
                action = ACTION_TOGGLE
                putExtra(EXTRA_ROUTINE_NAME, routineName)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = android.net.Uri.parse("app://$routineName/$appWidgetId")
            }

            val pi = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_checkmark_root, pi)
            mgr.updateAppWidget(appWidgetId, views)
            Log.d("WIDGET_DEBUG", "Widget UI applied")
        }

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
                -90f,
                330f,
                false,
                paint
            )
            return bmp
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WIDGET_DEBUG", "onReceive: ${intent.action}")

        if (intent.action == ACTION_TOGGLE) {
            Log.d("WIDGET_DEBUG", "TOGGLE - processing")

            val routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME) ?: return

            // Load data before modifying
            RoutineRepository.load(context)

            val routine = RoutineRepository.getByName(routineName) ?: return

            // Toggle the state
            routine.checkStates[0] = !routine.checkStates[0]

            // Save data
            RoutineRepository.save(context)

            // Push UI updates using the newly saved data in memory
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, CheckmarkWidget::class.java))
            ids.forEach { id ->
                updateWidget(context, mgr, id)
            }

            context.sendBroadcast(Intent(ACTION_HOME_REFRESH).apply {
                setPackage(context.packageName)
            })

            return
        }

        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        // Load data once when Android OS triggers an update interval
        RoutineRepository.load(context)
        ids.forEach { updateWidget(context, mgr, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context
            .getSharedPreferences("widget_bindings", Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { editor.remove(it.toString()) }
        editor.apply()
    }
}