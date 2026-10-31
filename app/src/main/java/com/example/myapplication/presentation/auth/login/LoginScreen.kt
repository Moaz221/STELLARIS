package com.example.myapplication.presentation.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.presentation.auth.components.AuthBackground
import com.example.myapplication.presentation.auth.components.AuthHeader
import com.example.myapplication.presentation.auth.components.AuthTextField
import com.example.myapplication.presentation.auth.components.ErrorBanner
import com.example.myapplication.presentation.auth.components.GlassCard
import com.example.myapplication.presentation.auth.components.GoogleButton
import com.example.myapplication.presentation.auth.components.PrimaryButton
import com.example.myapplication.presentation.auth.launchGoogleSignInIntent
import com.example.myapplication.presentation.auth.rememberGoogleSignInLauncher
import com.example.myapplication.presentation.theme.StellarTeal
import com.example.myapplication.presentation.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focus = LocalFocusManager.current
    val context = LocalContext.current

    val googleLauncher = rememberGoogleSignInLauncher(
        onSuccess = { idToken ->
            viewModel.onGoogleSignIn(idToken, onLoginSuccess)
        },
        onError = viewModel::setError,
    )

    val submit = {
        focus.clearFocus()
        viewModel.login(onLoginSuccess)
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            AuthHeader(
                title = "Welcome Back",
                subtitle = "Sign in to continue your analysis",
            )

            Spacer(Modifier.height(32.dp))

            GlassCard {
                AuthTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Email",
                    icon = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email,
                )

                Spacer(Modifier.height(14.dp))

                AuthTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Password",
                    icon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = state.passwordVisible,
                    onTogglePassword = viewModel::togglePassword,
                    imeAction = ImeAction.Done,
                    onImeAction = submit,
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = "Forgot password?",
                        color = StellarTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            focus.clearFocus()
                            viewModel.openForgotPasswordDialog()
                        },
                    )
                }

                Spacer(Modifier.height(16.dp))
                ErrorBanner(state.error)
                Spacer(Modifier.height(16.dp))

                PrimaryButton(
                    text = "Sign In",
                    isLoading = state.isLoading,
                    onClick = submit,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.15f),
                    )
                    Text(
                        text = "  OR  ",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.15f),
                    )
                }

                Spacer(Modifier.height(16.dp))

                GoogleButton(
                    text = "Sign in with Google",
                    isLoading = state.isLoading,
                    onClick = {
                        focus.clearFocus()
                        launchGoogleSignInIntent(
                            context = context,
                            launcher = googleLauncher,
                            onError = viewModel::setError,
                        )
                    },
                )
            }

            Spacer(Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New to STELLARIS? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "Create account",
                    color = StellarTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onNavigateToSignUp),
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        if (state.showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = viewModel::closeForgotPasswordDialog,
                containerColor = Color(0xFF0F1A2A),
                title = {
                    Text(
                        text = "Reset Password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your email address to receive a password reset link.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(16.dp))
                        AuthTextField(
                            value = state.resetEmail,
                            onValueChange = viewModel::onResetEmailChange,
                            label = "Email",
                            icon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email,
                        )
                        if (state.resetError != null) {
                            Spacer(Modifier.height(12.dp))
                            ErrorBanner(state.resetError)
                        }
                        if (state.resetSuccessMessage != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = state.resetSuccessMessage!!,
                                color = StellarTeal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                },
                confirmButton = {
                    PrimaryButton(
                        text = "Send Link",
                        isLoading = state.isResetLoading,
                        onClick = viewModel::sendPasswordResetEmail,
                    )
                },
                dismissButton = {
                    TextButton(onClick = viewModel::closeForgotPasswordDialog) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
            )
        }
    }
}

