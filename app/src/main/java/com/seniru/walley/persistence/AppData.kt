package com.seniru.walley.persistence

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import com.seniru.walley.models.Category
import com.seniru.walley.models.Transaction
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class AppData {
    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        fun exportData(context: Context) {

            val json = JSONObject()
            val preferences = SharedMemory.getInstance(context)

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

            val categoryStore = CategoryDataStore.getInstance(context)
            val categories = categoryStore.readAll()
            val categoriesJson = JSONArray()
            for (category in categories) {
                categoriesJson.put(category.toJson())
            }
            json.put("categories", categoriesJson)

            val transactionsStore = TransactionDataStore.getInstance(context)
            val transactions = transactionsStore.readAll()
            val transactionsJson = JSONArray()
            for (transaction in transactions) {
                transactionsJson.put(transaction.toJson())
            }
            json.put("transactions", transactionsJson)

            Log.i("AppData", "Exporting data: ${json}")
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
                Log.i("AppData", "Exported data")
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Something unexpected happened while exporting your data",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("AppData", e.toString())
            }
        }

        fun importData(launcher: ActivityResultLauncher<Intent>) {
            Log.i("AppData", "importData")
            val openFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            launcher.launch(openFileIntent)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun handleImport(result: ActivityResult, context: Context) {
            val preferences = SharedMemory.getInstance(context)
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    if (uri != null) {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val rawJson = inputStream?.bufferedReader()?.readText()
                        Log.d("AppData", "Imported json: $rawJson")

                        val json = JSONObject(rawJson ?: "{}")
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
                        CategoryDataStore.getInstance(context).set(categories)

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
                        TransactionDataStore.getInstance(context).set(transactions)

                        LiveDataEventBus.sendEvent("refresh_transactions")
                        LiveDataEventBus.sendEvent("refresh_settings")
                        Toast.makeText(context, "Imported your data", Toast.LENGTH_SHORT).show()
                        inputStream?.close()
                    }
                }
            } catch (e: Exception) {
                Log.e("AppData", e.toString())
                Toast.makeText(
                    context,
                    "An error occurred while importing your data",
                    Toast.LENGTH_LONG
                ).show()
                clearData(context)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun clearData(context: Context) {
            val preferences = SharedMemory.getInstance(context)
            val categoryStore = CategoryDataStore.getInstance(context)
            val transactionStore = TransactionDataStore.getInstance(context)
            preferences.clearAll()
            transactionStore.clearAll()
            categoryStore.clearAll()

            categoryStore.set(Category.defaults)
            preferences.setIsInitialized(true)

            LiveDataEventBus.sendEvent("refresh_settings")
            LiveDataEventBus.sendEvent("refresh_transactions")
            Toast.makeText(context, "Cleared all data", Toast.LENGTH_SHORT).show()
            Log.i("AppData", "clearData")
        }
    }
}