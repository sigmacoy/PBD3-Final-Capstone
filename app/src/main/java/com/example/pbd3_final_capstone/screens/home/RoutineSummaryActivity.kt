package com.example.pbd3_final_capstone.screens.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.google.android.material.button.MaterialButton
import java.util.Locale

class RoutineSummaryActivity : AppCompatActivity() {

    private var routineId: Long = -1L
    private var routine: Routine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        routineId = intent.getLongExtra("routine_id", -1L)
        loadRoutineData()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        // Reload data in case it was edited
        loadRoutineData()
    }

    private fun loadRoutineData() {
        routine = InMemoryDB.routines.find { it.id == routineId }

        if (routine == null) {
            finish() // Exit if routine not found
            return
        }

        // Bind Data to Views
        findViewById<TextView>(R.id.summary_name).text = routine!!.name

        // Handle Color
        val colorView = findViewById<View>(R.id.viewRoutineColor)
        try {
            colorView.setBackgroundColor(Color.parseColor(routine!!.color))
        } catch (e: Exception) {
            colorView.setBackgroundColor(Color.parseColor("#64B5F6")) // Fallback
        }

        // Type
        findViewById<TextView>(R.id.summary_type).text = if (routine!!.isMeasurable) "Measurable" else "Yes / No"

        // Question
        val questionView = findViewById<TextView>(R.id.summary_question)
        if (routine!!.question.isNotEmpty()) {
            questionView.text = routine!!.question
        } else {
            questionView.text = "N/A"
        }

        // Measurable Specifics (Unit and Target)
        val unitLabel = findViewById<TextView>(R.id.summary_unit_label)
        val unitText = findViewById<TextView>(R.id.summary_unit)
        val targetText = findViewById<TextView>(R.id.summary_target)

        if (routine!!.isMeasurable) {
            unitLabel.visibility = View.VISIBLE
            unitText.visibility = View.VISIBLE
            unitText.text = if (routine!!.unit.isNotEmpty()) routine!!.unit else "N/A"
            targetText.text = "${routine!!.target} ${routine!!.unit}"
        } else {
            unitLabel.visibility = View.GONE
            unitText.visibility = View.GONE
            targetText.text = "N/A"
        }

        // Frequency
        findViewById<TextView>(R.id.summary_frequency).text = buildFrequencyText()

        // Reminder
        findViewById<TextView>(R.id.summary_reminder).text = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            routine!!.reminderHour,
            routine!!.reminderMinute
        )

        // Notes
        val notesLabel = findViewById<TextView>(R.id.summary_notes_label)
        val notesText = findViewById<TextView>(R.id.summary_notes)
        if (routine!!.notes.isNotEmpty()) {
            notesLabel.visibility = View.VISIBLE
            notesText.visibility = View.VISIBLE
            notesText.text = routine!!.notes
        } else {
            notesLabel.visibility = View.GONE
            notesText.visibility = View.GONE
        }
    }

    private fun buildFrequencyText(): String {
        return when (routine!!.frequencyType) {
            "daily" -> "Every day"
            "every_x_days" -> "Every ${routine!!.frequencyValue1} days"
            "x_times_week" -> "${routine!!.frequencyValue1} times per week"
            "x_times_month" -> "${routine!!.frequencyValue1} times per month"
            "x_times_in_y_days" -> "${routine!!.frequencyValue1} times in ${routine!!.frequencyValue2} days"
            else -> "Custom"
        }
    }

    private fun setupButtons() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
            routine?.let {
                val intent = if (it.isMeasurable) {
                    Intent(this, CreateMeasurableActivity::class.java)
                } else {
                    Intent(this, CreateYesNoActivity::class.java)
                }
                intent.putExtra("edit_mode", true)
                intent.putExtra("routine_id", it.id)
                startActivity(intent)
            }
        }

        findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure to delete '${routine?.name}' routine?")
                .setPositiveButton("Delete") { _, _ ->
                    InMemoryDB.routines.removeAll { it.id == routineId }
                    RoutineRepository.save(this)
                    finish() // Close activity after deleting
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}