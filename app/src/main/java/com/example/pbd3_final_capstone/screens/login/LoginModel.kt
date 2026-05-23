package com.example.pbd3_final_capstone.screens.login

import com.example.pbd3_final_capstone.app.MyRoutineApp

class LoginModel(private val app: MyRoutineApp) {
    fun validate(username: String, password: String): ValidationResult {
        val trimmedUser = username.trim()

        // Retrieve the registered credentials
        val registeredUser = app.getRegisteredUsername()
        val registeredPass = app.getRegisteredPassword()

        return when {
            trimmedUser.isBlank() -> ValidationResult.Error("Name cannot be empty.")
            password.isBlank() -> ValidationResult.Error("Password cannot be empty.")
            // Compare input with registered credentials
            trimmedUser != registeredUser || password != registeredPass ->
                ValidationResult.Error("Invalid username or password.")
            else -> ValidationResult.Success(trimmedUser)
        }
    }

    sealed class ValidationResult {
        data class Success(val name: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}