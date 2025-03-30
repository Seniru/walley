package com.seniru.walley.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.seniru.walley.R
import com.seniru.walley.utils.Colors

class CategoryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_category_view, this, true)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.walley,
            0, 0
        ).apply {

            val value = getString(R.styleable.walley_value)!!.toFloat()
            val maxValue = getString(R.styleable.walley_maxValue)!!.toFloat()
            val percent = (value / maxValue) * 100

            findViewById<TextView>(R.id.icon).apply {
                text = getString(R.styleable.walley_useIcon)

                val color = getColor(
                    R.styleable.walley_color,
                    ContextCompat.getColor(context, R.color.primary)
                )
                val lighterColor = Colors.lightenColor(color, 0.85f)

                backgroundTintList = ColorStateList.valueOf(lighterColor)
                setTextColor(color)
            }

            findViewById<TextView>(R.id.categoryTitle).text = getString(R.styleable.walley_title)
            findViewById<TextView>(R.id.spendingValue).text = resources.getString(
                R.string.budget_vs_expenses,
                value.toString(),
                maxValue.toString()
            )
            // todo: the percent value should be a percentage of total spending
            findViewById<TextView>(R.id.percentValue).text =
                resources.getString(R.string.percent, percent)
            findViewById<ProgressBar>(R.id.progressBar).apply {
                progress = value.toInt()
                max = maxValue.toInt()
                progressTintList = ColorStateList.valueOf(
                    resources.getColor(
                        when {
                            percent < 65 -> R.color.primary
                            percent >= 65 && percent < 85 -> R.color.secondary
                            else -> R.color.error
                        }
                    )
                )
            }
        }
    }
}