package com.seniru.walley.persistence

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.seniru.walley.models.Category
import com.seniru.walley.models.Transaction
import org.json.JSONArray
import java.io.FileNotFoundException
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.O)
class CategoryDataStore private constructor(context: Context) : DataStorable<Category> {
    private var appContext = context.applicationContext
    private var categories: ArrayList<Category> = arrayListOf()
    private val fileName = "wally_categories.json"

    init {
        categories = readAll()
    }

    companion object {

        @Volatile
        private var storeInstance: CategoryDataStore? = null

        fun getInstance(context: Context): CategoryDataStore =
            storeInstance ?: synchronized(this) {
                storeInstance ?: CategoryDataStore(context).also { storeInstance = it }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun push(item: Category) {
        // failsafe: read everything if the array list is empty
        // it possibly does not have the updated list when changing contexts
        if (categories.size == 0) categories = readAll()
        categories.add(item)
        save()
    }

    override fun get(index: Int): Category {
        // failsafe: read everything if the array list is empty
        // it possibly does not have the updated list when changing contexts
        if (categories.size == 0) categories = readAll()
        return categories.get(index)
    }

    override fun replace(index: Int, item: Category) {
        // failsafe: read everything if the array list is empty
        // it possibly does not have the updated list when changing contexts
        if (categories.size == 0) categories = readAll()
        categories[index] = item
        save()
    }

    override fun delete(index: Int) {
        // failsafe: read everything if the array list is empty
        // it possibly does not have the updated list when changing contexts
        if (categories.size == 0) categories = readAll()
        categories.removeAt(index)
        save()
    }

    // internal storage functions
    @RequiresApi(Build.VERSION_CODES.O)
    override fun readAll(): ArrayList<Category> {
        try {
            appContext.openFileInput(fileName).bufferedReader().use { reader ->
                val content = reader.readText()
                Log.d("CategoryDataStore.readAll", "file content: $content")

                val jsonArray = JSONArray(content)
                return ArrayList(List(jsonArray.length()) { index ->
                    Category.fromJson(jsonArray.getJSONObject(index))
                })
            }
        } catch (e: FileNotFoundException) {
            Log.i("CategoryDataStore", "file not initialized yet")
            return ArrayList()
        }

    }

    override fun save() {
        val jsonArray = JSONArray()
        categories.forEach { jsonArray.put(it.toJson()) }
        try {
            appContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it?.write(jsonArray.toString().toByteArray())
                Log.i("CategoryDataStore.save", "File saved")

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun clearAll() {
        try {
            appContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it?.write("[]".toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}