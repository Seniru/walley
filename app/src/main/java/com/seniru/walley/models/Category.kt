package com.seniru.walley.models

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorInt
import androidx.core.graphics.toColorLong
import com.seniru.walley.utils.ValidationResult
import org.json.JSONObject

class Category(
    val name: String,
    val spendingLimit: Float?,
    val color: Color?,
    val icon: String,
    val index: Int? = null
) {

    fun validateName(): ValidationResult {
        return if (name.isEmpty()) {
            ValidationResult.Empty("Please specify a name")
        } else if (name.length > 50) {
            ValidationResult.Invalid("Name cannot be more than 50 characters")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateSpendingLimit(): ValidationResult {
        return if (spendingLimit == null) {
            ValidationResult.Empty("Amount cannot be empty")
        } else if (spendingLimit < 0) {
            ValidationResult.Invalid("Amount cannot be negative")
        } else {
            ValidationResult.Valid
        }
    }

    fun validate(): ValidationResult {
        val categoryNameValidation = validateName()
        if (categoryNameValidation is ValidationResult.Empty || categoryNameValidation is ValidationResult.Invalid) {
            return categoryNameValidation
        }

        val spendingLimitValidation = validateSpendingLimit()
        if (spendingLimitValidation is ValidationResult.Empty || spendingLimitValidation is ValidationResult.Invalid) {
            return spendingLimitValidation
        }

        return ValidationResult.Valid

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("limit", spendingLimit)
            put("color", color?.toArgb())
            put("icon", icon)
        }
    }

    companion object {

        @RequiresApi(Build.VERSION_CODES.O)
        val defaults = arrayListOf(
            Category("Health", 0f, (0xFF4500 or (0xFF shl 24)).toColor(), "\uf0fa", 5),
            Category("Food", 0f, (0xFFA500 or (0xFF shl 24)).toColor(), "\uf2e7", 1),
            Category("Utilities", 0f, (0xFFFF00 or (0xFF shl 24)).toColor(), "\uf084", 4),
            Category("Transport", 0f, (0xADFF2F or (0xFF shl 24)).toColor(), "\uf018", 0),
            Category("Groceries", 0f, (0x00FF7F or (0xFF shl 24)).toColor(), "\uf291", 3),
            Category("Education", 0f, (0x1E90FF or (0xFF shl 24)).toColor(), "\uf19d", 6),
            Category("Entertainment", 0f, (0x9932CC or (0xFF shl 24)).toColor(), "\uf630", 2),
            Category("Other", 0f, (0xFF1493 or (0xFF shl 24)).toColor(), "\uf86d", 7),
            Category("Income", 0f, (0x00FF00 or (0xFF shl 24)).toColor(), "\uf4c0", 8),
        )

        @RequiresApi(Build.VERSION_CODES.O)
        fun fromJson(jsonObject: JSONObject, index: Int?): Category {
            return Category(
                name = jsonObject.getString("name"),
                spendingLimit = jsonObject.getDouble("limit").toFloat(),
                color = Color.valueOf(jsonObject.getInt("color")),
                icon = jsonObject.getString("icon"),
                index = index
            )
        }
    }

}