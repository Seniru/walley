package com.seniru.walley

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.gridlayout.widget.GridLayout


class CreateCategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_new_category)

        findViewById<TextView>(R.id.back_button).setOnClickListener {
            finish()
        }

        val icons = arrayOf(
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


        val gridView = findViewById<GridLayout>(R.id.icon_grid)

        for (i in icons.indices) {
            val textView = TextView(this).apply {
                text = icons[i]
                gravity = Gravity.CENTER
                width = 180
                height = 180
                textSize = 32f
                setPadding(20)
            }
            textView.typeface = ResourcesCompat.getFont(this, R.font.fa_solid)


            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(i / 6)
                columnSpec = GridLayout.spec(i % 6)
            }
            params.rowSpec = GridLayout.spec(i / 6)
            params.columnSpec = GridLayout.spec(i % 6)
            params.setMargins(10, 10, 10, 10)

            textView.layoutParams = params
            gridView.addView(textView)
        }

    }
}