package com.seniru.walley.models

sealed class ValidationResult {
    data class Empty(val error: String) : ValidationResult()
    data class Invalid(val error: String) : ValidationResult()
    data object Valid : ValidationResult()
}