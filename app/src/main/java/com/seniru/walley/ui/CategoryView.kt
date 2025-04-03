package com.seniru.walley.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.seniru.walley.R
import com.seniru.walley.utils.Colors
import com.seniru.walley.utils.formatCurrency

class CategoryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val nameTextView: TextView
    private val progressBar: ProgressBar
    private val iconView: TextView
    private val spendingTextView: TextView
    private var value: Float = -1f
    private var maxValue: Float = -1f


    init {
        LayoutInflater.from(context).inflate(R.layout.layout_category_view, this, true)

        nameTextView = findViewById(R.id.categoryTitle)
        progressBar = findViewById(R.id.progressBar)
        iconView = findViewById(R.id.icon)
        spendingTextView = findViewById(R.id.spendingValue)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.walley,
            0, 0
        ).apply {
            setValue(getString(R.styleable.walley_value)?.toFloat() ?: -1f)
            setMaxValue(getString(R.styleable.walley_maxValue)?.toFloat() ?: -1f)
            setIcon(getString(R.styleable.walley_useIcon))
            setIconColor(
                getColor(
                    R.styleable.walley_color,
                    ContextCompat.getColor(context, R.color.primary)
                )
            )
            setSpending()
        }
    }

    fun setName(title: String) {
        nameTextView.text = title
    }

    fun setIcon(icon: String?) {
        iconView.text = icon
    }

    fun setIconColor(color: Int) {
        Log.i("test", color.toString())
        iconView.apply {
            val lighterColor = Colors.lightenColor(color, 0.85f)
            backgroundTintList = ColorStateList.valueOf(lighterColor)
            setTextColor(color)
        }
    }


    fun setValue(value: Float) {
        this.value = value
    }

    fun setMaxValue(maxValue: Float) {
        this.maxValue = maxValue
    }

    fun setSpending() {
        if (value == -1f) return
        spendingTextView.text = resources.getString(
            R.string.budget_vs_expenses,
            formatCurrency(value, context),
            formatCurrency(maxValue, context)
        )

        val percent = (value / maxValue) * 100
        progressBar.apply {
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