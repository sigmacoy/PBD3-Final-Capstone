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
import com.example.pbd3_final_capstone.screens.home.Routine
import com.example.pbd3_final_capstone.screens.home.InMemoryDB
import java.util.Locale
import android.view.View
import com.google.android.material.button.MaterialButton


class CreateYesNoActivity : AppCompatActivity() {
    private lateinit var inputName: EditText
    private lateinit var inputQuestion: EditText
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
        setContentView(R.layout.activity_create_yesno)

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
            val picker = com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setHour(reminderHour)
                .setMinute(reminderMinute)
                .setTitleText("Select Reminder Time")
                .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()

            picker.addOnPositiveButtonClickListener {
                reminderHour = picker.hour
                reminderMinute = picker.minute
                updateReminderText()
            }

            picker.show(supportFragmentManager, "time_picker")
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

    //    When clicking btnFrequency, it will open an AlertDialog with this layout/dialog_freq_opt_yesno.xml file
    private fun showFrequencyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_freq_opt_yesno, null)

        val radioEveryDay = dialogView.findViewById<RadioButton>(R.id.radio_every_day)
        val radioEveryXDays = dialogView.findViewById<RadioButton>(R.id.radio_every_x_days)
        val radioTimesPerWeek = dialogView.findViewById<RadioButton>(R.id.radio_times_per_week)
        val radioTimesPerMonth = dialogView.findViewById<RadioButton>(R.id.radio_times_per_month)
        val radioTimesInDays = dialogView.findViewById<RadioButton>(R.id.radio_times_in_days)

        val editEveryXDays = dialogView.findViewById<EditText>(R.id.edit_every_x_days)
        val editTimesPerWeek = dialogView.findViewById<EditText>(R.id.edit_times_per_week)
        val editTimesPerMonth = dialogView.findViewById<EditText>(R.id.edit_times_per_month)
        val editTimesInDaysX = dialogView.findViewById<EditText>(R.id.edit_times_in_days_x)
        val editTimesInDaysY = dialogView.findViewById<EditText>(R.id.edit_times_in_days_y)

        val btnSaveDialog = dialogView.findViewById<TextView>(R.id.btn_save)

        val radios = listOf(
            radioEveryDay,
            radioEveryXDays,
            radioTimesPerWeek,
            radioTimesPerMonth,
            radioTimesInDays
        )

        fun selectRadio(selected: RadioButton) {
            radios.forEach { it.isChecked = false }
            selected.isChecked = true
        }

        radioEveryDay.setOnClickListener { selectRadio(radioEveryDay) }
        radioEveryXDays.setOnClickListener { selectRadio(radioEveryXDays) }
        radioTimesPerWeek.setOnClickListener { selectRadio(radioTimesPerWeek) }
        radioTimesPerMonth.setOnClickListener { selectRadio(radioTimesPerMonth) }
        radioTimesInDays.setOnClickListener { selectRadio(radioTimesInDays) }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSaveDialog.setOnClickListener {

            when {
                radioEveryDay.isChecked -> {
                    frequencyType = "daily"
                    btnFrequency.setText("Every day")
                }

                radioEveryXDays.isChecked -> {
                    frequencyType = "every_x_days"
                    frequencyValue1 =
                        editEveryXDays.text.toString().toIntOrNull() ?: 3

                    btnFrequency.setText("Every $frequencyValue1 days")
                }

                radioTimesPerWeek.isChecked -> {
                    frequencyType = "times_per_week"
                    frequencyValue1 =
                        editTimesPerWeek.text.toString().toIntOrNull() ?: 3

                    btnFrequency.setText("$frequencyValue1 times per week")
                }

                radioTimesPerMonth.isChecked -> {
                    frequencyType = "times_per_month"
                    frequencyValue1 =
                        editTimesPerMonth.text.toString().toIntOrNull() ?: 10

                    btnFrequency.setText("$frequencyValue1 times per month")
                }

                radioTimesInDays.isChecked -> {
                    frequencyType = "times_in_days"

                    frequencyValue1 =
                        editTimesInDaysX.text.toString().toIntOrNull() ?: 3

                    frequencyValue2 =
                        editTimesInDaysY.text.toString().toIntOrNull() ?: 10

                    btnFrequency.setText(
                        "$frequencyValue1 times in $frequencyValue2 days"
                    )
                }
            }

            dialog.dismiss()
        }

        dialog.show()
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
        inputNotes.setText(routine.notes)
        reminderHour = routine.reminderHour
        reminderMinute = routine.reminderMinute
        frequencyType = routine.frequencyType
        frequencyValue1 = routine.frequencyValue1
        frequencyValue2 = routine.frequencyValue2
        selectedColor = routine.color
        targetType = routine.targetType ?: "at_least"
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
        val notes = inputNotes.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter routine name", Toast.LENGTH_SHORT).show()
            return
        }

        if (editingRoutineId != -1L) {
            val index = InMemoryDB.routines.indexOfFirst { it.id == editingRoutineId }
            if (index != -1) {
                val old = InMemoryDB.routines[index]
                InMemoryDB.routines[index] = old.copy(
                    name = name,
                    question = question,
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
            val routine = Routine(
                name = name,
                question = question,
                isMeasurable = false,
                color = selectedColor,
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