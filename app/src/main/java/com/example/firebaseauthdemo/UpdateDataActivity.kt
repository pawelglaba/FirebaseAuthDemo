package com.example.firebaseauthdemo

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.firebaseauthdemo.firebase.FirestoreClass
import com.google.firebase.auth.FirebaseAuth

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_data)

        // Initialize UI components
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)
        interestsInput = findViewById(R.id.interestsInput)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton)
        profileImageView = findViewById(R.id.profileImageView)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Load user data using FirestoreClass
            FirestoreClass().loadUserData(this, userId) { data ->

                // Fetch profile picture URL
                val profilePictureUrl = data["profilePictureUrl"] as? String

                // Check and load the profile picture
                loadProfilePicture(profilePictureUrl)

                // Populate all fields with user data
                nameInput.setText(data["name"] as? String ?: "")
                emailInput.setText(data["email"] as? String ?: "")
                phoneInput.setText(data["phoneNumber"] as? String ?: "")

                // Map address correctly
                val addressMap = data["address"] as? Map<String, String>
                if (addressMap != null) {
                    val city = addressMap["city"] ?: ""
                    val street = addressMap["street"] ?: ""
                    val postcode = addressMap["postcode"] ?: ""
                    addressInput.setText("$city, $street, $postcode")
                } else {
                    addressInput.setText("")
                }

                // Set interests
                interestsInput.setText((data["interests"] as? List<*>)?.joinToString(", ") ?: "")
            }
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            updateUserData(userId)
        }

        // Handle cancel button click
        cancelButton.setOnClickListener {
            finish() // Close the activity without changes
        }
    }

    private fun loadProfilePicture(profilePictureUrl: String?) {
        if (!profilePictureUrl.isNullOrEmpty()) {
            // Load image from the provided URL
            Glide.with(this)
                .load(Uri.parse(profilePictureUrl))
                .placeholder(R.drawable.ball) // Optional: Show placeholder while loading
                .into(profileImageView)
        } else {
            // Set default image if no URL is provided
            profileImageView.setImageResource(R.drawable.ball)
        }
    }

    private fun updateUserData(userId: String?) {
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

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

        // Collect updated data
        val updatedData = mapOf(
            "name" to nameInput.text.toString(),
            "email" to emailInput.text.toString(),
            "phoneNumber" to phoneInput.text.toString(),
            "address" to addressMap,
            "interests" to interestsInput.text.toString().split(",").map { it.trim() }
        )

        // Update Firestore data using FirestoreClass
        FirestoreClass().updateUserData(this, userId, updatedData)
        setResult(RESULT_OK) // Notify MainActivity that data has been updated
        finish() // Close the activity
    }
}
