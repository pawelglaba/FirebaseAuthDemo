package com.example.firebaseauthdemo

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.firebaseauthdemo.firebase.FirestoreClass
import com.example.firebaseauthdemo.firebase.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

/**
 * The main activity of the app where user information is displayed and managed.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var phoneInput: EditText
    private lateinit var dobText: TextView
    private lateinit var addressInput: EditText
    private lateinit var interestsInput: EditText
    private lateinit var updateButton: Button
    private lateinit var submitButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var dobButton: Button
    private lateinit var ageText: TextView

    private var userName: String? = null
    private var selectedDateOfBirth: String = ""
    private var selectedImageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance() // Firebase authentication instance
    private val firestoreClass = FirestoreClass() // Firestore interaction helper class



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeUI()

        // Load existing user data if the user is logged in
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserData(userId)
        }

        // Open gallery to select a profile picture
        selectImageButton.setOnClickListener { openGallery() }

        // Open DatePicker to select date of birth
        dobButton.setOnClickListener { openDatePicker() }

        // Save user data to Firestore
        submitButton.setOnClickListener {
            lifecycleScope.launch {
                saveUserData()
            }
        }

        // Navigate to the UpdateDataActivity
        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateDataActivity::class.java)
            updateDataLauncher.launch(intent)
        }
    }

    private val updateDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    loadUserData(userId) // Reload user data after update
                }
            }
        }

    /**
     * Initialize UI components by finding their views.
     */
    private fun initializeUI() {
        profileImageView = findViewById(R.id.profileImageView)
        phoneInput = findViewById(R.id.phoneInput)
        dobText = findViewById(R.id.dobText)
        addressInput = findViewById(R.id.addressInput)
        interestsInput = findViewById(R.id.interestsInput)
        updateButton = findViewById(R.id.updateDataButton)
        submitButton = findViewById(R.id.submitButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        dobButton = findViewById(R.id.dobButton)
        ageText = findViewById(R.id.ageText)
    }

    /**
     * Load user data from Firestore and populate the UI.
     *
     * @param userId The unique ID of the user whose data is being loaded.
     */
    private fun loadUserData(userId: String) {
        lifecycleScope.launch {
            try {
                // Fetch user data from Firestore
                val data = firestoreClass.loadUserData(userId)
                if (data != null) {
                    val user = User.fromMap(data) // Convert Firestore data into a User object
                    populateUI(user) // Populate the UI with user data
                } else {
                    Toast.makeText(this@MainActivity, "No user data found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Save user data to Firestore.
     */
    private suspend fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return

        // Parse address input into a structured map
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

        // Split interests into a list
        val interests = interestsInput.text.toString().split(",").map { it.trim() }

        // Fetch existing user data to fill in missing fields
        val data = firestoreClass.loadUserData(userId)
        if (data != null) {
            userName = User.fromMap(data).name.toString()
        }
        if (selectedDateOfBirth == "" && data != null) {
            selectedDateOfBirth = User.fromMap(data).dateOfBirth
        }

        // Create a new User object with updated data
        val user = User(
            email = FirebaseAuth.getInstance().currentUser?.email.toString(),
            name = userName,
            id = userId,
            phoneNumber = phoneInput.text.toString(),
            dateOfBirth = selectedDateOfBirth,
            address = addressMap,
            interests = interests,
            profilePictureUrl = selectedImageUri?.toString() ?: User.fromMap(data!!).profilePictureUrl
        )

        // Save user data to Firestore
        try {
            firestoreClass.registerOrUpdateUser(user)
            Toast.makeText(this@MainActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Populate the UI with user data.
     *
     * @param user The User object containing the data to display.
     */
    /**
     * Populate the UI with user data.
     *
     * @param user The User object containing the data to display.
     */
    private fun populateUI(user: User) {
        try {
            phoneInput.setText(user.phoneNumber)

            // Convert address map to a single string
            val address = user.address.values.joinToString(", ")
            addressInput.setText(address)

            interestsInput.setText(user.interests.joinToString(", "))

            // Set date of birth in dobText and calculate/display age
            if (user.dateOfBirth.isNotEmpty()) {
                dobText.text = user.dateOfBirth
                val age = calculateAge(user.dateOfBirth)
                ageText.text = "Age: $age"
            } else {
                dobText.text = "Select your date of birth"
                ageText.text = ""
            }

            // Load the profile picture into the ImageView using Glide
            if (user.profilePictureUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(user.profilePictureUrl))
                    .placeholder(R.drawable.ball) // Placeholder image
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.ball) // Default image
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error displaying user data.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open the gallery for profile picture selection.
     */
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView.setImageURI(it) // Show the selected image
            }
        }

    /**
     * Open a DatePicker dialog to select the date of birth.
     */
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDateOfBirth = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                dobText.text = selectedDateOfBirth

                // Calculate and display age
                val age = calculateAge(selectedDateOfBirth)
                ageText.text = "Age: $age"
            },
            year,
            month,
            day
        )

        // Restrict selection to past dates only
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    /**
     * Calculate age based on the date of birth.
     *
     * @param dateOfBirth The date of birth in the format "YYYY-MM-DD".
     * @return The calculated age.
     */
    private fun calculateAge(dateOfBirth: String): Int {
        val parts = dateOfBirth.split("-")
        if (parts.size != 3) return 0

        val birthYear = parts[0].toInt()
        val birthMonth = parts[1].toInt()
        val birthDay = parts[2].toInt()

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthYear

        // Adjust age if the current date is before the birthday
        if (today.get(Calendar.MONTH) < birthMonth - 1 ||
            (today.get(Calendar.MONTH) == birthMonth - 1 && today.get(Calendar.DAY_OF_MONTH) < birthDay)
        ) {
            age--
        }

        return age
    }
}
