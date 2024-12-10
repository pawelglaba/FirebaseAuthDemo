package com.example.firebaseauthdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.firebaseauthdemo.firebase.FirestoreClass
import com.example.firebaseauthdemo.firebase.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Handles user registration with input validation and Firebase Authentication integration.
 */
class RegisterActivity : BaseActivity() {

    // UI Components for the registration form
    private var registerButton: Button? = null
    private var inputEmail: EditText? = null
    private var inputName: EditText? = null
    private var inputPassword: EditText? = null
    private var inputPasswordRepeat: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize input fields and the registration button
        registerButton = findViewById(R.id.registerButton)
        inputEmail = findViewById(R.id.inputEmail)
        inputName = findViewById(R.id.inputName)
        inputPassword = findViewById(R.id.inputPasswordRegister)
        inputPasswordRepeat = findViewById(R.id.inputPasswordRepeat)

        // Set a click listener for the registration button
        registerButton?.setOnClickListener {
            registerUser()
        }
    }

    /**
     * Validates the registration details entered by the user.
     *
     * @return True if the details are valid, otherwise False.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            inputEmail?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
                false
            }

            inputName?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_name), true)
                false
            }

            inputPassword?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
                false
            }

            inputPasswordRepeat?.text.toString().trim { it <= ' ' }.isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_reppassword), true)
                false
            }

            inputPassword?.text.toString().trim { it <= ' ' } != inputPasswordRepeat?.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(getString(R.string.err_msg_password_mismatch), true)
                false
            }

            else -> true
        }
    }

    /**
     * Navigates the user to the login activity.
     *
     * @param view The current view triggering the navigation.
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close this activity to prevent returning to it without restarting the app.
    }

    /**
     * Registers the user using Firebase Authentication.
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val email = inputEmail?.text.toString().trim { it <= ' ' }
            val password = inputPassword?.text.toString().trim { it <= ' ' }
            val name = inputName?.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar(
                            "You are registered successfully. Your user ID is ${firebaseUser.uid}",
                            false
                        )

                        val user = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = email,
                            phoneNumber = "", // Default empty
                            dateOfBirth = "", // Default empty
                            address = mapOf(), // Default empty map
                            interests = listOf(), // Default empty list
                            profilePictureUrl = "" // Default empty
                        )

                        // Save user data to Firestore
                        FirestoreClass().registerUserFS(this@RegisterActivity, user)

                        FirebaseAuth.getInstance().signOut()
                        finish()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Called when the user is successfully registered. Displays a success message using Toast.
     */
    fun userRegistrationSuccess() {
        Toast.makeText(
            this@RegisterActivity,
            getString(R.string.register_success),
            Toast.LENGTH_LONG
        ).show()
    }
}
