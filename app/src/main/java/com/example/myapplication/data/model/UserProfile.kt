package com.example.myapplication.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val initials: String = "",
    val preferredName: String = "",
    val journeyType: String = "EARLY_DETECTION",
    val verificationStatus: String = "USER_CONFIRMED"
)

fun currentUserProfile(): UserProfile {
    val u = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val name = u?.displayName?.takeIf { it.isNotBlank() } ?: u?.email?.substringBefore("@").orEmpty()
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    return UserProfile(id = u?.uid.orEmpty(), name = name, email = u?.email.orEmpty(), initials = initials, preferredName = name.substringBefore(" "))
}
