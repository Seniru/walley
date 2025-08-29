package com.seniru.walley

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.seniru.walley.models.Category
import com.seniru.walley.persistence.AppData
import com.seniru.walley.persistence.CategoryDataStore
import com.seniru.walley.persistence.SharedMemory

class OnboardingActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val importFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val preferences = SharedMemory.getInstance(applicationContext)
            AppData.handleImport(result, applicationContext)
            preferences.setIsInitialized(true)
            finish()
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        findViewById<Button>(R.id.startFreshBtn).setOnClickListener {
            startFresh(applicationContext)
        }

        findViewById<Button>(R.id.restoreBackupBtn).setOnClickListener {
            restore()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startFresh(context: Context) {
        val preferences = SharedMemory.getInstance(context)
        val categoryStore = CategoryDataStore.getInstance(context)
        categoryStore.set(Category.defaults)
        preferences.setIsInitialized(true)
        startActivity(Intent(this, MainActivity::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun restore() {
        AppData.importData(importFileLauncher)
    }

}