package com.seniru.walley

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColor
import androidx.core.view.children
import androidx.gridlayout.widget.GridLayout
import com.seniru.walley.models.Category
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.ValidationResult
import com.seniru.walley.utils.dpToPixels
import org.w3c.dom.Text
import java.security.AccessController.getContext


class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var headingTextView: TextView
    private lateinit var createButton: Button
    private lateinit var selectedColorView: FrameLayout
    private lateinit var selectedIconView: FrameLayout
    private lateinit var categoryNameTextView: TextView
    private lateinit var spendingLimitTextView: TextView

    private val icons = arrayOf(
        "\ue4c6", "\uf2e7", "\uf787", "\uf7fb", "\uf578", "\uf72f",
        "\uf6d7", "\uf1fd", "\uf5d1", "\uf1b9", "\uf206", "\ue58b",
        "\uf472", "\uf5e1", "\uf77d", "\uf07a", "\uf072", "\uf21a",
        "\uf238", "\uf018", "\uf193", "\uf0f9", "\uf0fa", "\uf48e",
        "\uf004", "\uf490", "\uf5c9", "\uf54c", "\uf487", "\uf1ad",
        "\uF236", "\uf0f8", "\uF015", "\uf51d", "\uF084", "\uF2CD",
        "\uF562", "\uf678", "\uf67f", "\uF07A", "\uF0A7", "\uF291",
        "\uf030", "\uF217", "\uF07B", "\uF2B9", "\uF19C", "\uf06b",
        "\uf553", "\uf86d", "\uF29C", "\uF54F", "\uf09d", "\uF0C0",
        "\uf091", "\uf79c", "\uf3a5", "\uf0d6", "\uf555", "\uf81d",
        "\uf4c0", "\uf4d3", "\uf24e", "\uF183", "\uF5B0", "\uF8FF",
        "\uF0F4", "\uF0A1", "\uF52F", "\uF02D", "\uF5B6", "\uf001",
        "\uf549", "\uf19d", "\uF1C0", "\uF11B", "\uF0F3", "\uF1E3",
        "\uF251", "\uF1AC", "\uF51D", "\uF6C4", "\uf630", "\uf44b",
        "\uf45d", "\uf5bb", "\uf70c", "\uf44e", "\uf434", "\uf025",
        "\uf3ce", "\uf109", "\uf390", "\uf7d9", "\uf552", "\uf6be",
        "\uf1b0", "\uf6c8", "\uf6d3", "\uf6f0", "\uf0e3", "\ue4e1"

    )

    @RequiresApi(Build.VERSION_CODES.O)
    private val categoryDataStore = CategoryDataStore.getInstance(this)


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_new_category)

        val editting = intent.getBooleanExtra("editting", false)
        categoryNameTextView = findViewById(R.id.categoryName)
        spendingLimitTextView = findViewById(R.id.spendingLimitTextView)
        headingTextView = findViewById(R.id.headingTextView)
        createButton = findViewById(R.id.createButton)
        createButton.setOnClickListener {
            createCategory()
        }

        findViewById<TextView>(R.id.back_button).setOnClickListener {
            finish()
        }

        val colorCodes = findViewById<LinearLayout>(R.id.colorContainer).children


        if (editting) {
            headingTextView.text = getString(R.string.edit_category)
            createButton.text = getString(R.string.edit_category)
            categoryNameTextView.text = intent.getStringExtra("name")
            spendingLimitTextView.text = intent.getFloatExtra("maxValue", 0f).toString()
        }

        selectedColorView = colorCodes.first() as FrameLayout
        val color = intent.getIntExtra("color", 0)
        for (colorCode in colorCodes) {

            if (editting && (colorCode as FrameLayout).children.first().backgroundTintList?.defaultColor == color) {
                selectedColorView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.background))
                selectedColorView = colorCode
                selectedColorView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.textPrimary))
            }
            colorCode.setOnClickListener {
                selectedColorView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.background))
                selectedColorView = colorCode as FrameLayout
                selectedColorView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.textPrimary))
            }
        }


        val gridView = findViewById<GridLayout>(R.id.icon_grid)
        val textViewWidth = dpToPixels(44, this).toInt()

        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        gridView.columnCount = displayMetrics.widthPixels / textViewWidth

        for (i in icons.indices) {
            val textView = TextView(this).apply {
                text = icons[i]
                gravity = Gravity.CENTER
                width = textViewWidth
                height = textViewWidth
                textSize = dpToPixels(16, context)
                setTextColor(resources.getColor(R.color.textPrimary))
                background = resources.getDrawable(R.drawable.container)
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.background))
            }
            textView.typeface = ResourcesCompat.getFont(this, R.font.fa_solid)

            val params = GridLayout.LayoutParams()
            //params.rowSpec = GridLayout.spec(i / 5)
            //params.columnSpec = GridLayout.spec(i % 5)
            val margins = dpToPixels(2, this).toInt()
            params.setMargins(margins, margins, margins, margins)

            textView.layoutParams = params

            val frame = FrameLayout(this).apply {
                background = resources.getDrawable(R.drawable.container_highlighted)
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.background))
            }

            frame.setOnClickListener {
                selectedIconView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.background))
                selectedIconView = frame
                selectedIconView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.textPrimary))
            }

            frame.addView(textView)
            gridView.addView(frame)
        }

        val iconFrames = gridView.children
        selectedIconView = iconFrames.first() as FrameLayout
        if (editting) {
            val icon = intent.getStringExtra("icon")
            for (iconFrame in iconFrames) {
                if (((iconFrame as FrameLayout).children.first() as TextView).text == icon) {
                    selectedIconView = iconFrame
                    break
                }
            }
        }

        selectedIconView.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.textPrimary))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createCategory() {

        val category = Category(
            categoryNameTextView.text.toString(),
            spendingLimitTextView.text.toString().toFloatOrNull(),
            selectedColorView.children.first().backgroundTintList?.defaultColor?.toColor(),
            (selectedIconView.children.first() as TextView).text.toString()
        )

        when (val validationResult = category.validate()) {
            is ValidationResult.Empty -> Toast.makeText(
                this, validationResult.error, Toast.LENGTH_SHORT
            ).show()

            is ValidationResult.Invalid -> Toast.makeText(
                this, validationResult.error, Toast.LENGTH_SHORT
            ).show()

            else -> {
                if (intent.getBooleanExtra("editting", false)) {
                    val newName = categoryNameTextView.text.toString()
                    val oldName = intent.getStringExtra("name")
                    if (newName != oldName) {
                        val transactionStore = TransactionDataStore.getInstance(applicationContext)
                        val transactions = transactionStore.readAll().map {
                            if (it.category == oldName) {
                                it.category = newName
                            }
                            it
                        }
                        transactionStore.set(transactions as ArrayList<Transaction>)
                    }
                    categoryDataStore.replace(intent.getIntExtra("index", 0), category)
                } else {
                    categoryDataStore.push(category)
                }
                LiveDataEventBus.sendEvent("refresh_categories")
                finish()
            }
        }
    }

}