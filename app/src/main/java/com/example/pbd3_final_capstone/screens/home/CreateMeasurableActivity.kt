package com.example.pbd3_final_capstone.screens.home

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository
import java.util.Locale
import android.view.View
import com.google.android.material.button.MaterialButton


class CreateMeasurableActivity : AppCompatActivity() {
    private lateinit var inputName: EditText
    private lateinit var inputQuestion: EditText
    private lateinit var inputUnit: EditText
    private lateinit var inputTarget: EditText
    private lateinit var inputNotes: EditText
    private lateinit var btnFrequency: AutoCompleteTextView
    private lateinit var btnReminder: MaterialButton
    private lateinit var btnSave: Button
    private lateinit var colorPreview: View
    private lateinit var inputTargetType: MaterialButton

    private var reminderHour = 8
    private var reminderMinute = 0
    private var frequencyType = "daily"
    private var frequencyValue1 = 1
    private var frequencyValue2 = 1
    private var selectedColor = "#64B5F6"
    private var editingRoutineId: Long = -1L
    private var targetType = "at_least"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_measurable)

        initViews()
        setupListeners()

        if (intent.getBooleanExtra("edit_mode", false)) {
            editingRoutineId = intent.getLongExtra("routine_id", -1L)
            loadRoutineData()
        }
    }

    private fun initViews() {
        inputName = findViewById(R.id.inputName)
        inputQuestion = findViewById(R.id.inputQuestion)
        inputUnit = findViewById(R.id.inputUnit)
        inputTarget = findViewById(R.id.inputTarget)
        inputNotes = findViewById(R.id.inputNotes)
        btnFrequency = findViewById(R.id.btnFrequency)
        btnReminder = findViewById(R.id.btnReminder)
        btnSave = findViewById(R.id.btnSave)
        colorPreview = findViewById(R.id.colorPreview)
        inputTargetType = findViewById(R.id.inputTargetType)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnReminder.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    reminderHour = hour
                    reminderMinute = minute
                    updateReminderText()
                },
                reminderHour,
                reminderMinute,
                false
            ).show()
        }

        btnFrequency.setOnClickListener {
            showFrequencyDialog()
        }

        btnSave.setOnClickListener {
            saveRoutine()
        }

        colorPreview.setOnClickListener {
            showColorPicker()
        }

        inputTargetType.setOnClickListener {
            showTargetTypeDialog()
        }
    }

    private fun updateReminderText() {
        val amPm = if (reminderHour >= 12) "PM" else "AM"
        val formattedHour = if (reminderHour % 12 == 0) 12 else reminderHour % 12
        btnReminder.text = String.format(
            Locale.getDefault(),
            "%02d:%02d %s",
            formattedHour,
            reminderMinute,
            amPm
        )
    }

    private fun showTargetTypeDialog() {
        val items = arrayOf("At least", "At most")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Target Type")
            .setItems(items) { _, which ->
                targetType = if (which == 0) "at_least" else "at_most"
                inputTargetType.text = items[which]
            }
            .show()
    }

    private fun showFrequencyDialog() {
        val items = arrayOf(
            "Every day",
            "Every X days",
            "X times per week",
            "X times per month",
            "X times in Y days"
        )
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Frequency")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {
                        frequencyType = "daily"
                        btnFrequency.setText("Every day")
                    }
                    1 -> {
                        frequencyType = "every_x_days"
                        showNumberPickerDialog("Enter number of days", 2, 30) { value ->
                            frequencyValue1 = value
                            btnFrequency.setText("Every $value days")
                        }
                    }
                    2 -> {
                        frequencyType = "times_per_week"
                        showNumberPickerDialog("Enter times per week", 1, 7) { value ->
                            frequencyValue1 = value
                            btnFrequency.setText("$value times per week")
                        }
                    }
                    3 -> {
                        frequencyType = "times_per_month"
                        showNumberPickerDialog("Enter times per month", 1, 31) { value ->
                            frequencyValue1 = value
                            btnFrequency.setText("$value times per month")
                        }
                    }
                    4 -> {
                        frequencyType = "times_in_days"
                        showTwoNumberPickerDialog("Times in Days") { times, days ->
                            frequencyValue1 = times
                            frequencyValue2 = days
                            btnFrequency.setText("$times times in $days days")
                        }
                    }
                }
            }
            .show()
    }

    private fun showNumberPickerDialog(title: String, min: Int, max: Int, onResult: (Int) -> Unit) {
        val numberPicker = NumberPicker(this).apply {
            this.minValue = min
            this.maxValue = max
            this.value = min
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                onResult(numberPicker.value)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTwoNumberPickerDialog(title: String, onResult: (Int, Int) -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }

        val timesLabel = TextView(this).apply {
            text = "Times:"
            textSize = 16f
        }
        val timesPicker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 30
            value = 5
        }

        val daysLabel = TextView(this).apply {
            text = "Days:"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
        }
        val daysPicker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 90
            value = 7
        }

        layout.addView(timesLabel)
        layout.addView(timesPicker)
        layout.addView(daysLabel)
        layout.addView(daysPicker)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                onResult(timesPicker.value, daysPicker.value)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showColorPicker() {
        val colors = arrayOf("#64B5F6", "#EF5350", "#66BB6A", "#FFA726", "#AB47BC")
        val colorNames = arrayOf("Blue", "Red", "Green", "Orange", "Purple")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Color")
            .setItems(colorNames) { _, which ->
                selectedColor = colors[which]
                colorPreview.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))
            }
            .show()
    }

    private fun loadRoutineData() {
        val routine = InMemoryDB.routines.find { it.id == editingRoutineId } ?: return
        inputName.setText(routine.name)
        inputQuestion.setText(routine.question)
        inputUnit.setText(routine.unit)
        inputTarget.setText(routine.target)
        inputNotes.setText(routine.notes)
        reminderHour = routine.reminderHour
        reminderMinute = routine.reminderMinute
        frequencyType = routine.frequencyType
        frequencyValue1 = routine.frequencyValue1
        frequencyValue2 = routine.frequencyValue2
        selectedColor = routine.color
        targetType = routine.targetType
        colorPreview.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))

        // Fixed: Use setText() instead of direct text assignment
        val frequencyText = when(frequencyType) {
            "daily" -> "Every day"
            "every_x_days" -> "Every $frequencyValue1 days"
            "times_per_week" -> "$frequencyValue1 times per week"
            "times_per_month" -> "$frequencyValue1 times per month"
            "times_in_days" -> "$frequencyValue1 times in $frequencyValue2 days"
            else -> "Every day"
        }
        btnFrequency.setText(frequencyText)

        inputTargetType.text = if (targetType == "at_least") "At least" else "At most"
        updateReminderText()
    }

    private fun saveRoutine() {
        val name = inputName.text.toString().trim()
        val question = inputQuestion.text.toString().trim()
        val unit = inputUnit.text.toString().trim()
        val target = inputTarget.text.toString().trim()
        val notes = inputNotes.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter routine name", Toast.LENGTH_SHORT).show()
            return
        }
        if (question.isEmpty()) {
            Toast.makeText(this, "Enter question", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingRoutineId != -1L) {
            // Edit existing
            val index = InMemoryDB.routines.indexOfFirst { it.id == editingRoutineId }
            if (index != -1) {
                val old = InMemoryDB.routines[index]
                InMemoryDB.routines[index] = old.copy(
                    name = name,
                    question = question,
                    unit = unit,
                    target = target,
                    notes = notes,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                    frequencyType = frequencyType,
                    frequencyValue1 = frequencyValue1,
                    frequencyValue2 = frequencyValue2,
                    color = selectedColor,
                    targetType = targetType
                )
            }
        } else {
            // Create new
            val routine = Routine(
                name = name,
                question = question,
                isMeasurable = true,
                color = selectedColor,
                unit = unit,
                target = target,
                notes = notes,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                frequencyType = frequencyType,
                frequencyValue1 = frequencyValue1,
                frequencyValue2 = frequencyValue2,
                targetType = targetType
            )
            InMemoryDB.routines.add(routine)
        }

        RoutineRepository.save(this)
        finish()
    }
}