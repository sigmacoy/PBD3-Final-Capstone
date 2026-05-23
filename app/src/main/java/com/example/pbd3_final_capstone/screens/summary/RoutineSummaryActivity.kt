package com.example.pbd3_final_capstone.screens.summary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.model.Routine
import com.example.pbd3_final_capstone.data.repository.RoutineRepositoryImpl
import com.example.pbd3_final_capstone.screens.create_routine.CreateMeasurableActivity
import com.example.pbd3_final_capstone.screens.create_routine.CreateYesNoActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RoutineSummaryActivity : AppCompatActivity(), SummaryContract.View {

    private lateinit var presenter: SummaryPresenter
    private var routineId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        presenter = SummaryPresenter(this)
        routineId = intent.getLongExtra("routine_id", -1L)

        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        presenter.loadRoutine(routineId)
    }

    private fun setupButtons() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { presenter.onBackClicked() }
        findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener { presenter.onEditClicked() }
        findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener { presenter.onDeleteClicked() }
    }

    override fun displayRoutine(routine: Routine, frequencyText: String, reminderTime: String) {
        findViewById<TextView>(R.id.summary_name).text = routine.name

        val colorView = findViewById<View>(R.id.viewRoutineColor)
        try {
            colorView.setBackgroundColor(Color.parseColor(routine.color))
        } catch (e: Exception) {
            colorView.setBackgroundColor(Color.parseColor("#64B5F6"))
        }

        findViewById<TextView>(R.id.summary_type).text = if (routine.isMeasurable) "Measurable" else "Yes / No"

        val questionView = findViewById<TextView>(R.id.summary_question)
        questionView.text = if (routine.question.isNotEmpty()) routine.question else "N/A"

        val unitLabel = findViewById<TextView>(R.id.summary_unit_label)
        val unitText = findViewById<TextView>(R.id.summary_unit)
        val targetText = findViewById<TextView>(R.id.summary_target)

        if (routine.isMeasurable) {
            unitLabel.visibility = View.VISIBLE
            unitText.visibility = View.VISIBLE
            unitText.text = if (routine.unit.isNotEmpty()) routine.unit else "N/A"
            targetText.text = "${routine.target} ${routine.unit}"
        } else {
            unitLabel.visibility = View.GONE
            unitText.visibility = View.GONE
            targetText.text = if (routine.targetType == "at_most") "At most" else "At least"
        }

        findViewById<TextView>(R.id.summary_frequency).text = frequencyText
        findViewById<TextView>(R.id.summary_reminder).text = reminderTime

        val notesLabel = findViewById<TextView>(R.id.summary_notes_label)
        val notesText = findViewById<TextView>(R.id.summary_notes)
        if (routine.notes.isNotEmpty()) {
            notesLabel.visibility = View.VISIBLE
            notesText.visibility = View.VISIBLE
            notesText.text = routine.notes
        } else {
            notesLabel.visibility = View.GONE
            notesText.visibility = View.GONE
        }
    }

    override fun closeScreen() {
        finish()
    }

    override fun navigateToEditMeasurable(routineId: Long) {
        val intent = Intent(this, CreateMeasurableActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("routine_id", routineId)
        }
        startActivity(intent)
    }

    override fun navigateToEditYesNo(routineId: Long) {
        val intent = Intent(this, CreateYesNoActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("routine_id", routineId)
        }
        startActivity(intent)
    }

    override fun showDeleteConfirmation(routineName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirm, null)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnDeleteConfirm = dialogView.findViewById<TextView>(R.id.btnDeleteConfirm)

        dialogMessage.text = "Are you sure to delete '$routineName' routine?"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDeleteConfirm.setOnClickListener {
            presenter.confirmDelete(routineId)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
    }

    override fun saveDatabaseUpdates() {
        RoutineRepositoryImpl().save(this)
    }

    override fun drawHistoryCalendar(routine: Routine) {
        val colorMap = mapOf(
            "red" to "#F44336", "orange" to "#FF9800", "yellow" to "#FFEB3B",
            "green" to "#4CAF50", "blue" to "#2196F3", "purple" to "#9C27B0"
        )

        val resolvedColor = try {
            Color.parseColor(colorMap[routine.color] ?: routine.color)
        } catch (e: Exception) { Color.BLUE }

        val W = 660
        val H = 420
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        canvas.drawColor(Color.parseColor("#1E1E1E"))

        val weeks = 10
        val numDays = 7
        val padL = 44f
        val padT = 26f
        val padB = 32f
        val cellW = (W - padL) / weeks
        val cellH = (H - padT - padB) / numDays

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY; textSize = 20f; typeface = Typeface.DEFAULT }
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 17f; textAlign = Paint.Align.CENTER }
        val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = resolvedColor }
        val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3A3A3A") }
        val todayRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f }

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFmt = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFmt = SimpleDateFormat("d", Locale.getDefault())

        val checkedDates = mutableSetOf<String>()
        for (offset in 0..3) {
            val isDone = if (routine.isMeasurable) {
                val currentCount = routine.inputValues[offset].toIntOrNull() ?: 0
                val target = routine.target.toIntOrNull() ?: 1
                currentCount >= target
            } else routine.checkStates[offset]

            if (isDone) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, offset - 3)
                checkedDates.add(fmt.format(cal.time))
            }
        }

        val todayStr = fmt.format(Calendar.getInstance().time)
        val anchorSunday = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            add(Calendar.WEEK_OF_YEAR, -(weeks - 1))
        }

        var lastMonth = ""
        for (col in 0 until weeks) {
            val weekStart = (anchorSunday.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, col) }
            val month = monthFmt.format(weekStart.time)
            if (month != lastMonth) {
                canvas.drawText(month, padL + col * cellW + 4f, 20f, labelPaint)
                lastMonth = month
            }
        }

        val dayLabels = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
        dayLabels.forEachIndexed { row, label ->
            val y = padT + row * cellH + cellH * 0.66f
            canvas.drawText(label, 0f, y, labelPaint)
        }

        val today = Calendar.getInstance()
        val radius = minOf(cellW, cellH) * 0.4f

        for (col in 0 until weeks) {
            for (row in 0 until numDays) {
                val cellCal = (anchorSunday.clone() as Calendar).apply {
                    add(Calendar.WEEK_OF_YEAR, col)
                    add(Calendar.DAY_OF_YEAR, row)
                }

                if (cellCal.after(today)) continue

                val dateStr = fmt.format(cellCal.time)
                val isChecked = checkedDates.contains(dateStr)
                val isToday = dateStr == todayStr

                val cx = padL + col * cellW + cellW / 2
                val cy = padT + row * cellH + cellH / 2
                val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

                canvas.drawRoundRect(rect, 8f, 8f, if (isChecked) filledPaint else emptyPaint)
                if (isToday) canvas.drawRoundRect(rect, 8f, 8f, todayRingPaint)

                datePaint.color = if (isChecked) Color.WHITE else Color.GRAY
                canvas.drawText(dayFmt.format(cellCal.time), cx, cy + 6f, datePaint)
            }
        }

        findViewById<ImageView>(R.id.widget_history_canvas).setImageBitmap(bmp)
        findViewById<TextView>(R.id.widget_history_label).text = "History"
    }
}