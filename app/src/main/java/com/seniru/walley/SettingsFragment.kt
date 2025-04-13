package com.seniru.walley

import android.app.Activity
import android.content.Intent
import android.icu.util.Currency
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.seniru.walley.persistence.AppData
import com.seniru.walley.persistence.LiveDataEventBus
import com.seniru.walley.persistence.SharedMemory
import com.seniru.walley.persistence.AppData.Companion


class SettingsFragment : Fragment(R.layout.layout_settings) {

    private lateinit var preferences: SharedMemory
    private lateinit var pushNotificationsSwitch: SwitchCompat
    private lateinit var budgetLimitSwitch: SwitchCompat
    private lateinit var dailyReminderSwitch: SwitchCompat
    @RequiresApi(Build.VERSION_CODES.O)
    private val importFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            AppData.handleImport(result, requireContext())
        }

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
            AppData.exportData(requireContext())
        }

        view.findViewById<Button>(R.id.importDataButton).setOnClickListener {
            AppData.importData(importFileLauncher)
        }

        view.findViewById<Button>(R.id.deleteDataButton).setOnClickListener {
            AppData.clearData(requireContext())
            LiveDataEventBus.sendEvent("refresh_transactions")
        }
    }

    private fun getCurrencyDisplayName(currency: Currency): String {
        return "%s - %s %s".format(
            currency.currencyCode,
            currency.displayName,
            if (currency.currencyCode == currency.symbol) "" else "(" + currency.symbol + ")"
        )
    }

}
