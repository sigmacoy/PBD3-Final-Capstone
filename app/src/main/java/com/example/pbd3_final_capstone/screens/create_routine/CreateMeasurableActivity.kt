package com.example.pbd3_final_capstone.screens.create_routine

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.google.android.material.button.MaterialButton

class CreateMeasurableActivity : AppCompatActivity(), CreateRoutineContract.View {
    private lateinit var presenter: CreateRoutinePresenter

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
    private lateinit var headerTitle: TextView

    private var editingRoutineId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_measurable)

        presenter = CreateRoutinePresenter(this)
        initViews()
        setupListeners()

        if (intent.getBooleanExtra("edit_mode", false)) {
            editingRoutineId = intent.getLongExtra("routine_id", -1L)
            headerTitle.text = "Edit Routine"
            presenter.loadRoutine(editingRoutineId)
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
        headerTitle = findViewById(R.id.headerTitle)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnReminder.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_reminder_time, null)
            val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()

            dialogView.findViewById<TextView>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
            dialogView.findViewById<TextView>(R.id.btnOk).setOnClickListener {
                presenter.setReminder(timePicker.hour, timePicker.minute)
                dialog.dismiss()
            }
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        btnFrequency.setOnClickListener {
            val items = arrayOf("Every day", "Every week", "Every month")
            val adapter = ArrayAdapter(this, R.layout.dialog_list_item, items)
            val dialog = AlertDialog.Builder(this)
                .setAdapter(adapter) { _, which -> presenter.setFrequencyMeasurable(which) }
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2A2A2A")))
        }

        inputTargetType.setOnClickListener {
            val items = arrayOf("At least", "At most")
            val adapter = ArrayAdapter(this, R.layout.dialog_list_item, items)
            val dialog = AlertDialog.Builder(this)
                .setAdapter(adapter) { _, which -> presenter.setTargetType(which == 0) }
                .create()
            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2A2A2A")))
        }

        colorPreview.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setView(dialogView).create()

            fun bindColorBtn(id: Int, hex: String) {
                dialogView.findViewById<Button>(id).setOnClickListener {
                    presenter.setColor(hex)
                    dialog.dismiss()
                }
            }
            bindColorBtn(R.id.btnColorBlue, "#64B5F6")
            bindColorBtn(R.id.btnColorRed, "#EF5350")
            bindColorBtn(R.id.btnColorGreen, "#66BB6A")
            bindColorBtn(R.id.btnColorOrange, "#FFA726")
            bindColorBtn(R.id.btnColorPurple, "#AB47BC")

            dialog.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        btnSave.setOnClickListener {
            presenter.saveRoutine(
                routineId = editingRoutineId,
                isMeasurable = true,
                name = inputName.text.toString().trim(),
                question = inputQuestion.text.toString().trim(),
                notes = inputNotes.text.toString().trim(),
                unit = inputUnit.text.toString().trim(),
                target = inputTarget.text.toString().trim()
            )
        }
    }

    override fun displayRoutineData(name: String, question: String, notes: String, color: String, targetTypeDisplay: String, frequencyDisplay: String, reminderDisplay: String, unit: String, target: String) {
        inputName.setText(name)
        inputQuestion.setText(question)
        inputNotes.setText(notes)
        inputUnit.setText(unit)
        inputTarget.setText(target)
        updateColorDisplay(color)
        updateTargetTypeDisplay(targetTypeDisplay)
        updateFrequencyDisplay(frequencyDisplay)
        updateReminderDisplay(reminderDisplay)
    }

    override fun updateReminderDisplay(text: String) { btnReminder.text = text }
    override fun updateFrequencyDisplay(text: String) { btnFrequency.text = text }
    override fun updateColorDisplay(colorHex: String) { colorPreview.setBackgroundColor(Color.parseColor(colorHex)) }
    override fun updateTargetTypeDisplay(text: String) { inputTargetType.text = text }
    override fun showError(message: String) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }

    override fun finishAndSave() {
        RoutineRepositoryImpl().save(this)
        finish()
    }
}