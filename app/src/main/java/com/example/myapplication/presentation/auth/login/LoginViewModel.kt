package com.example.myapplication.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.auth.AuthRepository
import com.example.myapplication.data.auth.FirebaseAuthRepository
import com.example.myapplication.presentation.auth.AuthUiState
import com.example.myapplication.presentation.auth.validateLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun togglePassword() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value

        validateLogin(current.email, current.password)?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.login(current.email, current.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onGoogleSignIn(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.signInWithGoogle(idToken)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun setError(message: String) {
        _state.update { it.copy(error = message, isLoading = false) }
    }

    fun openForgotPasswordDialog() {
        _state.update {
            it.copy(
                showForgotPasswordDialog = true,
                resetEmail = it.email,
                resetError = null,
                resetSuccessMessage = null
            )
        }
    }

    fun closeForgotPasswordDialog() {
        _state.update {
            it.copy(
                showForgotPasswordDialog = false,
                resetError = null,
                resetSuccessMessage = null
            )
        }
    }

    fun onResetEmailChange(value: String) {
        _state.update { it.copy(resetEmail = value, resetError = null) }
    }

    fun sendPasswordResetEmail() {
        val email = _state.value.resetEmail.trim()
        if (!com.example.myapplication.presentation.auth.isValidEmail(email)) {
            _state.update { it.copy(resetError = "Enter a valid email address") }
            return
        }

        val currentTime = System.currentTimeMillis()
        val lastTime = _state.value.lastResetTimestamp
        val cooldownMillis = 5 * 60 * 1000L // 5 minutes

        if (lastTime > 0 && (currentTime - lastTime) < cooldownMillis) {
            val remainingSeconds = (cooldownMillis - (currentTime - lastTime)) / 1000
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeString = if (minutes > 0) "$minutes دقيقة و $seconds ثانية" else "$seconds ثانية"
            _state.update {
                it.copy(resetError = "برجاء الانتظار $timeString قبل طلب رابط آخر.")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isResetLoading = true, resetError = null, resetSuccessMessage = null) }

            val message = "لو الايميل بتاعك متسجل هيتبعتلك لينك على الايميل و دور ف ال Spam"

            repository.sendPasswordResetEmail(email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isResetLoading = false,
                            lastResetTimestamp = currentTime,
                            resetSuccessMessage = message
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isResetLoading = false,
                            lastResetTimestamp = currentTime,
                            resetSuccessMessage = message
                        )
                    }
                }
        }
    }
}
