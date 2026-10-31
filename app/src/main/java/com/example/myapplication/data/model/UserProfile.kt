package com.example.myapplication.data.model

data class UserProfile(
    val id: String = "user_001",
    val name: String = "Moaz Ragab",
    val email: String = "moazragab652@gmail.com",
    val initials: String = "MR",
    val preferredName: String = "Moaz",
    val journeyType: String = "EARLY_DETECTION",
    val verificationStatus: String = "USER_CONFIRMED"
)
