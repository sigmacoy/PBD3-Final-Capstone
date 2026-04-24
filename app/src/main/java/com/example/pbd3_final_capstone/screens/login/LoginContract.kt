package com.example.pbd3_final_capstone.screens.login

interface LoginContract {
    interface View {
        fun showInputError(message: String)
        fun clearErrors()
        fun navigateToTaskList()
        fun showSuccessToast()
        fun showErrorToast(message: String)
    }
    interface Presenter {
        fun onLoginClicked(username: String, password: String)
    }
}