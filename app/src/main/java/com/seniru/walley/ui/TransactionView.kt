package com.seniru.walley.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.seniru.walley.R
import com.seniru.walley.utils.Colors

class TransactionView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val titleView: TextView
    private val valueView: TextView
    private val iconView: TextView
    private val timeView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_transaction, this, true)

        titleView = findViewById(R.id.transactionTitle)
        valueView = findViewById(R.id.value)
        iconView = findViewById(R.id.icon)
        timeView = findViewById(R.id.time)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.walley,
            0, 0
        ).apply {
            setTitle(getString(R.styleable.walley_title))
            setIsIncome(getString(R.styleable.walley_type) == "income")
            setValue(getString(R.styleable.walley_value))
            setIcon(getString(R.styleable.walley_useIcon))
            setIconColor(
                getColor(
                    R.styleable.walley_color,
                    ContextCompat.getColor(context, R.color.primary)
                )
            )
            setTime(getString(R.styleable.walley_time))

        }
    }

    fun setTitle(title: String?) {
        titleView.text = title
    }

    fun setValue(value: String?) {
        valueView.text = value

    }

    fun setIsIncome(isIncome: Boolean) {
        valueView.setTextColor(
            ContextCompat.getColor(
                context, if (isIncome) R.color.primary else R.color.error
            )
        )
    }

    fun setIcon(icon: String?) {
        iconView.text = icon
    }

    fun setIconColor(color: Int) {
        val lighterColor = Colors.lightenColor(color, 0.85f)
        iconView.backgroundTintList = ColorStateList.valueOf(lighterColor)
        iconView.setTextColor(color)
    }

    fun setTime(time: String?) {
        timeView.text = time
    }

}