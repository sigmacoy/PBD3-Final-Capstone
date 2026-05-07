package com.example.pbd3_final_capstone.screens.home
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository
import java.util.Locale

class RoutineEditorActivity : AppCompatActivity() {
    private lateinit var inputName: EditText
    private lateinit var inputQuestion: EditText
    private lateinit var inputUnit: EditText
    private lateinit var inputTarget: EditText
    private lateinit var inputNotes: EditText
    private lateinit var btnFrequency: Button
    private lateinit var btnReminder: Button
    private lateinit var btnSave: Button
    private var reminderHour = 8
    private var reminderMinute = 0
    private var frequencyType = "daily"
    private var frequencyValue1 = 1
    private var frequencyValue2 = 1
    private var selectedColor = "red"
    private var editingRoutineId: Long = -1L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine_editor)
        inputName = findViewById(R.id.inputName)
        inputQuestion = findViewById(R.id.inputQuestion)
        inputUnit = findViewById(R.id.inputUnit)
        inputTarget = findViewById(R.id.inputTarget)
        inputNotes = findViewById(R.id.inputNotes)
        btnFrequency = findViewById(R.id.btnFrequency)
        btnReminder = findViewById(R.id.btnReminder)
        btnSave = findViewById(R.id.btnSave)
        val isEditMode = intent.getBooleanExtra("edit_mode", false)
        if (isEditMode) {
            editingRoutineId = intent.getLongExtra("routine_id", -1L)
            loadRoutineData()
        }
        btnReminder.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    reminderHour = hour
                    reminderMinute = minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val formattedHour = if (hour % 12 == 0) 12 else hour % 12
                    btnReminder.text = String.format(
                        Locale.getDefault(),
                        "%02d:%02d %s",
                        formattedHour,
                        minute,
                        amPm
                    )
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
            saveRoutine(isEditMode)
        }
    }
    private fun loadRoutineData() {
        val routine = InMemoryDB.routines.find { it.id == editingRoutineId } ?:
        return
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
        btnFrequency.text = frequencyType
        val amPm = if (reminderHour >= 12) "PM" else "AM"
        val formattedHour = if (reminderHour % 12 == 0) 12 else reminderHour %
                12
        btnReminder.text = String.format(
            Locale.getDefault(),
            "%02d:%02d %s",
            formattedHour,
            reminderMinute,
            amPm
        )
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
                        btnFrequency.text = "Every day"
                    }
                    1 -> {
                        frequencyType = "every_x_days"
                        frequencyValue1 = 2
                        btnFrequency.text = "Every 2 days"
                    }
                    2 -> {
                        frequencyType = "times_per_week"
                        frequencyValue1 = 3
                        btnFrequency.text = "3 times per week"
                    }
                    3 -> {
                        frequencyType = "times_per_month"
                        frequencyValue1 = 10
                        btnFrequency.text = "10 times per month"
                    }
                    4 -> {
                        frequencyType = "times_in_days"
                        frequencyValue1 = 5
                        frequencyValue2 = 7
                        btnFrequency.text = "5 times in 7 days"
                    }
                }
            }
            .show()
    }
    private fun saveRoutine(isEditMode: Boolean) {
        val name = inputName.text.toString().trim()
        val question = inputQuestion.text.toString().trim()
        val unit = inputUnit.text.toString().trim()
        val target = inputTarget.text.toString().trim()
        val notes = inputNotes.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter routine name",
                Toast.LENGTH_SHORT).show()
            return
        }
        if (question.isEmpty()) {
            Toast.makeText(this, "Enter question", Toast.LENGTH_SHORT).show()
            return
        }
        if (isEditMode) {
            val oldRoutine = InMemoryDB.routines.find { it.id ==
                    editingRoutineId } ?: return
            val updatedRoutine = oldRoutine.copy(
                name = name,
                question = question,
                unit = unit,
                target = target,
                notes = notes,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                frequencyType = frequencyType,
                frequencyValue1 = frequencyValue1,
                frequencyValue2 = frequencyValue2
            )
            val index = InMemoryDB.routines.indexOfFirst { it.id ==
                    editingRoutineId }
            if (index != -1) {
                InMemoryDB.routines[index] = updatedRoutine
            }
        } else {
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
                frequencyValue2 = frequencyValue2
            )
            InMemoryDB.routines.add(routine)
        }
        RoutineRepository.save(this)
        finish()
    }
}