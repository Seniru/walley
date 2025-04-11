package com.seniru.walley

import WalleyNotificationManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.toColor
import androidx.core.view.setPadding
import com.seniru.walley.models.Category
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.ValidationResult
import java.util.Calendar
import java.util.Date

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class CreateTransactionDialog(
    context: Context, private val onTransactionAdded: () -> Unit
) : AlertDialog(context) {

    private val transactionStore = TransactionDataStore.getInstance(context)
    private val preferences = SharedMemory.getInstance(context)

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun create() {
        super.create()
        val titleTextView = findViewById<TextView>(R.id.transactionTitle)
        val amountTextView = findViewById<TextView>(R.id.transactionAmount)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val dateView = findViewById<CalendarView>(R.id.calendar)
        val submitButton = findViewById<Button>(R.id.submitButton)

        if (categorySpinner != null) {
            setCategories(categorySpinner)
        }

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
                    preferences.setBalance(
                        preferences.getBalance() + (transaction.amount
                            ?: 0f) * (if (transaction.type == "income") 1 else -1)
                    )
                    notifyIfOverBudget()

                    dismiss()
                    LiveDataEventBus.sendEvent("refresh_transactions")
                }
            }

        }

    }

    private fun setCategories(spinner: Spinner) {
        val categories =
            CategoryDataStore.getInstance(context).readAll().map { it.name }.toMutableList()
        categories.add(0, "Other")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notifyIfOverBudget() {
        val monthlyBudget = preferences.getMonthlyBudget()
        val transactions = transactionStore.readLastMonth()
        val total = transactions.filter { it.type == "expense" }.map { it.amount ?: 0.0f }
            .reduceOrNull { total, amount -> total + amount } ?: 0f

        val today = android.icu.util.Calendar.getInstance().apply {
            time = Date()
        }
        val startOfDay = today.apply {
            set(android.icu.util.Calendar.HOUR_OF_DAY, 0)
            set(android.icu.util.Calendar.MINUTE, 0)
            set(android.icu.util.Calendar.SECOND, 0)
            set(android.icu.util.Calendar.MILLISECOND, 0)
        }.time
        val endOfDay = today.apply {
            set(android.icu.util.Calendar.HOUR_OF_DAY, 23)
            set(android.icu.util.Calendar.MINUTE, 59)
            set(android.icu.util.Calendar.SECOND, 59)
        }.time
        val todayTransactions = transactionStore.read(startOfDay, endOfDay)

        if (todayTransactions.size == 1 && total >= monthlyBudget * 0.75) {
            val currency = preferences.getCurrency().currencyCode
            val message =
                if (total > monthlyBudget)
                    "You went $currency${total - monthlyBudget} over your $currency$monthlyBudget budget."
                else "You’re nearing your budget! Just $currency${monthlyBudget - total} remains out of $currency$monthlyBudget"

            WalleyNotificationManager.createNotification(
                context,
                Intent(context, MainActivity::class.java),
                "Budget limit alert!",
                message
            )
        }
    }

}