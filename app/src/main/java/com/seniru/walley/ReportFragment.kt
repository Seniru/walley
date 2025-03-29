package com.seniru.walley

import android.graphics.Color
import android.os.Bundle
import android.view.View

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
import kotlin.random.Random

class ReportFragment : Fragment(R.layout.layout_report) {

    private var view: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view = view
        drawPieChart()
        drawSpendingLineChart()
    }

    private fun drawPieChart() {
        val pieChart = view?.findViewById<PieChart>(R.id.piechart)
        var entries = listOf(
            PieEntry(50f, "Food"),
            PieEntry(30f, "Transportation"),
            PieEntry(60f, "Electricity"),
            PieEntry(20f, "Entertainment"),
            PieEntry(12f, "Gifts")
        )

        pieChart?.data = PieData(PieDataSet(entries, "").apply {
            colors = listOf(
                "#FF6347".toColorInt(), // Tomato Red
                "#4682B4".toColorInt(), // Steel Blue
                "#32CD32".toColorInt(), // Lime Green
                "#FFD700".toColorInt(), // Gold
                "#8A2BE2".toColorInt(), // Blue Violet
                "#FF4500".toColorInt() // Orange Red
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        })
        pieChart?.description?.apply {
            text = ""
        }
        pieChart?.invalidate()
    }

    private fun drawSpendingLineChart() {
        val random = Random.Default
        val linechart = view?.findViewById<LineChart>(R.id.linechart)
        var values = ArrayList<Entry>()
        var bal = 1000f
        for (i in 1..30) {
            bal += random.nextDouble(-100.0, 80.0).toFloat()
            values.add(Entry(i.toFloat(), bal))
        }

        linechart?.data = LineData(LineDataSet(values, "").apply {
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

}
