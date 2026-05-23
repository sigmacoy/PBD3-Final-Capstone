package com.example.pbd3_final_capstone.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdater {
    fun updateAllWidgets(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)

        val checkmarkIds = mgr.getAppWidgetIds(
            ComponentName(context, CheckmarkWidget::class.java)
        )
        checkmarkIds.forEach { CheckmarkWidget.updateWidget(context, mgr, it) }

        val historyIds = mgr.getAppWidgetIds(
            ComponentName(context, HistoryWidget::class.java)
        )
        historyIds.forEach { HistoryWidget.updateWidget(context, mgr, it) }
    }
}