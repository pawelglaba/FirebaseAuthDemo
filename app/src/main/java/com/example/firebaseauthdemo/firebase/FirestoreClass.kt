package com.example.firebaseauthdemo.firebase

import com.example.firebaseauthdemo.RegisterActivity
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
    fun registerUserFS(activity: RegisterActivity, userInfo: User) {

        // Add the user to the "users" collection with their ID as the document ID.
        mFireStore.collection("users")
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Notify the activity of successful registration.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener {
                // Handle the failure case (add appropriate handling if necessary).
            }
    }
}


