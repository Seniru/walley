package com.seniru.walley.persistence

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Currency
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.seniru.walley.utils.ValidationResult

class SharedMemory private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val preferences: SharedPreferences =
        context.getSharedPreferences("walley_preferences", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: SharedMemory? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: SharedMemory(context).also { instance = it }
            }
    }

    fun getIsAllowingPushNotifications(): Boolean {
        return preferences.getBoolean("push_notifications_enabled", false)
    }

    fun getIsSendingBudgetExceededAlert(): Boolean {
        return preferences.getBoolean("budget_limit_alerts_enabled", false)
    }

    fun getIsDailyReminderEnabled(): Boolean {
        return preferences.getBoolean("daily_reminder_enabled", false)
    }

    fun getMonthlyBudget(): Float {
        return preferences.getFloat("monthly_budget", 0f)
    }

    fun getCurrency(): Currency {
        val currencyISO = preferences.getString("currency", "USD")
        return Currency.getInstance(currencyISO)
    }

    fun getBalance(): Float {
        return preferences.getFloat("balance", 0f)
    }

    fun setIsAllowingPushNotifications(enabled: Boolean) {
        Log.i("SharedMemory", "setNotificationPreferences: $enabled")
        preferences.edit() { putBoolean("push_notifications_enabled", enabled).apply() }
        Toast.makeText(
            appContext,
            "Push notifications ${if (enabled) "enabled" else "disabled"}!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setIsSendingBudgetExceededAlert(enabled: Boolean) {
        Log.i("SharedMemory", "setNotificationPreferences: $enabled")
        preferences.edit() { putBoolean("budget_limit_alerts_enabled", enabled).apply() }
        Toast.makeText(
            appContext,
            "Budget exceed alerts ${if (enabled) "enabled" else "disabled"}!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setIsDailyReminderEnabled(enabled: Boolean) {
        Log.i("SharedMemory", "setNotificationPreferences: $enabled")
        preferences.edit() { putBoolean("daily_reminder_enabled", enabled).apply() }
        Toast.makeText(
            appContext,
            "${if (enabled) "Enabled" else "Disabled"} daily reminders!",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun setMonthlyBudget(budget: Float?) {
        Log.i("SharedMemory", "setNotificationPreferences: $budget")
        val validation =
            if (budget == null) ValidationResult.Empty("Please set a value to budget")
            else if (budget < 0) ValidationResult.Invalid("Budget cannot be negative")
            else ValidationResult.Valid

        when (validation) {
            is ValidationResult.Empty ->
                Toast.makeText(appContext, validation.error, Toast.LENGTH_SHORT).show()

            is ValidationResult.Invalid ->
                Toast.makeText(appContext, validation.error, Toast.LENGTH_SHORT).show()

            is ValidationResult.Valid -> {
                preferences.edit() { putFloat("monthly_budget", budget!!).apply() }
                Toast.makeText(appContext, "Monthly budget updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setCurrency(currencyISO: String) {
        Log.i("SharedMemory", "setCurrency: $currencyISO")
        preferences.edit() { putString("currency", currencyISO).apply() }
        Toast.makeText(appContext, "Preferred currency updated!", Toast.LENGTH_SHORT).show()
    }

    fun setBalance(balance: Float) {
        Log.i("SharedMemory", "setBalance: $balance")
        preferences.edit() { putFloat("balance", balance).apply() }
    }

    fun clearAll() {
        Log.i("SharedMemory", "clearAll")
        preferences.edit(commit = true) { clear() }
    }

}