package com.example.pbd3_final_capstone.screens.register

import com.example.pbd3_final_capstone.app.MyRoutineApp

class RegisterPresenter(
    private val view: RegisterContract.View,
    private val model: RegisterModel,
    private val app: MyRoutineApp
) : RegisterContract.Presenter {

    override fun onRegisterClicked(username: String, pass: String, confirm: String) {
        view.clearErrors()
        when (val result = model.validate(username, pass, confirm)) {
            is RegisterModel.ValidationResult.Success -> {
                // Save the credentials in your App class (SharedPreferences)
                app.saveRegisteredUser(result.name, result.pass)
                view.showSuccessToast()
                view.navigateToLogin()
            }
            is RegisterModel.ValidationResult.Error -> {
                view.showErrorToast(result.message)
                view.showInputError(result.message)
            }
        }
    }
}