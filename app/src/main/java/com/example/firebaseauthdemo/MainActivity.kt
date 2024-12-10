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

    private val auth = FirebaseAuth.getInstance()
    private val firestoreClass = FirestoreClass()

    private val updateDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    loadUserData(userId)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeUI()

        // Load existing user data
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserData(userId)
        }

        // Handle profile picture selection
        selectImageButton.setOnClickListener { openGallery() }

        // Handle DatePicker for date of birth
        dobButton.setOnClickListener { openDatePicker() }

        // Handle save button click
        submitButton.setOnClickListener {
            lifecycleScope.launch {
            saveUserData() }}

        // Handle update button click
        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateDataActivity::class.java)
            updateDataLauncher.launch(intent)
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

    private fun loadUserData(userId: String) {
        lifecycleScope.launch {
            try {
                val data = firestoreClass.loadUserData(userId) // Suspend function
                if (data != null) {
                    val user = User.fromMap(data)
                    populateUI(user)
                } else {
                    Toast.makeText(this@MainActivity, "No user data found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun saveUserData() {
        val userId = auth.currentUser?.uid ?: return

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

        val interests = interestsInput.text.toString().split(",").map { it.trim() }




        val data = firestoreClass.loadUserData(userId) // Suspend function
        if (data != null) {
            userName = User.fromMap(data).name.toString()
        }
        if (selectedDateOfBirth=="" && User.fromMap(data!!).dateOfBirth!=""){
            selectedDateOfBirth = User.fromMap(data).dateOfBirth
        }


        val user = User(
            email = FirebaseAuth.getInstance().currentUser?.email.toString(),
            name = userName,
            id = userId,
            phoneNumber = phoneInput.text.toString(),
            dateOfBirth = selectedDateOfBirth,
            address = addressMap,
            interests = interests,
            profilePictureUrl = selectedImageUri?.toString() ?: User.fromMap(data!!).profilePictureUrl.toString()
        )

        lifecycleScope.launch {
            try {
                firestoreClass.registerOrUpdateUser(user) // Suspend function
                Toast.makeText(this@MainActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Populate the UI with user data.
     *
     * @param user The User object containing the data to display.
     */
    private fun populateUI(user: User) {
        try {
            // Log user data for debugging
            println("Populating UI with user: $user")
            phoneInput.setText(user.phoneNumber)

            val address = user.address.values.joinToString(", ")
            addressInput.setText(address)

            interestsInput.setText(user.interests.joinToString(", "))

            if (user.profilePictureUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(user.profilePictureUrl))
                    .placeholder(R.drawable.ball)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.ball)
            }
        } catch (e: Exception) {
            // Log error and show a message to the user
            println("Error populating UI: ${e.message}")
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

                val age = calculateAge(selectedDateOfBirth)
                ageText.text = "Age: $age"
            },
            year,
            month,
            day
        )

        // Set maximum date to today to prevent future dates
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

        if (today.get(Calendar.MONTH) < birthMonth - 1 ||
            (today.get(Calendar.MONTH) == birthMonth - 1 && today.get(Calendar.DAY_OF_MONTH) < birthDay)
        ) {
            age--
        }

        return age
    }
}
