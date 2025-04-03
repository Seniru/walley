package com.seniru.walley.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatTime(utcMillis: Long): String {
    val date = Date(utcMillis)
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    format.timeZone = TimeZone.getDefault()
    return format.format(date)
}