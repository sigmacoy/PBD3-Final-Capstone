package com.example.pbd3_final_capstone.screens.register

interface RegisterContract {
    interface View {
        fun showInputError(message: String)
        fun clearErrors()
        fun navigateToLogin()
        fun showSuccessToast()
        fun showErrorToast(message: String)
    }
    interface Presenter {
        fun onRegisterClicked(username: String, pass: String, confirm: String)
    }
}