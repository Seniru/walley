package com.seniru.walley.utils

import android.content.Context
import android.icu.text.NumberFormat
import com.seniru.walley.persistence.SharedMemory

fun formatCurrency(amount: Float, context: Context): String {
    val sharedMemory = SharedMemory.getInstance(context)
    val preferredCurrency = sharedMemory.getCurrency()
    val format = NumberFormat.getCurrencyInstance().apply {
        currency = preferredCurrency
    }
    return format.format(amount)
}
