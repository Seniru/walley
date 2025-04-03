package com.seniru.walley.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.seniru.walley.R
import com.seniru.walley.models.Category
import com.seniru.walley.utils.Colors
import com.seniru.walley.utils.formatCurrency

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
            setValue(getFloat(R.styleable.walley_value, 0f))
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

    fun setValue(value: Float) {
        valueView.text = formatCurrency(value, context)

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun setCategory(category: Category) {
        setIcon(category.icon)
        category.color?.let { setIconColor(it.toArgb()) }
    }

    fun setTime(time: String?) {
        timeView.text = time
    }

}