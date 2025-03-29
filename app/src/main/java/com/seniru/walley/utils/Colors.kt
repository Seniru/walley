package com.seniru.walley.utils

import android.graphics.Color

class Colors {
    companion object {
        fun lightenColor(color: Int, factor: Float): Int {
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            val newR = (r + (255 - r) * factor).toInt()
            val newG = (g + (255 - g) * factor).toInt()
            val newB = (b + (255 - b) * factor).toInt()

            return Color.rgb(newR, newG, newB)
        }
    }

}