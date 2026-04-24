package com.example.pbd3_final_capstone.screens.login

import com.example.pbd3_final_capstone.app.MyRoutineApp

class LoginPresenter(
    private val view: LoginContract.View,
    private val model: LoginModel,
    private val app: MyRoutineApp
) : LoginContract.Presenter {

    override fun onLoginClicked(username: String, password: String) {
        view.clearErrors()
        when (val result = model.validate(username, password)) {
            is LoginModel.ValidationResult.Success -> {
                app.saveUser(result.name)
                view.showSuccessToast()
                view.navigateToTaskList()
            }
            is LoginModel.ValidationResult.Error -> {
                view.showErrorToast(result.message)
                view.showInputError(result.message)
            }
        }
    }
}