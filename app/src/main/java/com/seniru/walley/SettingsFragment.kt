package com.seniru.walley

import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.seniru.walley.persistence.SharedMemory
import kotlin.enums.enumEntries

class SettingsFragment : Fragment(R.layout.layout_settings) {

    private lateinit var preferences: SharedMemory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        preferences = SharedMemory.getInstance(requireContext())
        super.onViewCreated(view, savedInstanceState)
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
                preferences.setCurrency(currencyISO)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        view.findViewById<SwitchCompat>(R.id.push_notification_switch).apply {
            isChecked = preferences.getIsAllowingPushNotifications()
            setOnClickListener {
                preferences.setIsAllowingPushNotifications(isChecked)
            }
        }

        view.findViewById<SwitchCompat>(R.id.budget_limit_switch).apply {
            isChecked = preferences.getIsSendingBudgetExceededAlert()
            setOnClickListener {
                preferences.setIsSendingBudgetExceededAlert(isChecked)
            }
        }

        view.findViewById<SwitchCompat>(R.id.daily_reminder_switch).apply {
            isChecked = preferences.getIsDailyReminderEnabled()
            setOnClickListener {
                preferences.setIsDailyReminderEnabled(isChecked)
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

    }

    private fun getCurrencyDisplayName(currency: Currency): String {
        return "%s - %s %s".format(
            currency.currencyCode,
            currency.displayName,
            if (currency.currencyCode == currency.symbol) "" else "(" + currency.symbol + ")"
        )
    }

}
