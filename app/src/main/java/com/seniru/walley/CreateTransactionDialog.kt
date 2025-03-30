package com.seniru.walley

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding

class CreateTransactionDialog(context: Context) : AlertDialog(context) {

    init {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.layout_add_transaction_dialog, null)
                .apply {
                    layoutParams = ViewGroup.MarginLayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    ).apply {
                        setPadding(50)
                    }
                }
        setView(view)
        create()

    }

}