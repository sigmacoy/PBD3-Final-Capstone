package com.example.pbd3_final_capstone.utils

import android.graphics.Color

object ColorHelper {
    val colorMap = mapOf(
        "red" to "#F44336",
        "orange" to "#FF9800",
        "yellow" to "#FFEB3B",
        "green" to "#4CAF50",
        "blue" to "#2196F3",
        "purple" to "#9C27B0"
    )

    fun resolveColor(colorStr: String): Int {
        val hex = colorMap[colorStr.lowercase().trim()] ?: colorStr
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.WHITE
        }
    }

    fun buildCircleDrawable(
        context: android.content.Context,
        hexColor: String,
        isSelected: Boolean
    ): android.graphics.drawable.Drawable {
        val color = try {
            Color.parseColor(hexColor)
        } catch (e: Exception) {
            Color.WHITE
        }
        val dp = context.resources.displayMetrics.density
        return if (isSelected) {
            val outer = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.WHITE)
            }
            val inner = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(color)
            }
            val inset = (4 * dp).toInt()
            android.graphics.drawable.LayerDrawable(arrayOf(outer, inner)).also {
                it.setLayerInset(1, inset, inset, inset, inset)
            }
        } else {
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(color)
            }
        }
    }
}