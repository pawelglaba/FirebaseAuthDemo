package com.example.firebaseauthdemo.firebase

/**
 * Data class representing a user in the Firestore database.
 */
data class User(
    val id: String = "",
    val name: String? = null,
    val email: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val address: Map<String, String> = mapOf(),
    val interests: List<String> = listOf(),
    val profilePictureUrl: String = ""
) {
    companion object {
        /**
         * Converts a map of Firestore document data into a User object.
         *
         * @param data The map containing the user data.
         * @return A User object.
         */
        fun fromMap(data: Map<String, Any?>): User {
            return User(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String,
                email = data["email"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                address = (data["address"] as? Map<*, *>)
                    ?.mapNotNull { (key, value) ->
                        if (key is String && value is String) key to value else null
                    }?.toMap() ?: mapOf(),
                interests = (data["interests"] as? List<*>)
                    ?.mapNotNull { it as? String } ?: listOf(),
                profilePictureUrl = data["profilePictureUrl"] as? String ?: ""
            )
        }
    }
}


