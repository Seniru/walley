package com.seniru.walley

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.setPadding
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.ValidationResult
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
class CreateTransactionDialog(
    context: Context, private val onTransactionAdded: () -> Unit
) : AlertDialog(context) {

    private val transactionStore = TransactionDataStore.getInstance(context)

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun create() {
        super.create()
        val titleTextView = findViewById<TextView>(R.id.transactionTitle)
        val amountTextView = findViewById<TextView>(R.id.transactionAmount)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val dateView = findViewById<CalendarView>(R.id.calendar)
        val submitButton = findViewById<Button>(R.id.submitButton)

        dateView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            dateView.date = Calendar.getInstance()
                .apply { set(year, month, dayOfMonth) }
                .timeInMillis
        }

        submitButton?.setOnClickListener {
            val transaction = Transaction(
                titleTextView?.text.toString(),
                amountTextView?.text.toString().toFloatOrNull(),
                typeSpinner?.selectedItem.toString().lowercase(),
                categorySpinner?.selectedItem.toString(),
                dateView?.date!!
            )

            when (val validationResult = transaction.validate()) {
                is ValidationResult.Empty -> Toast.makeText(
                    this.context,
                    validationResult.error,
                    Toast.LENGTH_SHORT
                ).show()

                is ValidationResult.Invalid -> Toast.makeText(
                    this.context,
                    validationResult.error,
                    Toast.LENGTH_SHORT
                ).show()

                else -> {
                    transactionStore.push(transaction)
                    dismiss()
                    LiveDataEventBus.sendEvent("refresh_transactions")

                }
            }

        }

    }

}