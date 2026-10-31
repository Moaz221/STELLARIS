package com.example.myapplication.presentation.auth

import android.util.Patterns

data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val resetEmail: String = "",
    val isResetLoading: Boolean = false,
    val resetError: String? = null,
    val resetSuccessMessage: String? = null,
    val lastResetTimestamp: Long = 0L,
)

fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

fun validateLogin(email: String, password: String): String? = when {
    !isValidEmail(email) -> "Enter a valid email address"
    password.isBlank() -> "Enter your password"
    else -> null
}

fun validateSignUp(name: String, email: String, password: String): String? = when {
    name.isBlank() -> "Enter your name"
    !isValidEmail(email) -> "Enter a valid email address"
    password.length < 6 -> "Password must be at least 6 characters"
    else -> null
}
