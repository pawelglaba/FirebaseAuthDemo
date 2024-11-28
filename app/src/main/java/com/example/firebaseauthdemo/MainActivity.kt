package com.example.firebaseauthdemo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Main activity of the application, displaying a welcome message to the user.
 */
class MainActivity : AppCompatActivity() {

    // TextView to display the welcome message
    private var welcomeTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Retrieve the user ID passed from the previous activity
        val userID = intent.getStringExtra("uID")

        // Initialize the welcome text field and set the welcome message
        welcomeTextView = findViewById(R.id.welcomeText)
        welcomeTextView?.text = "Welcome ${userID}!"
    }
}