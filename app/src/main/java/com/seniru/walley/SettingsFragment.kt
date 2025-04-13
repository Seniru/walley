package com.seniru.walley

import android.app.Activity
import android.icu.util.Currency
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.TransactionDataStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.enums.enumEntries

class SettingsFragment : Fragment(R.layout.layout_settings) {

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
        }

        view.findViewById<Button>(R.id.exportDataButton).setOnClickListener {
            exportData()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun exportData() {
        val json = JSONObject()

        val preferences = JSONObject().apply {
            put("initialized", true)
            put("balance", preferences.getBalance())
            put("monthly_budget", preferences.getMonthlyBudget())
            put("currency", preferences.getCurrency().currencyCode)
            put("push_notifications", preferences.getIsAllowingPushNotifications())
            put("budget_alerts", preferences.getIsSendingBudgetExceededAlert())
            put("daily_reminder", preferences.getIsDailyReminderEnabled())
        }
        json.put("preferences", preferences)

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

    }

    private fun clearData() {

    }

    private fun getCurrencyDisplayName(currency: Currency): String {
        return "%s - %s %s".format(
            currency.currencyCode,
            currency.displayName,
            if (currency.currencyCode == currency.symbol) "" else "(" + currency.symbol + ")"
        )
    }

}
