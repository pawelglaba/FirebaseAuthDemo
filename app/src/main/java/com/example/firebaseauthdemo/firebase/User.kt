package com.example.firebaseauthdemo.firebase


class User(
    val id: String = "", // Unique identifier for the user
    val name: String? = "", // Name of the user
    val registeredUser: Boolean = false, // Indicates if the user is registered
    val email: String = "", // Email address of the user
    val age: Int = 0, // Age of the user as an integer
    val phoneNumber: String = "", // User's phone number as a string
    val profilePictureUrl: String = "", // URL to the user's profile picture
    val dateOfBirth: String = "", // Date of birth in "YYYY-MM-DD" format
    val address: Map<String, String> = mapOf(), // Address stored as key-value pairs (e.g., city, street, postcode)
    val interests: List<String> = listOf(), // List of user's interests
    val preferences: Map<String, Any> = mapOf() // User preferences stored as a key-value map




)
