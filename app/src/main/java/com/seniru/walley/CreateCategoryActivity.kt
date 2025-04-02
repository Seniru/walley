package com.seniru.walley

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
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
import androidx.core.view.setPadding
import androidx.gridlayout.widget.GridLayout
import com.seniru.walley.models.Category
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.utils.ValidationResult


class CreateCategoryActivity : AppCompatActivity() {

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
        "\uf553", "\uF291", "\uF29C", "\uF54F", "\uf09d", "\uF0C0",
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_new_category)

        categoryNameTextView = findViewById(R.id.categoryName)
        spendingLimitTextView = findViewById(R.id.spendingLimitTextView)
        findViewById<Button>(R.id.createButton).setOnClickListener {
            createCategory()
        }

        findViewById<TextView>(R.id.back_button).setOnClickListener {
            finish()
        }

        val colorCodes = findViewById<LinearLayout>(R.id.colorContainer).children

        selectedColorView =
            colorCodes.first() as FrameLayout

        for (colorCode in colorCodes) {
            colorCode.setOnClickListener {
                selectedColorView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.background))
                selectedColorView = colorCode as FrameLayout
                selectedColorView.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.textPrimary))
            }
        }


        val gridView = findViewById<GridLayout>(R.id.icon_grid)

        for (i in icons.indices) {
            val textView = TextView(this).apply {
                text = icons[i]
                gravity = Gravity.CENTER
                width = 180
                height = 180
                textSize = 32f
                background = resources.getDrawable(R.color.background)
            }
            textView.typeface = ResourcesCompat.getFont(this, R.font.fa_solid)

            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(i / 5)
                columnSpec = GridLayout.spec(i % 5)
            }
            params.rowSpec = GridLayout.spec(i / 5)
            params.columnSpec = GridLayout.spec(i % 5)
            params.setMargins(10, 10, 10, 10)

            textView.layoutParams = params

            val frame = FrameLayout(this).apply {
                background = resources.getDrawable(R.drawable.container_highlighted)
                backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.background))
                setPadding(15)
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
                this,
                validationResult.error,
                Toast.LENGTH_SHORT
            ).show()

            is ValidationResult.Invalid -> Toast.makeText(
                this,
                validationResult.error,
                Toast.LENGTH_SHORT
            ).show()

            else -> {
                categoryDataStore.push(category)
                LiveDataEventBus.sendEvent("refresh_categories")
                finish()
            }
        }
    }

}