package com.seniru.walley

import WalleyNotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.core.view.setPadding
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.ValidationResult
import com.seniru.walley.utils.formatCurrency
import java.util.Calendar
import java.util.Date

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class CreateTransactionDialog(
    context: Context,
    var isEditing: Boolean = false,
    var index: Int? = null,
    var title: String? = null,
    var amount: Float? = null,
    var type: String? = null,
    var category: String? = null,
    var date: Long? = null
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
        val heading = findViewById<TextView>(R.id.dialogHeading)
        val submitButton = findViewById<Button>(R.id.submitButton)

        if (categorySpinner != null) {
            setCategories(categorySpinner)
        }

        if (isEditing) {
            heading?.text = context.getString(R.string.edit_transaction)
            submitButton?.text = context.getString(R.string.edit_transaction)
            titleTextView?.text = title
            amountTextView?.text = amount.toString()
            typeSpinner?.setSelection(if (type == "expense") 0 else 1)
        }

        date?.let { dateView?.date = it }
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
                dateView?.date!!,
                index
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
                    if (isEditing) {
                        if (index == null) return@setOnClickListener
                        transactionStore.replace(index!!, transaction)
                        preferences.setBalance(
                            preferences.getBalance() + ((amount ?: 0f) - (transaction.amount
                                ?: 0f)) * (if (transaction.type == "income") 1 else -1)
                        )
                    } else {
                        transactionStore.push(transaction)
                        preferences.setBalance(
                            preferences.getBalance()
                                    + (transaction.amount
                                ?: 0f) * (if (transaction.type == "income") 1 else -1)
                        )
                    }

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
        if (isEditing) {
            spinner.setSelection(categories.indexOf(category))
        } else {
            spinner.setSelection(0)
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notifyIfOverBudget() {
        val monthlyBudget = preferences.getMonthlyBudget()
        val transactions = transactionStore.readLastMonth()
        val total = transactions.filter { it.type == "expense" }.map { it.amount ?: 0.0f }
            .reduceOrNull { total, amount -> total + amount } ?: 0f
        Log.d("CreateTransactionDialog", "monthlyBudget: $monthlyBudget, total: $total")

        /*val today = Calendar.getInstance().apply {
            time = Date()
        }
        val startOfDay = today.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfDay = today.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time
        val todayTransactions = transactionStore.read(startOfDay, endOfDay)*/
        if (total >= monthlyBudget * 0.65) {
            val message =
                if (total > monthlyBudget)
                    "You went ${
                        formatCurrency(
                            total - monthlyBudget,
                            context
                        )
                    } over your ${formatCurrency(monthlyBudget, context)} budget."
                else "You’re nearing your budget! Just ${
                    formatCurrency(
                        monthlyBudget - total,
                        context
                    )
                } remains out of ${formatCurrency(monthlyBudget, context)}"

            WalleyNotificationManager.createNotification(
                context,
                Intent(context, MainActivity::class.java),
                "Budget limit alert!",
                message
            )
        }
    }

}