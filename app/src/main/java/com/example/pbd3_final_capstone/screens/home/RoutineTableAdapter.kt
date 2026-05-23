package com.example.pbd3_final_capstone.screens.home

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.model.Routine
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.example.pbd3_final_capstone.utils.ColorHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoutineTableAdapter(
    private val activity: AppCompatActivity,
    private val onRoutineClick: (Routine) -> Unit,
    private val onDataChanged: () -> Unit
) {
    private lateinit var scrollContainer: HorizontalScrollView
    private lateinit var tableRoutines: TableLayout
    private val cellSize: Int
    private val nameWidth: Int

    init {
        val dp = activity.resources.displayMetrics.density
        cellSize = (56 * dp).toInt()
        nameWidth = (105 * dp).toInt()
    }

    fun bindViews() {
        scrollContainer = activity.findViewById(R.id.scrollContainer)
        tableRoutines = activity.findViewById(R.id.tableRoutines)
    }

    fun refresh(routines: List<Routine>) {
        tableRoutines.removeAllViews()

        if (routines.isEmpty()) {
            scrollContainer.visibility = View.GONE
            return
        }

        scrollContainer.visibility = View.VISIBLE
        addHeaderRow()
        routines.forEach { addDataRow(it) }

        tableRoutines.requestLayout()
        tableRoutines.invalidate()
    }

    private fun addHeaderRow() {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        val days = mutableListOf<Pair<String, String>>()
        val calendar = Calendar.getInstance()

        for (i in 0..3) {
            days.add(Pair(dayFormat.format(calendar.time).uppercase(), dateFormat.format(calendar.time)))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        days.reverse()

        val headerRow = TableRow(activity).apply { setPadding(0, 0, 0, 0) }
        headerRow.addView(TextView(activity).apply {
            layoutParams = TableRow.LayoutParams(nameWidth, cellSize)
            setPadding(0, 0, 0, 0)
        })

        days.forEach { (dayText, dateText) ->
            val cell = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(cellSize, cellSize).apply { setMargins(0, 0, 0, 0) }
                setPadding(0, 0, 0, 0)
            }

            cell.addView(TextView(activity).apply {
                text = dayText
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(Color.LTGRAY)
                setPadding(0, 0, 0, 0)
            })

            cell.addView(TextView(activity).apply {
                text = dateText
                textSize = 16f
                gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.WHITE)
                setPadding(0, 0, 0, 0)
            })

            headerRow.addView(cell)
        }
        tableRoutines.addView(headerRow)
    }

    private fun addDataRow(routine: Routine) {
        val resolvedColor = ColorHelper.resolveColor(routine.color)
        val row = TableRow(activity).apply { setPadding(0, 0, 0, 0) }

        val nameLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(8, 0, 4, 0)
            layoutParams = TableRow.LayoutParams(nameWidth, cellSize).apply { setMargins(0, 0, 0, 0) }
        }

        val nameText = TextView(activity).apply {
            text = routine.name
            setTextColor(resolvedColor)
            textSize = 14f
            gravity = Gravity.CENTER
        }
        nameLayout.addView(nameText)

        if (routine.isMeasurable && routine.unit.isNotBlank()) {
            val unitText = TextView(activity).apply {
                text = routine.unit
                setTextColor(resolvedColor)
                textSize = 11f
                alpha = 0.7f
                gravity = Gravity.CENTER
            }
            nameLayout.addView(unitText)
        }

        nameLayout.setOnClickListener { onRoutineClick(routine) }
        row.addView(nameLayout)

        for (i in 0..3) {
            if (routine.isMeasurable) {
                addMeasurableCell(row, routine, i, resolvedColor)
            } else {
                addYesNoCell(row, routine, i, resolvedColor)
            }
        }
        tableRoutines.addView(row)
    }

    private fun addMeasurableCell(row: TableRow, routine: Routine, index: Int, resolvedColor: Int) {
        val targetVal = routine.target.toIntOrNull() ?: 1
        val currentVal = routine.inputValues[index].toIntOrNull() ?: 0

        val input = EditText(activity).apply {
            hint = "0"
            setText(routine.inputValues[index])
            setSelection(text.length)
            setTextColor(if (currentVal >= targetVal) resolvedColor else Color.DKGRAY)
            setHintTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            textSize = 18f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
            minWidth = 0
            minimumWidth = 0
            setPadding(0, 0, 0, 0)
            includeFontPadding = false
            background = null
            layoutParams = TableRow.LayoutParams(cellSize, cellSize).apply { setMargins(0, 0, 0, 0) }
        }

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val value = s?.toString() ?: ""
                routine.inputValues[index] = value
                val newVal = value.toIntOrNull() ?: 0
                input.setTextColor(if (newVal >= targetVal) resolvedColor else Color.DKGRAY)
                RoutineRepositoryImpl().save(activity)
                onDataChanged()
            }
        })

        row.addView(input)
    }

    private fun addYesNoCell(row: TableRow, routine: Routine, index: Int, resolvedColor: Int) {
        val container = LinearLayout(activity).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            layoutParams = TableRow.LayoutParams(cellSize, cellSize).apply { setMargins(0, 0, 0, 0) }
        }

        val checkBox = CheckBox(activity).apply {
            isChecked = routine.checkStates[index]
            setPadding(0, 0, 0, 0)
            minWidth = 0
            minimumWidth = 0
            buttonTintList = android.content.res.ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                intArrayOf(Color.DKGRAY, resolvedColor)
            )
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            routine.checkStates[index] = isChecked
            RoutineRepositoryImpl().save(activity)
            onDataChanged()
        }

        container.addView(checkBox)
        row.addView(container)
    }
}