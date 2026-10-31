package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.HealthRepository
import com.example.myapplication.data.repository.LocalHealthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class HomeUiState(
    val journeyType: JourneyType? = null,
    val userName: String = "",
    val userEmail: String = "",
    val userInitials: String = "",
    val photoUrl: String? = null,
    val hasUnreadNotifications: Boolean = false,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val healthRepository: HealthRepository = LocalHealthRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        updateUserFromFirebase(auth.currentUser)
    }

    init {
        // Fetch current user immediately
        updateUserFromFirebase(firebaseAuth.currentUser)
        // Listen for auth state changes
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    private fun updateUserFromFirebase(user: FirebaseUser?) {
        viewModelScope.launch {
            if (user != null) {
                // Reload to get the latest profile data
                user.reload().addOnCompleteListener {
                    val refreshedUser = firebaseAuth.currentUser
                    val displayName = refreshedUser?.displayName
                        ?: refreshedUser?.email?.substringBefore("@")
                        ?: ""
                    val email = refreshedUser?.email ?: ""
                    val initials = computeInitials(displayName.ifBlank { email })

                    _uiState.value = _uiState.value.copy(
                        userName = displayName,
                        userEmail = email,
                        userInitials = initials,
                        photoUrl = refreshedUser?.photoUrl?.toString(),
                        isLoading = false
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    userName = "",
                    userEmail = "",
                    userInitials = "",
                    photoUrl = null,
                    isLoading = false
                )
            }
        }
    }

    private fun computeInitials(name: String): String {
        return name
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .uppercase(Locale.ROOT)
    }

    fun selectJourney(journeyType: JourneyType) {
        _uiState.value = _uiState.value.copy(journeyType = journeyType)
    }

    fun refreshUser() {
        updateUserFromFirebase(firebaseAuth.currentUser)
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}