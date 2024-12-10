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
 * Activity to update user data.
 */
class UpdateDataActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var submitButton: Button
    private lateinit var cancelButton: Button
    private lateinit var profileImageView: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_data)

        // Initialize UI components
        initializeUI()

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Load user data using FirestoreClass
            lifecycleScope.launch {
                try {
                    val data = firestoreClass.loadUserData(userId) // Suspend function
                    if (data != null) {
                        val user = User.fromMap(data)
                        populateUI(user)
                    } else {
                        Toast.makeText(this@UpdateDataActivity, "No user data found.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@UpdateDataActivity, "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            if (userId != null) {
                lifecycleScope.launch {
                    updateUserData(userId)
                }
            } else {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle cancel button click
        cancelButton.setOnClickListener {
            finish() // Close the activity without changes
        }
    }

    /**
     * Initialize UI components.
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
        nameInput.setText(user.name ?: "")
        emailInput.setText(user.email)
        phoneInput.setText(user.phoneNumber)

        val address = user.address.values.joinToString(", ")
        addressInput.setText(address)

        interestsInput.setText(user.interests.joinToString(", "))

        if (user.profilePictureUrl.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.profilePictureUrl))
                .placeholder(R.drawable.ball) // Optional: Show placeholder while loading
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ball) // Default image
        }
    }

    /**
     * Collect updated data from UI and update it in Firestore.
     *
     * @param userId The ID of the user being updated.
     */
    private suspend fun updateUserData(userId: String) {
        val addressParts = addressInput.text.toString().split(",").map { it.trim() }
        val addressMap = if (addressParts.size == 3) {
            mapOf(
                "city" to addressParts[0],
                "street" to addressParts[1],
                "postcode" to addressParts[2]
            )
        } else {
            mapOf()
        }

        val updatedData = mapOf(
            "name" to nameInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phoneNumber" to phoneInput.text.toString(),
            "address" to addressMap,
            "interests" to interestsInput.text.toString().split(",").map { it.trim() }
        )

        try {
            firestoreClass.updateUserData(userId, updatedData) // Suspend function
            Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK) // Notify MainActivity that data has been updated
            finish() // Close the activity
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to update data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
