package com.seniru.walley.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.seniru.walley.CreateCategoryActivity
import com.seniru.walley.R
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.Colors
import com.seniru.walley.utils.formatCurrency
import org.w3c.dom.Text

@RequiresApi(Build.VERSION_CODES.O)
class CategoryView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val nameTextView: TextView
    private val progressBar: ProgressBar
    private val iconView: TextView
    private val spendingTextView: TextView
    private val percentValueTextView: TextView
    private val contextMenuIcon: TextView
    private var index: Int? = null
    private var name: String? = ""
    private var icon: String? = ""
    private var color: Int? = null
    private var value: Float = -1f
    private var maxValue: Float = -1f


    init {
        LayoutInflater.from(context).inflate(R.layout.layout_category_view, this, true)

        nameTextView = findViewById(R.id.categoryTitle)
        progressBar = findViewById(R.id.progressBar)
        iconView = findViewById(R.id.icon)
        spendingTextView = findViewById(R.id.spendingValue)
        percentValueTextView = findViewById(R.id.percentValue)
        contextMenuIcon = findViewById(R.id.contextMenuTextView)

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

        (context as Activity).registerForContextMenu(contextMenuIcon)
        contextMenuIcon.setOnClickListener {
            showContextMenuForChild(contextMenuIcon, 0f, 0f)
        }
        contextMenuIcon.setOnCreateContextMenuListener { menu, v, menuInfo ->
            val inflater = MenuInflater(context)
            inflater.inflate(R.menu.transaction_menu, menu)
            // edit option
            menu[0].setOnMenuItemClickListener {
                openEditCategoryActivity()
                return@setOnMenuItemClickListener true
            }
            // delete option
            menu[1].setOnMenuItemClickListener {
                deleteCategory()
                return@setOnMenuItemClickListener true
            }
        }

    }

    fun setName(title: String) {
        this.name = title
        nameTextView.text = title
    }

    fun setIcon(icon: String?) {
        this.icon = icon
        iconView.text = icon
    }

    fun setIconColor(color: Int) {
        this.color = color
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
            max = maxValue.toInt()
            progress = value.toInt()
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

    fun setSpendingVsTotal(percentage: Float) {
        percentValueTextView.text = resources.getString(R.string.percent, percentage)
    }

    fun setIndex(index: Int) {
        this.index = index
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteCategory() {
        if (index == null) return
        val transactionStore = TransactionDataStore.getInstance(context)
        val categoryStore = CategoryDataStore.getInstance(context)
        val deletingCategory = categoryStore.get(index!!)
        val transactions = transactionStore.readAll().map {
            if (it.category == deletingCategory.name) {
                it.category = "Other"
            }
            it
        }
        transactionStore.set(transactions as ArrayList<Transaction>)
        categoryStore.delete(index!!)

        LiveDataEventBus.sendEvent("refresh_categories")
    }

    fun openEditCategoryActivity() {
        val createCategoryIntent = Intent(context, CreateCategoryActivity::class.java).apply {
            putExtra("editting", true)
            putExtra("index", index)
            putExtra("name", name)
            putExtra("maxValue", maxValue)
            putExtra("icon", icon)
            putExtra("color", color)
        }
        context.startActivity(createCategoryIntent)
    }

}