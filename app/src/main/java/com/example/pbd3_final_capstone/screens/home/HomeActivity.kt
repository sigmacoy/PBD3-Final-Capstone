package com.example.pbd3_final_capstone.screens.home

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import android.widget.PopupMenu
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.Gravity
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.pbd3_final_capstone.screens.home.HomeContract
import android.view.View

class HomeActivity : AppCompatActivity(), HomeContract.View {
    private lateinit var presenter: HomePresenter
    private lateinit var tableHabits: TableLayout

    private val colorMap = mapOf(
        "red"    to "#F44336",
        "orange" to "#FF9800",
        "yellow" to "#FFEB3B",
        "green"  to "#4CAF50",
        "blue"   to "#2196F3",
        "purple" to "#9C27B0"
    )

    private fun resolveColor(colorStr: String): Int {
        val hex = colorMap[colorStr.lowercase().trim()] ?: colorStr
        return try { Color.parseColor(hex) } catch (e: Exception) { Color.WHITE }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        presenter = HomePresenter(this)
        tableHabits = findViewById(R.id.tableRoutines)

        findViewById<ImageButton>(R.id.btnAdd).setOnClickListener { showTypeDialog() }
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener { showSortDialog() }

        presenter.loadRoutines()
    }

    override fun showTypeDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_type, null)

        view.findViewById<View>(R.id.cardYesNo).setOnClickListener {
            dialog.dismiss()
            showCreateDialog(false)
        }
        view.findViewById<View>(R.id.cardMeasurable).setOnClickListener {
            dialog.dismiss()
            showCreateDialog(true)
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

        val colorLabel = TextView(this).apply {
            text = "Pick a color"
            setTextColor(Color.DKGRAY)
            setPadding(0, 16, 0, 8)
        }
        layout.addView(colorLabel)

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
        }

        var selectedColor = "red"
        val circleViews = mutableMapOf<String, View>()
        val dp = resources.displayMetrics.density
        val size   = (32 * dp).toInt()
        val margin = (8  * dp).toInt()

        colorMap.forEach { (name, hex) ->
            val circle = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply { setMargins(margin, 0, margin, 0) }
                background = buildCircleDrawable(hex, isSelected = (name == selectedColor))
            }
            circle.setOnClickListener {
                circleViews[selectedColor]?.background = buildCircleDrawable(colorMap[selectedColor]!!, isSelected = false)
                selectedColor = name
                circle.background = buildCircleDrawable(hex, isSelected = true)
            }
            circleViews[name] = circle
            colorRow.addView(circle)
        }
        circleViews["red"]?.background = buildCircleDrawable(colorMap["red"]!!, isSelected = true)
        layout.addView(colorRow)

        AlertDialog.Builder(this)
            .setTitle(if (isMeasurable) "Create Measurable" else "Create Yes / No")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                presenter.addRoutine(Routine(
                    name         = inputName.text.toString(),
                    question     = inputQuestion.text.toString(),
                    isMeasurable = isMeasurable,
                    color        = selectedColor,
                    unit         = inputUnit?.text?.toString() ?: "",
                    target       = inputTarget?.text?.toString() ?: ""
                ))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun persistUIState() {
        val table = findViewById<TableLayout>(R.id.tableRoutines)

        // Skip header row (index 0)
        for (i in 1 until table.childCount) {
            val row = table.getChildAt(i) as TableRow
            val routine = InMemoryDB.routines[i - 1]

            for (j in 1 until row.childCount) {
                val cell = row.getChildAt(j)

                if (routine.isMeasurable) {
                    val input = cell as EditText
                    routine.inputValues[j - 1] = input.text.toString()
                } else {
                    val container = cell as LinearLayout
                    val checkBox = container.getChildAt(0) as CheckBox
                    routine.checkStates[j - 1] = checkBox.isChecked
                }
            }
        }
    }

    override fun showSortDialog() {
        val sortButton = findViewById<View>(R.id.btnSort)
        val popup = PopupMenu(this, sortButton)

        popup.menu.add(0, 0, 0, "Sort").isEnabled = false
        popup.menu.add(0, 1, 1, "By name")
        popup.menu.add(0, 2, 2, "By color")

        popup.setOnMenuItemClickListener { item ->
            persistUIState()
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
            val cellLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(cellSize, cellSize)
            }
            cellLayout.addView(TextView(this).apply {
                text = dayText; textSize = 10f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER
            })
            cellLayout.addView(TextView(this).apply {
                text = dateText; textSize = 12f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            headerRow.addView(cellLayout)
        }
        tableRoutines.addView(headerRow)

        // ── Data rows ────────────────────────────────────────────────────────
        routines.forEach { routine ->
            val resolvedColor = resolveColor(routine.color)
            val row = TableRow(this)

            row.addView(TextView(this).apply {
                text = "${routine.name}\n${if (routine.isMeasurable) routine.unit else ""}"
                setTextColor(resolvedColor)
                setPadding(16, 25, 16, 0)
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
                        setSelection(text.length)
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
                        override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
                        override fun afterTextChanged(s: Editable?) {
                            val v = s?.toString() ?: ""
                            routine.inputValues[idx] = v          // ← persist into routine
                            input.setTextColor(
                                if ((v.toIntOrNull() ?: 0) > 0) resolvedColor else Color.DKGRAY
                            )
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
                        isChecked = routine.checkStates[i]        // ← restore saved state
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
                        routine.checkStates[idx] = isChecked      // ← persist into routine
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
            val outerRing   = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(Color.WHITE)
            }
            val innerCircle = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(color)
            }
            val inset = (4 * dp).toInt()
            android.graphics.drawable.LayerDrawable(arrayOf(outerRing, innerCircle)).also {
                it.setLayerInset(1, inset, inset, inset, inset)
            }
        } else {
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(color)
            }
        }
    }
}