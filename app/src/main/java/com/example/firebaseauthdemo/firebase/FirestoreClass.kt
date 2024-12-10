package com.example.firebaseauthdemo.firebase


import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * A class that handles Firestore operations using Kotlin coroutines.
 */
class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * Registers or updates a user in Firestore.
     *
     * @param user The user object to save.
     */
    suspend fun registerOrUpdateUser(user: User) {
        try {
            mFireStore.collection("users").document(user.id).set(user).await()
        } catch (e: Exception) {
            throw Exception("Error saving user data: ${e.message}")
        }
    }

    /**
     * Loads user data from Firestore.
     *
     * @param userId The ID of the user whose data is to be loaded.
     * @return A map containing user data, or null if the document does not exist.
     */
    suspend fun loadUserData(userId: String): Map<String, Any>? {
        val documentSnapshot = mFireStore.collection("users")
            .document(userId)
            .get()
            .await() // Suspends until the document is retrieved
        return documentSnapshot.data
    }

    suspend fun updateUserData(userId: String, updatedData: Map<String, Any?>) {

        val filteredData = updatedData.filterValues { value ->
            value != null && !(value is String && value.isBlank())
        }

        if (filteredData.isEmpty()) return

        mFireStore.collection("users")
            .document(userId)
            .update(filteredData)
            .await() // Suspends until the operation is complete
    }
}