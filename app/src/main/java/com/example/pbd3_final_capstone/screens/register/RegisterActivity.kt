package com.example.pbd3_final_capstone.screens.register

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
import com.example.pbd3_final_capstone.screens.login.LoginActivity
import com.example.pbd3_final_capstone.utils.app
import com.example.pbd3_final_capstone.utils.getButtonView
import com.example.pbd3_final_capstone.utils.getEditTextValue
import com.example.pbd3_final_capstone.utils.start
import com.example.pbd3_final_capstone.utils.toast

class RegisterActivity : AppCompatActivity(), RegisterContract.View {
    private lateinit var presenter: RegisterPresenter
    private lateinit var textviewError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        textviewError = findViewById(R.id.textviewRegisterError)
        presenter = RegisterPresenter(this, RegisterModel(), app())

        getButtonView(R.id.buttonRegister).setOnClickListener {
            val username = getEditTextValue(R.id.edittextRegisterUsername)
            val pass = getEditTextValue(R.id.edittextRegisterPassword)
            val confirm = getEditTextValue(R.id.edittextConfirmPassword)
            presenter.onRegisterClicked(username, pass, confirm)
        }

        findViewById<TextView>(R.id.textviewGoToLogin).setOnClickListener {
            navigateToLogin()
        }
    }

    override fun showInputError(message: String) {
        textviewError.text = message
        textviewError.visibility = View.VISIBLE
    }

    override fun clearErrors() {
        textviewError.text = ""
        textviewError.visibility = View.GONE
    }

    override fun showSuccessToast() = toast("Registration successful!")

    override fun showErrorToast(message: String) = toast("Registration failed: $message")

    override fun navigateToLogin() {
        start(LoginActivity::class.java)
        finish()
    }
}