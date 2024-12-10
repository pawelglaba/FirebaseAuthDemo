package com.example.firebaseauthdemo

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.firebaseauthdemo.firebase.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

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
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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



        // Load existing user data
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserData(userId)
        }

        // Select profile picture
        selectImageButton.setOnClickListener {
            openGallery()
        }

        // Handle DatePicker for date of birth
        dobButton.setOnClickListener {
            openDatePicker()
        }

        // Handle submit button click
        submitButton.setOnClickListener {
            saveUserData(userName)
        }

        // Handle update button click
        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateDataActivity::class.java)
            updateDataLauncher.launch(intent)
        }
    }

    private fun loadUserData(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") // Pobierz pole "name"
                    userName = name // Przypisz wartość do zmiennej globalnej
                    Toast.makeText(this, "User name: $userName", Toast.LENGTH_SHORT).show() // Debugowanie

                    // Opcjonalnie: wywołanie metody `populateUI` dla innych danych
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        populateUI(user)
                    }
                } else {
                    Toast.makeText(this, "No user data found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateUI(user: User) {

        phoneInput.setText(user.phoneNumber)
        dobText.text = user.dateOfBirth
        selectedDateOfBirth = user.dateOfBirth

        val address = user.address.values.joinToString(", ")
        addressInput.setText(address)

        interestsInput.setText(user.interests.joinToString(", "))

        if (user.dateOfBirth.isNotEmpty()) {
            val age = calculateAge(user.dateOfBirth)
            ageText.text = "Age: $age"
        }

        if (user.profilePictureUrl.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.profilePictureUrl))
                .into(profileImageView)
        }
    }

    private fun saveUserData(userName: String?) {
        val userId = auth.currentUser?.uid ?: return

        val addressParts = addressInput.text.toString().split(",").map { it.trim() }
        val addressMap = if (addressParts.size == 3) {
            mapOf(
                "city" to addressParts[0],
                "street" to addressParts[1],
                "postcode" to addressParts[2]
            )
        } else {
            mapOf<String, String>()
        }

        val interests = interestsInput.text.toString().split(",").map { it.trim() }

        val user = User(
            email = FirebaseAuth.getInstance().currentUser?.email.toString(),
            name = userName,
            id = userId,
            phoneNumber = phoneInput.text.toString(),
            dateOfBirth = selectedDateOfBirth,
            address = addressMap,
            interests = interests,
            profilePictureUrl = selectedImageUri?.toString() ?: ""
        )

        firestore.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView.setImageURI(it) // Show the selected image
            }
        }

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
        datePickerDialog.show()
    }

    private fun calculateAge(dateOfBirth: String): Int {
        val parts = dateOfBirth.split("-")
        if (parts.size != 3) return 0

        val birthYear = parts[0].toInt()
        val birthMonth = parts[1].toInt()
        val birthDay = parts[2].toInt()

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthYear

        if (today.get(Calendar.MONTH) < birthMonth - 1 ||
            (today.get(Calendar.MONTH) == birthMonth - 1 && today.get(Calendar.DAY_OF_MONTH) < birthDay)) {
            age--
        }

        return age
    }
}
