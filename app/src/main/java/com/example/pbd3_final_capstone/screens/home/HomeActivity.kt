package com.example.pbd3_final_capstone.screens.home

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import android.view.View
import android.widget.PopupMenu
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.Gravity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.example.pbd3_final_capstone.widget.CheckmarkWidget
import com.example.pbd3_final_capstone.utils.ReminderScheduler
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity(), HomeContract.View {

    // Receives broadcast from CheckmarkWidget tap → instant table refresh
    private val widgetRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            presenter.loadRoutines()
        }
    }
    private lateinit var presenter: HomePresenter
    private lateinit var tableHabits: TableLayout

    private val colorMap = mapOf(
        "red"    to "#F44336",
        "orange" to "#FF9800",
        "yellow" to "#FFEB3B",
        "green"  to "#4CAF50",
        "blue"   to "#2196F3",
        "purple" to "#9C27B0",
    )

    private fun resolveColor(colorStr: String): Int {
        val hex = colorMap[colorStr.lowercase().trim()] ?: colorStr
        return try { Color.parseColor(hex) } catch (e: Exception) { Color.WHITE }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ReminderScheduler.createChannel(this)
        ReminderScheduler.scheduleMidnightReset(this)

        // Use context-aware presenter
        presenter = HomePresenter(this, this)
        tableHabits = findViewById(R.id.tableRoutines)

        findViewById<ImageButton>(R.id.btnAdd).setOnClickListener { showTypeDialog() }
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener { showSortDialog() }

        presenter.loadRoutines()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadRoutines()

        ContextCompat.registerReceiver(
            this,
            widgetRefreshReceiver,
            IntentFilter(CheckmarkWidget.ACTION_HOME_REFRESH),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        RoutineRepository.save(this)
        unregisterReceiver(widgetRefreshReceiver)
    }

    override fun showTypeDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_type, null)

        view.findViewById<View>(R.id.cardYesNo).setOnClickListener {
            dialog.dismiss(); showCreateDialog(false)
        }
        view.findViewById<View>(R.id.cardMeasurable).setOnClickListener {
            dialog.dismiss(); showCreateDialog(true)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun showCreateDialog(isMeasurable: Boolean) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val inputName = EditText(this).apply {
            hint = if (isMeasurable) "Name (e.g. Water Intake)" else "Name (e.g. Exercise)"
        }
        val inputQuestion = EditText(this).apply {
            hint = if (isMeasurable) "Question (e.g. How many glasses?)" else "Question (e.g. Did you exercise?)"
        }
        layout.addView(inputName)
        layout.addView(inputQuestion)

        var inputUnit: EditText? = null
        var inputTarget: EditText? = null
        if (isMeasurable) {
            inputUnit   = EditText(this).apply { hint = "Unit (e.g. glasses)" }
            inputTarget = EditText(this).apply { hint = "Target (e.g. 5)" }
            layout.addView(inputUnit)
            layout.addView(inputTarget)
        }

        // ── Color picker ─────────────────────────────────────────────────────
        layout.addView(TextView(this).apply {
            text = "Pick a color"
            setTextColor(Color.DKGRAY)
            setPadding(0, 16, 0, 8)
        })

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }

        var selectedColor = "red"
        val circleViews = mutableMapOf<String, View>()
        val dp     = resources.displayMetrics.density
        val size   = (32 * dp).toInt()
        val margin = (8  * dp).toInt()

        colorMap.forEach { (name, hex) ->
            val circle = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                background = buildCircleDrawable(hex, isSelected = (name == selectedColor))
            }
            circle.setOnClickListener {
                circleViews[selectedColor]?.background =
                    buildCircleDrawable(colorMap[selectedColor]!!, isSelected = false)
                selectedColor = name
                circle.background = buildCircleDrawable(hex, isSelected = true)
            }
            circleViews[name] = circle
            colorRow.addView(circle)
        }
        circleViews["red"]?.background = buildCircleDrawable(colorMap["red"]!!, isSelected = true)
        layout.addView(colorRow)

        // ── Reminder time picker ─────────────────────────────────────────────
        layout.addView(TextView(this).apply {
            text = "Daily reminder time"
            setTextColor(Color.DKGRAY)
            setPadding(0, 24, 0, 8)
        })

        val timePicker = TimePicker(this).apply {
            setIs24HourView(false)
            hour   = 8
            minute = 0
        }
        layout.addView(timePicker)

        AlertDialog.Builder(this)
            .setTitle(if (isMeasurable) "Create Measurable" else "Create Yes / No")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val routine = Routine(
                    name           = inputName.text.toString(),
                    question       = inputQuestion.text.toString(),
                    isMeasurable   = isMeasurable,
                    color          = selectedColor,
                    unit           = inputUnit?.text?.toString()   ?: "",
                    target         = inputTarget?.text?.toString() ?: "",
                    reminderHour   = timePicker.hour,
                    reminderMinute = timePicker.minute
                )
                presenter.addRoutine(routine)
                // ⚠️ ADD THIS EXACT LINE:
                // This permanently saves the routine so the widget can actually find it!
                com.example.pbd3_final_capstone.data.RoutineRepository.save(this)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showSortDialog() {
        val sortButton = findViewById<View>(R.id.btnSort)
        val popup = PopupMenu(this, sortButton)

        popup.menu.add(0, 0, 0, "Sort").isEnabled = false
        popup.menu.add(0, 1, 1, "By name")
        popup.menu.add(0, 2, 2, "By color")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> presenter.sortRoutines(byName = true)
                2 -> presenter.sortRoutines(byName = false)
            }
            true
        }
        popup.show()
    }

    override fun refreshTable(routines: List<Routine>) {
        val scrollContainer = findViewById<HorizontalScrollView>(R.id.scrollContainer)
        val tableRoutines   = findViewById<TableLayout>(R.id.tableRoutines)

        tableRoutines.removeAllViews()

        if (routines.isEmpty()) {
            scrollContainer.visibility = View.GONE
            return
        }
        scrollContainer.visibility = View.VISIBLE

        val dayFormat  = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d",   Locale.getDefault())
        val days = mutableListOf<Pair<String, String>>()
        val calendar = Calendar.getInstance()
        for (i in 0..3) {
            days.add(Pair(dayFormat.format(calendar.time).uppercase(), dateFormat.format(calendar.time)))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val dp        = resources.displayMetrics.density
        val cellSize  = (50  * dp).toInt()
        val nameWidth = (120 * dp).toInt()

        // ── Header row ───────────────────────────────────────────────────────
        val headerRow = TableRow(this)
        headerRow.addView(TextView(this).apply {
            layoutParams = TableRow.LayoutParams(nameWidth, cellSize)
        })
        days.forEach { (dayText, dateText) ->
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(cellSize, cellSize)
            }
            cell.addView(TextView(this).apply {
                text = dayText; textSize = 10f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER
            })
            cell.addView(TextView(this).apply {
                text = dateText; textSize = 12f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            headerRow.addView(cell)
        }
        tableRoutines.addView(headerRow)

        // ── Data rows ────────────────────────────────────────────────────────
        routines.forEach { routine ->
            val resolvedColor = resolveColor(routine.color)
            val row = TableRow(this)

            row.addView(TextView(this).apply {
                text = "${routine.name}\n${if (routine.isMeasurable) routine.unit else ""}"
                setTextColor(resolvedColor)
                setPadding(16, 35, 16, 0)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = TableRow.LayoutParams(nameWidth, cellSize).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            })

            for (i in 0..3) {
                if (routine.isMeasurable) {
                    val input = EditText(this).apply {
                        hint = "0"
                        setText(routine.inputValues[i])
                        setTextColor(
                            if ((routine.inputValues[i].toIntOrNull() ?: 0) > 0) resolvedColor
                            else Color.DKGRAY
                        )
                        setHintTextColor(Color.DKGRAY)
                        gravity = Gravity.CENTER
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                        layoutParams = TableRow.LayoutParams(cellSize, cellSize).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    val idx = i
                    input.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int)     = Unit
                        override fun afterTextChanged(s: Editable?) {
                            val v = s?.toString() ?: ""
                            routine.inputValues[idx] = v
                            input.setTextColor(
                                if ((v.toIntOrNull() ?: 0) > 0) resolvedColor else Color.DKGRAY
                            )
                            RoutineRepository.save(this@HomeActivity)
                            updateAllWidgets()
                        }
                    })
                    row.addView(input)
                } else {
                    val container = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = TableRow.LayoutParams(cellSize, cellSize).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    val checkBox = CheckBox(this).apply {
                        isChecked = routine.checkStates[i]
                        buttonTintList = android.content.res.ColorStateList(
                            arrayOf(
                                intArrayOf(-android.R.attr.state_checked),
                                intArrayOf( android.R.attr.state_checked)
                            ),
                            intArrayOf(Color.DKGRAY, resolvedColor)
                        )
                    }
                    val idx = i
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        routine.checkStates[idx] = isChecked
                        RoutineRepository.save(this@HomeActivity)
                        updateAllWidgets()
                    }
                    container.addView(checkBox)
                    row.addView(container)
                }
            }
            tableRoutines.addView(row)
        }
    }

    private fun buildCircleDrawable(hexColor: String, isSelected: Boolean): android.graphics.drawable.Drawable {
        val color = try { Color.parseColor(hexColor) } catch (e: Exception) { Color.WHITE }
        val dp    = resources.displayMetrics.density
        return if (isSelected) {
            val outer = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(Color.WHITE)
            }
            val inner = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(color)
            }
            val inset = (4 * dp).toInt()
            android.graphics.drawable.LayerDrawable(arrayOf(outer, inner)).also {
                it.setLayerInset(1, inset, inset, inset, inset)
            }
        } else {
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(color)
            }
        }
    }

    private fun updateAllWidgets() {
        val mgr = android.appwidget.AppWidgetManager.getInstance(this)

        // Update Checkmark Widgets
        val checkmarkIds = mgr.getAppWidgetIds(android.content.ComponentName(this, CheckmarkWidget::class.java))
        if (checkmarkIds.isNotEmpty()) {
            sendBroadcast(Intent(this, CheckmarkWidget::class.java).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, checkmarkIds)
            })
        }

        // Update History Widgets
        val historyIds = mgr.getAppWidgetIds(android.content.ComponentName(this, com.example.pbd3_final_capstone.widget.HistoryWidget::class.java))
        if (historyIds.isNotEmpty()) {
            sendBroadcast(Intent(this, com.example.pbd3_final_capstone.widget.HistoryWidget::class.java).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, historyIds)
            })
        }
    }
}