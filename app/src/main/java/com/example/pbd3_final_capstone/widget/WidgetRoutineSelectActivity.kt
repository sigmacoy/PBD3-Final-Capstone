package com.example.pbd3_final_capstone.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.example.pbd3_final_capstone.screens.home.InMemoryDB

/**
 * Shown after the user picks Checkmark or History widget type.
 * Lists all routines by name. User taps one to bind it to the widget.
 *
 * Launch with extras:
 *   EXTRA_WIDGET_TYPE  = "checkmark" | "history"
 *   AppWidgetManager.EXTRA_APPWIDGET_ID
 */
    class WidgetRoutineSelectActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WIDGET_TYPE = "widget_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val widgetType = intent.getStringExtra(EXTRA_WIDGET_TYPE) ?: "checkmark"

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        // Load persisted routines
        RoutineRepository.load(this)

        val routines = InMemoryDB.routines
        if (routines.isEmpty()) {
            Toast.makeText(this, "No routines found. Create one first.", Toast.LENGTH_LONG).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Build simple list UI
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Choose a Routine"
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 24)
        }
        root.addView(title)

        val colorMap = mapOf(
            "red" to "#F44336", "orange" to "#FF9800", "yellow" to "#FFEB3B",
            "green" to "#4CAF50", "blue" to "#2196F3", "purple" to "#9C27B0"
        )

        routines.forEach { routine ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 40, 16, 40) // Increased padding for better touch target
            }

            // Color dot
            val dot = TextView(this).apply {
                text = "●"
                textSize = 18f
                val hex = colorMap[routine.color] ?: routine.color
                setTextColor(try { Color.parseColor(hex) } catch (e: Exception) { Color.WHITE })
                setPadding(0, 0, 16, 0)
            }

            val nameView = TextView(this).apply {
                text = routine.name
                textSize = 16f
                setTextColor(Color.WHITE)
            }

            row.addView(dot)
            row.addView(nameView)
            root.addView(row)

            // ADD GREY SEPARATOR
            val separator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (1 * resources.displayMetrics.density).toInt() // 1dp height
                )
                setBackgroundColor(Color.parseColor("#333333")) // Dark grey
            }
            root.addView(separator)

            row.setOnClickListener {
                // Check duplicate checkmark widget
                if (widgetType == "checkmark") {
                    val prefs = getSharedPreferences("widget_bindings", MODE_PRIVATE)
                    val allIds = prefs.all.entries
                        .filter { it.value == "${widgetType}:${routine.name}" }
                    if (allIds.isNotEmpty()) {
                        Toast.makeText(
                            this,
                            "A Checkmark widget for '${routine.name}' already exists.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }

                // Save binding: widgetId → "type:routineName"
                getSharedPreferences("widget_bindings", MODE_PRIVATE)
                    .edit()
                    .putString(appWidgetId.toString(), "${widgetType}:${routine.name}")
                    .apply()

                // Trigger the correct widget provider to update
                val updateIntent = when (widgetType) {
                    "history"   -> Intent(this, HistoryWidget::class.java)
                    else        -> Intent(this, CheckmarkWidget::class.java)
                }.apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                }
                sendBroadcast(updateIntent)

                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
                finish()
            }
        }

        setContentView(ScrollView(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(root)
        })
    }
}