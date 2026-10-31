package com.example.myapplication.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(AuthException(e.toFriendlyMessage()))
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Unit> = try {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
        user?.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(name.trim())
                .build()
        )?.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(AuthException(e.toFriendlyMessage()))
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(AuthException(e.toFriendlyMessage()))
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email.trim()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(AuthException(e.toFriendlyMessage()))
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override fun logout() = auth.signOut()

    private fun Exception.toFriendlyMessage(): String =
        when ((this as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address"
            "ERROR_WRONG_PASSWORD",
            "INVALID_LOGIN_CREDENTIALS",
            "ERROR_INVALID_CREDENTIAL" -> "Wrong email or password"
            "ERROR_USER_NOT_FOUND" -> "No account found with this email"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
            "ERROR_WEAK_PASSWORD" -> "Password is too weak"
            "ERROR_NETWORK_REQUEST_FAILED" -> "No internet connection"
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts, try again later"
            else -> message ?: "Something went wrong, please try again"
        }
}
