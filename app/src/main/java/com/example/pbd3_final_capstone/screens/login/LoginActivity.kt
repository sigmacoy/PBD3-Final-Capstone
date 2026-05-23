package com.example.pbd3_final_capstone.screens.login

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pbd3_final_capstone.R
//import com.example.pbd3_final_capstone.screens.tasklist.TaskListActivity
import com.example.pbd3_final_capstone.utils.app
import com.example.pbd3_final_capstone.utils.getButtonView
import com.example.pbd3_final_capstone.utils.getEditTextValue
import com.example.pbd3_final_capstone.utils.start
import com.example.pbd3_final_capstone.utils.toast
import com.example.pbd3_final_capstone.screens.home.HomeActivity

class LoginActivity : AppCompatActivity(), LoginContract.View {
    private lateinit var presenter: LoginPresenter
    private lateinit var textviewError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        textviewError = findViewById(R.id.textviewError)

        // Initialize only once, passing app() to LoginModel
        presenter = LoginPresenter(this, LoginModel(app()), app())

        getButtonView(R.id.buttonLogin).setOnClickListener {
            val username = getEditTextValue(R.id.edittextUsername)
            val password = getEditTextValue(R.id.edittextPassword)
            presenter.onLoginClicked(username, password)
        }

        // Handle the register click
        findViewById<TextView>(R.id.textviewGoToRegister).setOnClickListener {
            start(com.example.pbd3_final_capstone.screens.register.RegisterActivity::class.java)
            finish() // Optional: Removes LoginActivity from the back stack so pressing back doesn't return here
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

    override fun showSuccessToast() = toast("Login successful!")

    override fun showErrorToast(message: String) = toast("Login failed: $message")

    override fun navigateToTaskList() {
        start(HomeActivity::class.java)
    }

}