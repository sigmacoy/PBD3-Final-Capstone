package com.example.pbd3_final_capstone.screens.home

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository
import java.util.Locale
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton


class CreateMeasurableActivity : AppCompatActivity() {
    private lateinit var inputName: EditText
    private lateinit var inputQuestion: EditText
    private lateinit var inputUnit: EditText
    private lateinit var inputTarget: EditText
    private lateinit var inputNotes: EditText
    private lateinit var btnFrequency: TextView
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
            val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_time, null)
            val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
            val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
            val btnOk = dialogView.findViewById<TextView>(R.id.btnOk)

            // Set initial time
            timePicker.hour = reminderHour
            timePicker.minute = reminderMinute

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnOk.setOnClickListener {
                reminderHour = timePicker.hour
                reminderMinute = timePicker.minute
                updateReminderText()
                dialog.dismiss()
            }

            dialog.show()

            // Makes the corners transparent if your root layout has rounded corners
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
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

        val adapter = ArrayAdapter<String>(
            this,
            R.layout.dialog_list_item,
            items
        )

        val dialog = AlertDialog.Builder(this)
            .setAdapter(adapter) { _, which ->
                targetType = if (which == 0) "at_least" else "at_most"
                inputTargetType.text = items[which]
            }
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2A2A2A")))
    }

    //    When Create Measurable, user will only pick these three choices...
    private fun showFrequencyDialog() {
        val items = arrayOf("Every day", "Every week", "Every month")

        val adapter = ArrayAdapter<String>(
            this,
            R.layout.dialog_list_item,
            items
        )

        val dialog = AlertDialog.Builder(this)
            .setAdapter(adapter) { _, which ->
                when (which) {
                    0 -> {
                        frequencyType = "daily"
                        btnFrequency.setText("Every day")
                    }
                    1 -> {
                        frequencyType = "weekly"
                        btnFrequency.setText("Every week")
                    }
                    2 -> {
                        frequencyType = "monthly"
                        btnFrequency.setText("Every month")
                    }
                }
            }
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2A2A2A")))
    }

    private fun showColorPicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Helper function to handle color selection
        fun setColor(colorHex: String) {
            selectedColor = colorHex
            colorPreview.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnColorBlue).setOnClickListener { setColor("#64B5F6") }
        dialogView.findViewById<Button>(R.id.btnColorRed).setOnClickListener { setColor("#EF5350") }
        dialogView.findViewById<Button>(R.id.btnColorGreen).setOnClickListener { setColor("#66BB6A") }
        dialogView.findViewById<Button>(R.id.btnColorOrange).setOnClickListener { setColor("#FFA726") }
        dialogView.findViewById<Button>(R.id.btnColorPurple).setOnClickListener { setColor("#AB47BC") }

        dialog.show()

        // Optional: Make dialog corners transparent if you added rounded corners to your root layout
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
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