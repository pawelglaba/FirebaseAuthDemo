package com.example.firebaseauthdemo.firebase

import android.app.Activity
import android.widget.Toast
import com.example.firebaseauthdemo.MainActivity
import com.example.firebaseauthdemo.RegisterActivity
import com.example.firebaseauthdemo.UpdateDataActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * A class that handles Firestore operations.
 */
class FirestoreClass {

    // Instance of FirebaseFirestore to interact with the Firestore database.
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * Registers a new user in the Firestore database.
     *
     * @param activity The activity instance where this function is called, used to handle success and failure callbacks.
     * @param userInfo An object of type `User` containing the user's information to be stored in Firestore.
     */
    fun registerUserFS(activity: Activity, user: User) {
        mFireStore.collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener {
                if (activity is RegisterActivity) {
                    activity.userRegistrationSuccess()
                }
            }
            .addOnFailureListener { e ->
                if (activity is RegisterActivity) {
                    activity.showErrorSnackBar("Error while registering the user: ${e.message}", true)
                }
            }
    }

    /**
     * Loads user data from Firestore and populates the UI.
     *
     * @param activity The activity instance used for UI updates.
     * @param userId The unique ID of the user whose data is being loaded.
     * @param onSuccess A lambda to handle success, passing the document data.
     */
    fun loadUserData(
        activity: Activity,
        userId: String,
        onSuccess: (Map<String, Any?>) -> Unit
    ) {
        mFireStore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Pass the document data to the onSuccess callback
                    onSuccess(document.data ?: mapOf())
                } else {
                    Toast.makeText(activity, "No user data found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(activity, "Failed to load user data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }





    /**
     * Updates user data in the Firestore database.
     *
     * @param activity The activity instance used for callbacks.
     * @param userId The unique ID of the user whose data is being updated.
     * @param updatedData A map containing the fields to be updated.
     */
    fun updateUserData(
        activity: Activity,
        userId: String,
        updatedData: Map<String, Any>
    ) {
        mFireStore.collection("users").document(userId)
            .update(updatedData)
            .addOnSuccessListener {
                if (activity is UpdateDataActivity) {
                    Toast.makeText(activity, "User data updated successfully.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                if (activity is UpdateDataActivity) {
                    Toast.makeText(activity, "Failed to update user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
