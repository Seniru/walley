package com.seniru.walley.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.seniru.walley.utils.ValidationResult
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId.systemDefault

class Transaction(
    val title: String,
    val amount: Float?,
    val type: String,
    val category: String,
    val date: Long
) {

    fun validateTitle(): ValidationResult {
        return if (title.isEmpty()) {
            ValidationResult.Empty("Please specify a title")
        } else if (title.length > 50) {
            ValidationResult.Invalid("Title cannot be more than 50 characters")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateAmount(): ValidationResult {
        return if (amount == null) {
            ValidationResult.Empty("Amount cannot be empty")
        } else if (amount < 0) {
            ValidationResult.Invalid("Amount cannot be negative")
        } else {
            ValidationResult.Valid
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateDate(): ValidationResult {
        val today = LocalDate.now(systemDefault())
        val transactionDate = Instant.ofEpochMilli(date).atZone(systemDefault()).toLocalDate()

        return if (today.isBefore(transactionDate)) {
            ValidationResult.Invalid("Please enter a date prior to today")
        } else {
            ValidationResult.Valid
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun validate(): ValidationResult {
        val titleValidation = validateTitle()
        if (titleValidation is ValidationResult.Invalid || titleValidation is ValidationResult.Empty) {
            return titleValidation
        }

        val amountValidation = validateAmount()
        if (amountValidation is ValidationResult.Invalid || amountValidation is ValidationResult.Empty) {
            return amountValidation
        }

        val dateValidation = validateDate()
        if (dateValidation is ValidationResult.Invalid) {
            return dateValidation
        }

        return ValidationResult.Valid
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("title", title)
            put("amount", amount)
            put("type", type)
            put("category", category)
            put("date", date)
        }
    }

    companion object {
        fun fromJson(jsonObject: JSONObject): Transaction {
            return Transaction(
                title = jsonObject.getString("title"),
                amount = jsonObject.getDouble("amount").toFloat(),
                type = jsonObject.getString("type"),
                category = jsonObject.getString("category"),
                date = jsonObject.getLong("date")
            )
        }
    }

}