package com.seniru.walley.utils

import android.content.Context


fun dpToPixels(dps: Int, context: Context): Float {
    val scale: Float = context.resources.displayMetrics.density
    return (dps * scale + 0.5f)
}