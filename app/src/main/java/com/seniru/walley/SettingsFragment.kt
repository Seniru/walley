package com.seniru.walley

import android.app.Activity
import android.content.Intent
import android.icu.util.Currency
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.seniru.walley.models.Category
import com.seniru.walley.models.Transaction
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.enums.enumEntries

class SettingsFragment : Fragment(R.layout.layout_settings) {

    private val OPEN_FILE_REQUEST_CODE = 1000
    private lateinit var preferences: SharedMemory
    private lateinit var pushNotificationsSwitch: SwitchCompat
    private lateinit var budgetLimitSwitch: SwitchCompat
    private lateinit var dailyReminderSwitch: SwitchCompat

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        preferences = SharedMemory.getInstance(requireContext())
        super.onViewCreated(view, savedInstanceState)

        pushNotificationsSwitch = view.findViewById(R.id.push_notification_switch)
        budgetLimitSwitch = view.findViewById(R.id.budget_limit_switch)
        dailyReminderSwitch = view.findViewById(R.id.daily_reminder_switch)

        displaySettings(view)
        lifecycleScope.launchWhenCreated {
            LiveDataEventBus.events.collect { event ->
                if (event == "refresh_settings") {
                    displaySettings(view)
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun displaySettings(view: View) {
        val currencyList = Currency.getAvailableCurrencies()
            .map { getCurrencyDisplayName(it) }

        val adapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val preferredCurrency = preferences.getCurrency()
        val currencySpinner = view.findViewById<Spinner>(R.id.currency_spinner)
        currencySpinner.adapter = adapter
        currencySpinner.setSelection(adapter.getPosition(getCurrencyDisplayName(preferredCurrency)))
        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val currencyISO = currencyList[position].subSequence(0, 3).toString()
                // do not update if no changes were made to the previous state
                if (currencyISO == preferences.getCurrency().currencyCode) return
                preferences.setCurrency(currencyISO)
                LiveDataEventBus.sendEvent("refresh_transactions")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        pushNotificationsSwitch.apply {
            isChecked = preferences.getIsAllowingPushNotifications()
            setOnClickListener {
                preferences.setIsAllowingPushNotifications(isChecked)
                budgetLimitSwitch.isEnabled = isChecked
                dailyReminderSwitch.isEnabled = isChecked
                if (isChecked && !WalleyNotificationManager.checkPermissions(requireContext()))
                    WalleyNotificationManager.requestPermissions(activity as Activity)
            }
        }

        budgetLimitSwitch.apply {
            isChecked = preferences.getIsSendingBudgetExceededAlert()
            isEnabled = preferences.getIsAllowingPushNotifications()
            setOnClickListener {
                preferences.setIsSendingBudgetExceededAlert(isChecked)
                if (isChecked && !WalleyNotificationManager.checkPermissions(requireContext()))
                    WalleyNotificationManager.requestPermissions(activity as Activity)
            }
        }

        dailyReminderSwitch.apply {
            isChecked = preferences.getIsDailyReminderEnabled()
            isEnabled = preferences.getIsAllowingPushNotifications()
            setOnClickListener {
                preferences.setIsDailyReminderEnabled(isChecked)
                if (isChecked && !WalleyNotificationManager.checkPermissions(requireContext()))
                    WalleyNotificationManager.requestPermissions(activity as Activity)
                if (isChecked && !Reminder.checkPermissions(requireContext()))
                    Reminder.requestRequiredPermissions(activity as Activity)
            }
        }

        val monthlyBudgetTextView =
            view.findViewById<TextView>(R.id.monthly_budget_textview).apply {
                text = preferences.getMonthlyBudget().toString()
            }

        view.findViewById<Button>(R.id.update_budget_button).setOnClickListener {
            val budget = monthlyBudgetTextView.text.toString().toFloatOrNull()
            preferences.setMonthlyBudget(budget)
            LiveDataEventBus.sendEvent("refresh_transactions")
        }

        view.findViewById<Button>(R.id.exportDataButton).setOnClickListener {
            exportData()
        }

        view.findViewById<Button>(R.id.importDataButton).setOnClickListener {
            importData()
        }

        view.findViewById<Button>(R.id.deleteDataButton).setOnClickListener {
            clearData()
            LiveDataEventBus.sendEvent("refresh_transactions")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_FILE_REQUEST_CODE) handleImport(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun exportData() {
        val json = JSONObject()

        val prefsJson = JSONObject().apply {
            put("initialized", true)
            put("balance", preferences.getBalance())
            put("monthly_budget", preferences.getMonthlyBudget())
            put("currency", preferences.getCurrency().currencyCode)
            put("push_notifications", preferences.getIsAllowingPushNotifications())
            put("budget_alerts", preferences.getIsSendingBudgetExceededAlert())
            put("daily_reminder", preferences.getIsDailyReminderEnabled())
        }
        json.put("preferences", prefsJson)

        val categoryStore = CategoryDataStore.getInstance(requireContext())
        val categories = categoryStore.readAll()
        val categoriesJson = JSONArray()
        for (category in categories) {
            categoriesJson.put(category.toJson())
        }
        json.put("categories", categoriesJson)

        val transactionsStore = TransactionDataStore.getInstance(requireContext())
        val transactions = transactionsStore.readAll()
        val transactionsJson = JSONArray()
        for (transaction in transactions) {
            transactionsJson.put(transaction.toJson())
        }
        json.put("transactions", transactionsJson)

        Log.i("SettingsFragment", "Exporting data: ${json}")
        // save the file in Downloads
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "walley_exported.json"
        )

        try {
            FileOutputStream(file).use { out ->
                out.write(json.toString().toByteArray())
                Toast.makeText(
                    context,
                    "Exported to Downloads/walley_exported.json",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.i("SettingsFragment", "Exported data")
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Something unexpected happened while exporting your data",
                Toast.LENGTH_LONG
            ).show()
            Log.e("SettingsFragment", e.toString())
        }
    }

    private fun importData() {
        val openFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(openFileIntent, OPEN_FILE_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleImport(intent: Intent?) {
        try {
            if (intent?.data != null) {
                context?.contentResolver?.openInputStream(intent.data!!)?.apply {
                    val rawJson = bufferedReader().readText()
                    Log.d("SettingsFragment", "Imported json: $rawJson")

                    val json = JSONObject(rawJson)
                    val preferencesJson = json.getJSONObject("preferences")
                    val categoriesJson = json.getJSONArray("categories")
                    val transactionsJson = json.getJSONArray("transactions")

                    // import all preferences
                    preferences.setBalance(preferencesJson.getDouble("balance").toFloat())
                    preferences.setMonthlyBudget(
                        preferencesJson.getDouble("monthly_budget").toFloat(),
                        true
                    )
                    preferences.setCurrency(preferencesJson.getString("currency"), true)
                    preferences.setIsAllowingPushNotifications(
                        preferencesJson.getBoolean("push_notifications"),
                        true
                    )
                    preferences.setIsDailyReminderEnabled(
                        preferencesJson.getBoolean("daily_reminder"),
                        true
                    )
                    preferences.setIsSendingBudgetExceededAlert(
                        preferencesJson.getBoolean("budget_alerts"),
                        true
                    )

                    // import all categories
                    val categories = arrayListOf<Category>()
                    for (i in 0 until categoriesJson.length()) {
                        categories.add(
                            Category.fromJson(
                                categoriesJson[i] as JSONObject,
                                index = i
                            )
                        )
                    }
                    CategoryDataStore.getInstance(requireContext()).set(categories)

                    // import all transactions
                    val transactions = arrayListOf<Transaction>()
                    for (i in 0 until transactionsJson.length()) {
                        transactions.add(
                            Transaction.fromJson(
                                transactionsJson[i] as JSONObject,
                                index = i
                            )
                        )
                    }
                    TransactionDataStore.getInstance(requireContext()).set(transactions)

                    LiveDataEventBus.sendEvent("refresh_transactions")
                    LiveDataEventBus.sendEvent("refresh_settings")
                    close()
                }
            }
        } catch (e: Exception) {
            Log.e("SettingsFragment", e.toString())
            Toast.makeText(
                context,
                "An error occurred while importing your data",
                Toast.LENGTH_LONG
            ).show()
            clearData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clearData() {
        preferences.clearAll()
        TransactionDataStore.getInstance(requireContext()).clearAll()
        CategoryDataStore.getInstance(requireContext()).clearAll()
        LiveDataEventBus.sendEvent("refresh_settings")
        LiveDataEventBus.sendEvent("refresh_transactions")
        Toast.makeText(context, "Cleared all data", Toast.LENGTH_SHORT).show()
        Log.i("SettingsFragment", "clearData")
    }

    private fun getCurrencyDisplayName(currency: Currency): String {
        return "%s - %s %s".format(
            currency.currencyCode,
            currency.displayName,
            if (currency.currencyCode == currency.symbol) "" else "(" + currency.symbol + ")"
        )
    }

}
