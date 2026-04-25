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
import android.view.View
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
            // IGNORE PINK
        )

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, CheckmarkWidget::class.java))
            RoutineRepository.load(context)
            ids.forEach { updateWidget(context, mgr, it) }
        }

        fun updateWidget(context: Context, mgr: AppWidgetManager, appWidgetId: Int) {
            Log.d("WIDGET_DEBUG", "updateWidget called ID=$appWidgetId")

            val views = RemoteViews(context.packageName, R.layout.widget_checkmark)
            val prefs = context.getSharedPreferences("widget_bindings", Context.MODE_PRIVATE)
            val binding = prefs.getString(appWidgetId.toString(), null)

            if (binding == null || !binding.startsWith("checkmark:")) {
                views.setTextViewText(R.id.widget_checkmark_label, "Unbound")
                mgr.updateAppWidget(appWidgetId, views)
                return
            }

            val routineName = binding.removePrefix("checkmark:")
            RoutineRepository.load(context)
            val routine = RoutineRepository.getByName(routineName)

            if (routine == null) {
                views.setTextViewText(R.id.widget_checkmark_label, "DB Error")
                mgr.updateAppWidget(appWidgetId, views)
                return
            }

            val resolvedColor = try {
                Color.parseColor(colorMap[routine.color] ?: "#2196F3")
            } catch (e: Exception) { Color.BLUE }

            if (routine.isMeasurable) {
                val currentCount = routine.inputValues[0].toIntOrNull() ?: 0
                val target = routine.target.toIntOrNull() ?: 1
                val achieved = currentCount >= target

                // UI for Measurable
                views.setInt(R.id.widget_checkmark_root, "setBackgroundColor", if (achieved) resolvedColor else Color.parseColor("#2A2A2A"))
                views.setViewVisibility(R.id.widget_checkmark_icon, View.GONE)
                views.setViewVisibility(R.id.widget_checkmark_count, View.VISIBLE)
                views.setTextViewText(R.id.widget_checkmark_count, currentCount.toString())
                views.setImageViewBitmap(R.id.widget_checkmark_circle, drawRingBitmap(240, 18f, if (achieved) Color.WHITE else Color.parseColor("#888888")))

                // INTENT: Open App to Home Menu directly
                val launchIntent = Intent(context, com.example.pbd3_final_capstone.screens.home.HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("FOCUS_ROUTINE", routine.name)
                    data = android.net.Uri.parse("focus://$appWidgetId")
                }
                val pi = PendingIntent.getActivity(context, appWidgetId, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                views.setOnClickPendingIntent(R.id.widget_checkmark_root, pi)
                views.setOnClickPendingIntent(R.id.widget_checkmark_circle, pi)
                views.setOnClickPendingIntent(R.id.widget_checkmark_count, pi)
                views.setOnClickPendingIntent(R.id.widget_checkmark_label, pi)

            } else {
                // UI for Yes/No
                val checked = routine.checkStates[0]
                views.setInt(R.id.widget_checkmark_root, "setBackgroundColor", if (checked) resolvedColor else Color.parseColor("#2A2A2A"))
                views.setViewVisibility(R.id.widget_checkmark_icon, View.VISIBLE)
                views.setViewVisibility(R.id.widget_checkmark_count, View.GONE)
                views.setImageViewResource(R.id.widget_checkmark_icon, if (checked) R.drawable.widget_icon_check else R.drawable.widget_icon_close)
                views.setInt(R.id.widget_checkmark_icon, "setColorFilter", Color.WHITE)
                views.setImageViewBitmap(R.id.widget_checkmark_circle, drawRingBitmap(240, 18f, if (checked) Color.WHITE else Color.parseColor("#888888")))

                // INTENT: Background Broadcast
                val toggleIntent = Intent(context, CheckmarkWidget::class.java).apply {
                    action = ACTION_TOGGLE
                    putExtra(EXTRA_ROUTINE_NAME, routineName)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val pi = PendingIntent.getBroadcast(context, appWidgetId, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                views.setOnClickPendingIntent(R.id.widget_checkmark_root, pi)
                views.setOnClickPendingIntent(R.id.widget_checkmark_circle, pi)
                views.setOnClickPendingIntent(R.id.widget_checkmark_icon, pi)
                views.setOnClickPendingIntent(R.id.widget_checkmark_label, pi)
            }

            views.setTextViewText(R.id.widget_checkmark_label, routine.name)
            mgr.updateAppWidget(appWidgetId, views)
            Log.d("WIDGET_DEBUG", "Widget UI applied successfully")
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
                -90f, 330f, false, paint
            )
            return bmp
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("WIDGET_DEBUG", "onReceive: ${intent.action}")

//        if (intent.action == ACTION_TOGGLE) {
//            val routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME) ?: return
//
//            RoutineRepository.load(context)
//            val routine = RoutineRepository.getByName(routineName) ?: return
//
//            routine.checkStates[0] = !routine.checkStates[0]
//            RoutineRepository.save(context)
//
//            val mgr = AppWidgetManager.getInstance(context)
//            val ids = mgr.getAppWidgetIds(ComponentName(context, CheckmarkWidget::class.java))
//            ids.forEach { id -> updateWidget(context, mgr, id) }
//
//            context.sendBroadcast(Intent(ACTION_HOME_REFRESH).apply {
//                setPackage(context.packageName)
//            })
//        }

        if (intent.action == ACTION_TOGGLE) {
            val routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME) ?: return

            RoutineRepository.load(context)
            val routine = RoutineRepository.getByName(routineName) ?: return

            // Toggle and Save
            routine.checkStates[0] = !routine.checkStates[0]
            RoutineRepository.save(context)

            val mgr = AppWidgetManager.getInstance(context)

            // 1. Refresh all Checkmark Widgets
            val checkmarkIds = mgr.getAppWidgetIds(ComponentName(context, CheckmarkWidget::class.java))
            checkmarkIds.forEach { id -> updateWidget(context, mgr, id) }

            // 2. ⚠️ THE FIX: Broadcast a refresh command to all History Widgets
            val historyIds = mgr.getAppWidgetIds(ComponentName(context, HistoryWidget::class.java))
            if (historyIds.isNotEmpty()) {
                val historyUpdateIntent = Intent(context, HistoryWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, historyIds)
                }
                context.sendBroadcast(historyUpdateIntent)
            }

            // 3. Refresh Home Screen Activity
            context.sendBroadcast(Intent(ACTION_HOME_REFRESH).apply {
                setPackage(context.packageName)
            })
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        RoutineRepository.load(context)
        ids.forEach { updateWidget(context, mgr, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences("widget_bindings", Context.MODE_PRIVATE).edit()
        appWidgetIds.forEach { editor.remove(it.toString()) }
        editor.apply()
    }
}