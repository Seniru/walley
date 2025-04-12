package com.seniru.walley

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.ui.TransactionView
import com.seniru.walley.utils.formatTime
import java.util.Date
import java.util.Locale

class DiaryFragment : Fragment(R.layout.layout_diary) {

    private lateinit var transactionStore: TransactionDataStore
    private lateinit var categoryStore: CategoryDataStore
    private lateinit var itemsContainer: LinearLayout
    private lateinit var displayDateTextView: TextView

    private var displayingDate = Calendar.getInstance().apply {
        time = Date()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionStore = TransactionDataStore.getInstance(requireContext())
        categoryStore = CategoryDataStore.getInstance(requireContext())
        itemsContainer = view.findViewById(R.id.items)
        displayDateTextView = view.findViewById(R.id.displayDate)

        view.findViewById<TextView>(R.id.datePre).setOnClickListener {
            changeDateBy(-1)
        }

        view.findViewById<TextView>(R.id.dateNext).setOnClickListener {
            changeDateBy(1)

        }

        displayTransactions()

        lifecycleScope.launchWhenCreated {
            LiveDataEventBus.events.collect { event ->
                if (event == "refresh_transactions") {
                    displayTransactions()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun displayTransactions() {
        val startOfDay = displayingDate.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfDay = displayingDate.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time
        val transactions = transactionStore.read(startOfDay, endOfDay)
        val categories = categoryStore.readAll()
        itemsContainer.removeAllViews()
        for (transaction in transactions) {
            TransactionView(requireContext(), null).apply {
                val transactionView = TransactionView(requireContext(), null)
                transactionView.setTitle(transaction.title)
                transactionView.setIcon("\ue4c6")
                transactionView.setTime(formatTime(transaction.date))
                transactionView.setIconColor(R.color.primary)
                transactionView.setIsIncome(transaction.type == "income")
                transaction.index?.let { transactionView.setIndex(it) }
                categories.find { it.name == transaction.category }
                    ?.let { transactionView.setCategory(it) }
                transaction.amount?.let { transactionView.setValue(it) }
                itemsContainer.addView(transactionView)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeDateBy(amount: Int) {
        displayingDate.add(Calendar.DAY_OF_MONTH, amount)

        val calendar = Calendar.getInstance().apply {
            time = Date()

        }
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time.time
        val yesterday = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time.time
        displayDateTextView.text = when {
            displayingDate.time.time >= today && displayingDate.time.time < today + 24 * 60 * 60 * 1000 -> "Today"
            displayingDate.time.time in yesterday..<today -> "Yesterday"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(displayingDate)
        }
        displayTransactions()
    }



}
