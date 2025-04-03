package com.seniru.walley

import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.seniru.walley.models.Category
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.ui.CategoryView
import com.seniru.walley.ui.TransactionView
import com.seniru.walley.utils.formatTime

class CategoryFragment : Fragment(R.layout.layout_category) {

    private lateinit var categoryStore: CategoryDataStore
    private lateinit var transactionStore: TransactionDataStore
    private lateinit var categoryList: LinearLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryStore = CategoryDataStore.getInstance(requireContext())
        transactionStore = TransactionDataStore.getInstance(requireContext())
        categoryList = view.findViewById(R.id.categoryView)
        view.findViewById<Button>(R.id.create_category_button).setOnClickListener {
            val createCategoryIntent =
                Intent(requireContext(), CreateCategoryActivity::class.java)
            startActivity(createCategoryIntent)
        }
        displayCategories()

        lifecycleScope.launchWhenCreated {
            LiveDataEventBus.events.collect { event ->
                if (event == "refresh_categories") {
                    displayCategories()
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayCategories() {
        val calendar = Calendar.getInstance()
        val fromDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val toDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time

        val categories = categoryStore.readAll()
        val transactions = transactionStore.read(fromDate, toDate)

        val monthTotal = transactions
            .map { it.amount ?: 0.0f }
            .reduceOrNull { total, amount -> total + amount } ?: 1f

        categoryList.removeAllViews()
        for (category in categories) {
            val categoryView = CategoryView(requireContext(), null)
            categoryView.setName(category.name)
            val total = transactions
                .filter { it.category == category.name }
                .map { it.amount ?: 0.0f }
                .reduceOrNull { total, amount -> total + amount } ?: 0f
            categoryView.setValue(total)
            categoryView.setMaxValue(category.spendingLimit!!)
            categoryView.setIcon(category.icon)
            category.color?.toArgb()?.let { categoryView.setIconColor(it) }
            categoryView.setSpending()
            categoryView.setSpendingVsTotal((total / monthTotal) * 100)
            categoryList.addView(categoryView)
        }
    }

}
