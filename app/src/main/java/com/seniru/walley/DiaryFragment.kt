package com.seniru.walley

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

class DiaryFragment : Fragment(R.layout.layout_diary) {

    private lateinit var transactionStore: TransactionDataStore
    private lateinit var categoryStore: CategoryDataStore
    private lateinit var itemsContainer: LinearLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionStore = TransactionDataStore.getInstance(requireContext())
        categoryStore = CategoryDataStore.getInstance(requireContext())
        itemsContainer = view.findViewById(R.id.items)
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
        val transactions = transactionStore.readAll()
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
                categories.find { it.name == transaction.category }
                    ?.let { transactionView.setCategory(it) }
                transaction.amount?.let { transactionView.setValue(it) }
                itemsContainer.addView(transactionView)
            }
        }
    }

}
