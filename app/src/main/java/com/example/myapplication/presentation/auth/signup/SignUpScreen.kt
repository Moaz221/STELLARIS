package com.example.myapplication.presentation.auth.signup

import com.example.myapplication.core.ui.loc
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.myapplication.presentation.theme.TextPrimary
import com.example.myapplication.presentation.theme.TextSecondary

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: SignUpViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val focus = LocalFocusManager.current
    val context = LocalContext.current

    val googleLauncher = rememberGoogleSignInLauncher(
        onSuccess = { idToken ->
            viewModel.onGoogleSignIn(idToken, onSignUpSuccess)
        },
        onError = viewModel::setError,
    )

    val submit = {
        focus.clearFocus()
        viewModel.register(onSignUpSuccess)
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = com.example.myapplication.core.ui.loc("Back", "رجوع"),
                    tint = TextPrimary,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                AuthHeader(
                    title = com.example.myapplication.core.ui.loc("Create Account", "إنشاء حساب"),
                    subtitle = com.example.myapplication.core.ui.loc("Your AI assistant for X-ray analysis starts here", "مساعدك الذكي لتحليل الأشعة بيبدأ من هنا"),
                )

                Spacer(Modifier.height(32.dp))

                GlassCard {
                    AuthTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = loc("Full name", "الاسم الكامل"),
                        icon = Icons.Outlined.Person,
                    )

                    Spacer(Modifier.height(14.dp))

                    AuthTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = loc("Email", "البريد الإلكتروني"),
                        icon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email,
                    )

                    Spacer(Modifier.height(14.dp))

                    AuthTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = loc("Password (min 6 chars)", "كلمة المرور (6 أحرف على الأقل)"),
                        icon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = state.passwordVisible,
                        onTogglePassword = viewModel::togglePassword,
                        imeAction = ImeAction.Done,
                        onImeAction = submit,
                    )

                    Spacer(Modifier.height(16.dp))
                    ErrorBanner(state.error)
                    Spacer(Modifier.height(16.dp))

                    PrimaryButton(
                        text = com.example.myapplication.core.ui.loc("Create Account", "إنشاء حساب"),
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
                        text = com.example.myapplication.core.ui.loc("Sign up with Google", "التسجيل بحساب جوجل"),
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

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
