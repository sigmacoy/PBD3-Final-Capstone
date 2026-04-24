package com.example.pbd3_final_capstone.screens.login

class LoginModel {
    fun validate(username: String, password: String): ValidationResult {
        val trimmedUser = username.trim()
        return when {
            trimmedUser.isBlank() -> ValidationResult.Error("Name cannot be empty.")
            password.isBlank() -> ValidationResult.Error("Password cannot be empty.")
            password.length < 4 -> ValidationResult.Error("Password is too short.")
            else -> ValidationResult.Success(trimmedUser)
        }
    }

    sealed class ValidationResult {
        data class Success(val name: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}