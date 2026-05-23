package com.example.pbd3_final_capstone.screens.register

class RegisterModel {
    fun validate(username: String, pass: String, confirm: String): ValidationResult {
        val trimmedUser = username.trim()
        return when {
            trimmedUser.isBlank() -> ValidationResult.Error("Username cannot be empty.")
            pass.isBlank() -> ValidationResult.Error("Password cannot be empty.")
            pass.length < 3 -> ValidationResult.Error("Password must be at least 4 characters.")
            pass != confirm -> ValidationResult.Error("Passwords do not match.")
            else -> ValidationResult.Success(trimmedUser, pass)
        }
    }

    sealed class ValidationResult {
        data class Success(val name: String, val pass: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}