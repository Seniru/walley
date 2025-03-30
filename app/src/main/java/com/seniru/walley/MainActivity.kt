package com.seniru.walley

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private var mainFrame: FrameLayout? = null
    private var currentScreen = 0
    private val screens = arrayOf(
        arrayOf(R.id.diary_button, DiaryFragment::class.java),
        arrayOf(R.id.categories_button, CategoryFragment::class.java),
        arrayOf(R.id.report_button, ReportFragment::class.java),
        arrayOf(R.id.settings_button, DiaryFragment::class.java),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainFrame = findViewById(R.id.mainframe)

        for (i in screens.indices) {
            val screen = screens[i]

            val button = findViewById<LinearLayout>(screen[0] as Int)
            button.setOnClickListener {
                switchScreens(i)
            }
        }

        switchScreens(0)
    }

    private fun switchScreens(newScreen: Int) {
        // remove previous states
        mainFrame?.removeAllViews()
        val previousButton = findViewById<LinearLayout>(screens[currentScreen][0] as Int)
        for (child in previousButton.children) {
            if (child is TextView) child.setTextColor(resources.getColor(R.color.background))
        }
        // create new state
        currentScreen = newScreen
        switchFragment(currentScreen)
        val currentButton = findViewById<LinearLayout>(screens[currentScreen][0] as Int)
        for (child in currentButton.children) {
            if (child is TextView) child.setTextColor(resources.getColor(R.color.textPrimary))
        }

    }

    private fun switchFragment(newScreen: Int) {
        val fragmentClass = screens[newScreen][1] as Class<*>
        val fragment = fragmentClass.newInstance() as Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainframe, fragment)
            .commit()
    }
}