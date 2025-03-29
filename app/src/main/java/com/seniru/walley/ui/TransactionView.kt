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
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_transaction, this, true)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.walley,
            0, 0
        ).apply {
            val isIncome = getString(R.styleable.walley_type) == "income"
            findViewById<TextView>(R.id.value).apply {
                text = getString(R.styleable.walley_value)
                setTextColor(
                    getResources().getColor(
                        if (isIncome) R.color.primary else R.color.error
                    )
                )
            }

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

            findViewById<TextView>(R.id.transactionTitle).text = getString(R.styleable.walley_title)
            findViewById<TextView>(R.id.time).text = getString(R.styleable.walley_time)

        }
    }
}