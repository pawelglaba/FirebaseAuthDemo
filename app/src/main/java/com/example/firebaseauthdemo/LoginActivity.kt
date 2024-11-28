package com.example.firebaseauthdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth


/**
 * Activity responsible for handling user login using Firebase Authentication.
 */
class LoginActivity : BaseActivity(), View.OnClickListener {

    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize input fields and login button
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.loginButton)

        // Set click listener for the login button
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }

        // Set click listener for registration link
        findViewById<View>(R.id.registerTextViewClickable)?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.registerTextViewClickable -> {
                    // Navigate to the registration screen when the link is clicked
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Validates the login details entered by the user.
     * @return True if the details are valid, otherwise False.
     */
    private fun validateLoginDetails(): Boolean {
        val email = inputEmail?.text.toString().trim { it <= ' ' }
        val password = inputPassword?.text.toString().trim { it <= ' ' }

        return when {
            email.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            password.isEmpty() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                // Optionally show a success message
                true
            }
        }
    }

    /**
     * Logs in a registered user using Firebase Authentication.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }

            // Sign in with FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar("You are logged in successfully.", false)
                        goToMainActivity()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception?.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Navigates to the main activity after successful login and passes the user's UID to the main activity.
     */
    open fun goToMainActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.orEmpty()

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("uID", email)
        }
        startActivity(intent)
    }
}
