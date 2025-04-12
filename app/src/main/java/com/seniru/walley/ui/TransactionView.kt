package com.seniru.walley.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.seniru.walley.R
import com.seniru.walley.models.Category
import com.seniru.walley.utils.Colors
import com.seniru.walley.utils.formatCurrency
import org.w3c.dom.Text
import androidx.core.view.get
import com.seniru.walley.CreateTransactionDialog
import com.seniru.walley.DiaryFragment
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class TransactionView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val titleView: TextView
    private val valueView: TextView
    private val iconView: TextView
    private val timeView: TextView
    private val contextMenuIcon: TextView
    private var index: Int? = null
    private var value: Float = 0f
    private var isIncome: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_transaction, this, true)

        titleView = findViewById(R.id.transactionTitle)
        valueView = findViewById(R.id.value)
        iconView = findViewById(R.id.icon)
        timeView = findViewById(R.id.time)
        contextMenuIcon = findViewById(R.id.contextMenuTextView)

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

        (context as Activity).registerForContextMenu(contextMenuIcon)
        contextMenuIcon.setOnClickListener {
            showContextMenuForChild(contextMenuIcon, 0f, 0f)
        }
        contextMenuIcon.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.transaction_menu, menu)
            // edit option
            menu[0].setOnMenuItemClickListener {
                displayEditTransactionDialog()
                return@setOnMenuItemClickListener true
            }
            // delete option
            menu[1].setOnMenuItemClickListener {
                deleteTransaction()
                return@setOnMenuItemClickListener true
            }
        }

    }

    fun setTitle(title: String?) {
        titleView.text = title
    }

    fun setValue(value: Float) {
        this.value = value
        valueView.text = formatCurrency(value, context)
    }

    fun setIsIncome(isIncome: Boolean) {
        this.isIncome = isIncome
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

    fun setIndex(index: Int) {
        this.index = index
    }

    fun deleteTransaction() {
        if (index == null) return
        val transactionStore = TransactionDataStore.getInstance(context)
        val preferences = SharedMemory.getInstance(context)
        transactionStore.delete(index!!)
        preferences.setBalance(
            preferences.getBalance() + (value) * (if (isIncome) -1 else 1)
        )
        LiveDataEventBus.sendEvent("refresh_transactions")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun displayEditTransactionDialog() {
        if (index == null) return
        val transactionStore = TransactionDataStore.getInstance(context)
        val transaction = transactionStore.get(index!!)
        val dialog = CreateTransactionDialog(
            context,
            true,
            transaction.index,
            transaction.title,
            transaction.amount,
            transaction.type,
            transaction.category,
            transaction.date
        )
        dialog.show()
    }

}