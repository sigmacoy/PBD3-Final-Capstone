package com.example.pbd3_final_capstone.screens.home

import android.content.Context
import android.content.Intent
import com.example.pbd3_final_capstone.data.model.Routine
import com.example.pbd3_final_capstone.screens.create_routine.CreateMeasurableActivity
import com.example.pbd3_final_capstone.screens.create_routine.CreateYesNoActivity

object RoutineDialogHelper {
    fun showCreateDialog(context: Context, type: String) {
        val intent = when(type) {
            "measurable" -> Intent(context, CreateMeasurableActivity::class.java)
            "yesno" -> Intent(context, CreateYesNoActivity::class.java)
            else -> return
        }
        context.startActivity(intent)
    }

    fun showEditDialog(context: Context, routine: Routine) {
        val intent = if (routine.isMeasurable) {
            Intent(context, CreateMeasurableActivity::class.java)
        } else {
            Intent(context, CreateYesNoActivity::class.java)
        }
        intent.putExtra("edit_mode", true)
        intent.putExtra("routine_id", routine.id)
        context.startActivity(intent)
    }
}