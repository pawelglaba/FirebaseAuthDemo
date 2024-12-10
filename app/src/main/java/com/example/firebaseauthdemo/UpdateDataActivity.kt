package com.example.firebaseauthdemo

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.firebaseauthdemo.firebase.FirestoreClass
import com.example.firebaseauthdemo.firebase.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Activity to update user data in the Firestore database.
 */
class UpdateDataActivity : AppCompatActivity() {

    // UI components for collecting and displaying user data
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var profileImageView: ImageView

    // Firebase authentication and Firestore class instances
    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_data)

        // Initialize UI components
        initializeUI()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Load user data asynchronously using FirestoreClass
            lifecycleScope.launch {
                try {
                    // Fetch user data from Firestore
                    val data = firestoreClass.loadUserData(userId) // Suspend function
                    if (data != null) {
                        val user = User.fromMap(data) // Convert Firestore data to a User object
                        populateUI(user) // Populate UI with user data
                    } else {
                        Toast.makeText(this@UpdateDataActivity, "No user data found.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@UpdateDataActivity, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle submit button click to save updated user data
        submitButton.setOnClickListener {
            if (userId != null) {
                lifecycleScope.launch {
                    updateUserData(userId) // Save data asynchronously
                }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle cancel button click to exit without saving
        cancelButton.setOnClickListener {
            finish() // Close the activity without changes
        }
    }

    /**
     * Initialize UI components by finding their views from the layout.
     */
    private fun initializeUI() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)
        interestsInput = findViewById(R.id.interestsInput)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton)
        profileImageView = findViewById(R.id.profileImageView)
    }

    /**
     * Populate the UI with user data.
     *
     * @param user The User object containing the data to display.
     */
    private fun populateUI(user: User) {
        nameInput.setText(user.name ?: "") // Set user name in the EditText
        emailInput.setText(user.email) // Set user email in the EditText
        phoneInput.setText(user.phoneNumber) // Set user phone number in the EditText

        // Convert the address map to a single string and display it
        val address = user.address.values.joinToString(", ")
        addressInput.setText(address)

        // Convert the list of interests to a comma-separated string and display it
        interestsInput.setText(user.interests.joinToString(", "))

        // Load the profile picture using Glide, with a placeholder for fallback
        if (user.profilePictureUrl.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.profilePictureUrl))
                .placeholder(R.drawable.ball) // Placeholder while the image loads
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ball) // Default image if no profile picture is set
        }
    }

    /**
     * Collect updated data from UI and update it in Firestore.
     *
     * @param userId The ID of the user being updated.
     */
    private suspend fun updateUserData(userId: String) {
        // Parse the address from a comma-separated string into a structured map
        val addressParts = addressInput.text.toString().split(",").map { it.trim() }
        val addressMap = if (addressParts.size == 3) {
            mapOf(
                "city" to addressParts[0],
                "street" to addressParts[1],
                "postcode" to addressParts[2]
            )
        } else {
            mapOf() // Default empty map if address format is incorrect
        }

        // Prepare the updated data as a map
        val updatedData = mapOf(
            "name" to nameInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phoneNumber" to phoneInput.text.toString(),
            "address" to addressMap,
            "interests" to interestsInput.text.toString().split(",").map { it.trim() } // Convert comma-separated string to a list
        )

        try {
            // Save the updated data to Firestore
            firestoreClass.updateUserData(userId, updatedData) // Suspend function
            Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK) // Notify the calling activity that data was updated
            finish() // Close the activity
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to update data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
