package com.seniru.walley.persistence

import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import com.seniru.walley.models.Transaction
import org.json.JSONArray
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date

class TransactionDataStore private constructor(context: Context) : DataStorable<Transaction> {

    private var appContext = context.applicationContext
    private var transactions: ArrayList<Transaction> = arrayListOf()
    private val fileName = "wally_transactions.json"

    init {
        transactions = readAll()
    }

    companion object {

        @Volatile
        private var storeInstance: TransactionDataStore? = null

        fun getInstance(context: Context): TransactionDataStore =
            storeInstance ?: synchronized(this) {
                storeInstance ?: TransactionDataStore(context).also { storeInstance = it }
            }
    }

    override fun push(item: Transaction) {
        // failsafe: read everything if the array list is empty
        // it possibly does not have the updated list when changing contexts
        if (transactions.size == 0) transactions = readAll()
        transactions.add(item)
        save()
    }

    override fun delete(index: Int) {
        // failsafe: read everything if the array list is empty
        // it possibly does not have the updated list when changing contexts
        if (transactions.size == 0) transactions = readAll()
        transactions.removeAt(index)
        save()
    }

    // internal storage functions
    override fun readAll(): ArrayList<Transaction> {
        try {
            appContext.openFileInput(fileName).bufferedReader().use { reader ->
                val content = reader.readText()
                Log.d("TransactionDateStore", "file content: $content")

                val jsonArray = JSONArray(content)
                return ArrayList(List(jsonArray.length()) { index ->
                    Transaction.fromJson(jsonArray.getJSONObject(index), index)
                })
            }
        } catch (e: FileNotFoundException) {
            Log.i("TransactionDataStore", "file not initialized yet")
            return ArrayList()
        }
    }

    fun read(fromDate: Date, toDate: Date): ArrayList<Transaction> {
        val allTransactions = readAll()
        return allTransactions.filter { it.date in fromDate.time..toDate.time } as ArrayList<Transaction>
    }

    fun readLastMonth(): ArrayList<Transaction> {
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

        return read(fromDate, toDate)
    }

    override fun save() {
        val jsonArray = JSONArray()
        transactions.forEach { jsonArray.put(it.toJson()) }
        try {
            appContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it?.write(jsonArray.toString().toByteArray())
                Log.i("TransactionDateStore", "File saved")

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}