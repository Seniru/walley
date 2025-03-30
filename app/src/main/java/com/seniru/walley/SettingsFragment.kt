package com.seniru.walley

import android.icu.util.Currency
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlin.enums.enumEntries

class SettingsFragment : Fragment(R.layout.layout_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currencyList = Currency.getAvailableCurrencies()
            .map {
                "%s - %s %s".format(
                    it.currencyCode,
                    it.displayName,
                    if (it.currencyCode == it.symbol) "" else "(" + it.symbol + ")"
                )
            }

        val adapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val currencySpinner = view.findViewById<Spinner>(R.id.currency_spinner)
        currencySpinner.adapter = adapter

    }

}
