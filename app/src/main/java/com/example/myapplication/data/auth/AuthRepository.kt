package com.example.myapplication.data.auth

class AuthException(message: String) : Exception(message)

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun isLoggedIn(): Boolean
    fun logout()
}
