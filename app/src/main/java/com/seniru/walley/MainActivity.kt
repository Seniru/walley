package com.seniru.walley

import android.content.Intent
import android.content.res.ColorStateList
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import com.seniru.walley.utils.formatCurrency
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private var mainFrame: FrameLayout? = null
    private var currentScreen = 0
    private val screens = arrayOf(
        arrayOf(R.id.diary_button, DiaryFragment::class.java),
        arrayOf(R.id.categories_button, CategoryFragment::class.java),
        arrayOf(R.id.report_button, ReportFragment::class.java),
        arrayOf(R.id.settings_button, SettingsFragment::class.java),
    )
    private lateinit var addTransactionButton: TextView
    private lateinit var preferences: SharedMemory
    private lateinit var transactionDataStore: TransactionDataStore
    private lateinit var spendingProgress: ProgressBar

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = SharedMemory.getInstance(this)
        transactionDataStore = TransactionDataStore.getInstance(this)
        mainFrame = findViewById(R.id.mainframe)
        addTransactionButton = findViewById(R.id.add_trans_button)
        spendingProgress = findViewById(R.id.spendingProgress)
        addTransactionButton.setOnClickListener {
            val dialog = CreateTransactionDialog(this) {
                val diaryFragment =
                    supportFragmentManager.findFragmentByTag("DiaryFragment") as? DiaryFragment
                diaryFragment?.displayTransactions()
            }
            dialog.show()

        }


        for (i in screens.indices) {
            val screen = screens[i]

            val button = findViewById<LinearLayout>(screen[0] as Int)
            button.setOnClickListener {
                switchScreens(i)
            }
        }

        switchScreens(0)
        displayAvailableBalance()
        updateBudgetInformation()
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

    private fun displayAvailableBalance() {
        findViewById<TextView>(R.id.availableBalanceTextView).text = formatCurrency(99.99f, this)
    }

    private fun updateBudgetInformation() {
        val calendar = Calendar.getInstance()
        val fromDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val toDate = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time

        val monthlyBudget = preferences.getMonthlyBudget()
        val transactions = transactionDataStore.read(fromDate, toDate)
        val total = transactions.map { it.amount ?: 0.0f }.reduceOrNull { total, amount -> total + amount }
        findViewById<TextView>(R.id.budgetLimitTextView).text =
            getString(
                R.string.budget_vs_expenses,
                formatCurrency(total ?: 0f, this),
                formatCurrency(monthlyBudget, this)
            )

        val percent = (total?.div(monthlyBudget))?.times(100)
        spendingProgress.max = monthlyBudget.toInt()
        spendingProgress.progress = total?.toInt() ?: 0

        if (percent != null) {
            spendingProgress.progressTintList = ColorStateList.valueOf(
                resources.getColor(
                    when {
                        percent < 65 -> R.color.primary
                        percent >= 65 && percent < 85 -> R.color.secondary
                        else -> R.color.error
                    }
                )
            )
        }
        spendingProgress.invalidate()
    }


}