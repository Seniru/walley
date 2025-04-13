package com.seniru.walley

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColor

import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.seniru.walley.models.Category
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.formatCurrency
import java.time.LocalDate
import java.util.Date
import kotlin.math.exp
import kotlin.random.Random

class ReportFragment : Fragment(R.layout.layout_report) {

    private var view: View? = null
    private lateinit var transactionDataStore: TransactionDataStore
    private lateinit var categoryDataStore: CategoryDataStore
    private lateinit var totalIncomeTextView: TextView
    private lateinit var totalExpensesTextView: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view = view
        transactionDataStore = TransactionDataStore.getInstance(requireContext())
        categoryDataStore = CategoryDataStore.getInstance(requireContext())

        totalIncomeTextView = view.findViewById(R.id.totalIncomeTextView)
        totalExpensesTextView = view.findViewById(R.id.totalExpensesTextView)

        drawPieChart()
        drawSpendingLineChart()
        displayIncomeExpenses()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawPieChart() {
        val pieChart = view?.findViewById<PieChart>(R.id.piechart)

        val transactions = transactionDataStore.readLastMonth()
        val categories = categoryDataStore.readAll()
        categories.add(
            0,
            Category("Other", 0f, resources.getColor(R.color.textSecondary).toColor(), "")
        )

        val colors = arrayListOf<Int>()
        val entries = transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount?.toDouble() ?: 0.0 } }
            .map { entry ->
                colors.add(categories.find { it.name == entry.key }?.color?.toArgb() ?: R.color.accent)
                PieEntry(entry.value.toFloat(), entry.key)
            }

        pieChart?.data = PieData(PieDataSet(entries, "").apply {
            setColors(colors)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        })
        pieChart?.description?.apply {
            text = ""
        }
        pieChart?.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawSpendingLineChart() {
        val preferences = SharedMemory.getInstance(requireContext())
        val random = Random.Default
        val linechart = view?.findViewById<LineChart>(R.id.linechart)
        var values = ArrayList<Entry>()
        var bal = preferences.getBalance()

        val now = LocalDate.now()
        val transactions =
            transactionDataStore.readLastMonth().apply { sortByDescending { it.date } }
        for (i in now.month.maxLength() downTo 1) {
            if (i > now.dayOfMonth) values.add(Entry(i.toFloat(), bal))
            else if (i == now.dayOfMonth) values.add(Entry(i.toFloat(), bal))
            else {
                bal += transactions
                    .filter { Date(it.date).date == i }
                    .map { (it.amount ?: 0f) * (if (it.type == "income") -1 else 1) }
                    .sum()

                values.add(Entry(i.toFloat(), bal))
            }
        }

        values.reverse()

        linechart?.data = LineData(
            LineDataSet(values, "").apply
            {
                color = "#FF6347".toColorInt()
                fillColor = "#FF6347".toColorInt()
                lineWidth = 3f
                setDrawCircles(false)
                setDrawFilled(true)

            }).apply {
            setDrawValues(false)
        }
        linechart?.setDrawBorders(false)
        linechart?.setDrawMarkers(false)
        linechart?.description?.apply {
            text = ""
        }

    }

    private fun displayIncomeExpenses() {
        val transactions = transactionDataStore.readLastMonth()
        val income =
            transactions.filter { it.type == "income" }.sumOf { it.amount?.toDouble() ?: 0.0 }
        val expenses =
            transactions.filter { it.type == "expense" }.sumOf { it.amount?.toDouble() ?: 0.0 }

        totalIncomeTextView.text = formatCurrency(income.toFloat(), requireContext())
        totalExpensesTextView.text = formatCurrency(expenses.toFloat(), requireContext())
    }

}
