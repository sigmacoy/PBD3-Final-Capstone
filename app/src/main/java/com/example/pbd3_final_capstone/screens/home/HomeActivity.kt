package com.example.pbd3_final_capstone.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.data.RoutineRepository
import com.example.pbd3_final_capstone.utils.ReminderScheduler
import com.example.pbd3_final_capstone.widget.CheckmarkWidget
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var presenter: HomePresenter
    private lateinit var tableAdapter: RoutineTableAdapter

    private val widgetRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            presenter.loadRoutines()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        requestNotificationPermission()
        ReminderScheduler.createChannel(this)
        ReminderScheduler.scheduleMidnightReset(this)

        presenter = HomePresenter(this, this)
        tableAdapter = RoutineTableAdapter(
            activity = this,
            onRoutineClick = { routine ->
                val intent = Intent(this, RoutineSummaryActivity::class.java)
                intent.putExtra("routine_id", routine.id)
                startActivity(intent)
            },
            onDataChanged = { WidgetUpdater.updateAllWidgets(this) }
        )
        tableAdapter.bindViews()

        findViewById<ImageButton>(R.id.btnAdd).setOnClickListener { showTypeDialog() }
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener { showSortDialog() }

        presenter.loadRoutines()
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
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
            dialog.dismiss()
            startActivity(Intent(this, CreateYesNoActivity::class.java))
        }

        view.findViewById<View>(R.id.cardMeasurable).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, CreateMeasurableActivity::class.java))
        }

        dialog.setContentView(view)
        dialog.show()
    }

    override fun showCreateDialog(isMeasurable: Boolean) {
        // This method is kept for contract but not used
    }

    override fun showSortDialog() {
        val sortButton = findViewById<View>(R.id.btnSort)

        // Force a true dark theme overlay onto the popup context
        val themedContext = android.view.ContextThemeWrapper(this, R.style.CustomPopupMenuTheme)
        val popup = androidx.appcompat.widget.PopupMenu(themedContext, sortButton)

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
        tableAdapter.refresh(routines)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        presenter.loadRoutines()
    }
}