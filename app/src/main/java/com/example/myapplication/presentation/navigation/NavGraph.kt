package com.example.myapplication.presentation.navigation

import android.content.ContextWrapper
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.auth.FirebaseAuthRepository
import com.example.myapplication.data.local.AppPreferences
import com.example.myapplication.presentation.auth.login.LoginScreen
import com.example.myapplication.presentation.auth.signup.SignUpScreen
import com.example.myapplication.presentation.main.MainScreen
import com.example.myapplication.presentation.onboarding.OnboardingScreen
import com.example.myapplication.presentation.splash.SplashScreen
import com.example.myapplication.presentation.theme.MyApplicationTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.flow.first

private const val NAVIGATION_LOG_TAG = "StellarisNavigation"

private fun findActivityResultRegistryOwner(context: android.content.Context): ActivityResultRegistryOwner? {
    var ctx = context
    while (ctx is ContextWrapper) {
        if (ctx is ActivityResultRegistryOwner) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    return ctx as? ActivityResultRegistryOwner
}

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")
}

@Composable
fun StellarisNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val configuration = LocalConfiguration.current

    val preferences = remember(applicationContext) {
        AppPreferences(applicationContext)
    }

    val savedLanguage by preferences.languageCode.collectAsState(initial = "ar")

    val appCompatLanguage = remember {
        AppCompatDelegate.getApplicationLocales()[0]?.language
    }

    var currentLanguageCode by rememberSaveable(savedLanguage, appCompatLanguage) {
        mutableStateOf(appCompatLanguage ?: savedLanguage)
    }

    val authRepo = remember {
        FirebaseAuthRepository()
    }

    val scope = rememberCoroutineScope()

    val isDarkTheme by preferences.isDarkTheme.collectAsState(initial = true)

    val locale = remember(currentLanguageCode) { Locale.forLanguageTag(currentLanguageCode) }
    val localizedConfig = remember(currentLanguageCode, configuration) {
        Configuration(configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
    }
    val localizedContext = remember(currentLanguageCode, context) {
        context.createConfigurationContext(localizedConfig)
    }

    // Make the real activity locale match the saved language so dialogs/sheets (separate windows) use it too.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            val saved = preferences.languageCode.first()
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
        }
    }

    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
        ?: remember(context) { findActivityResultRegistryOwner(context) }

    val providers = remember(localizedConfig, localizedContext, activityResultRegistryOwner) {
        buildList {
            add(LocalConfiguration provides localizedConfig)
            add(LocalContext provides localizedContext)
            if (activityResultRegistryOwner != null) {
                add(LocalActivityResultRegistryOwner provides activityResultRegistryOwner)
            }
        }.toTypedArray()
    }

    CompositionLocalProvider(*providers) {
        MyApplicationTheme(darkTheme = isDarkTheme) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        preferences = preferences,

                        onNavigateToOnboarding = {
                            navController.navigateFrom(
                                source = Screen.Splash,
                                destination = Screen.Onboarding,
                                removeSource = true
                            )
                        },

                        onNavigateToLogin = {
                            if (navController.isCurrentScreen(Screen.Splash)) {
                                val destination = if (authRepo.isLoggedIn()) {
                                    Screen.Home
                                } else {
                                    Screen.Login
                                }

                                navController.navigateFrom(
                                    source = Screen.Splash,
                                    destination = destination,
                                    removeSource = true
                                )
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    var isFinishingOnboarding by remember {
                        mutableStateOf(false)
                    }

                    OnboardingScreen(
                        onFinish = {
                            if (
                                !isFinishingOnboarding &&
                                navController.isCurrentScreen(Screen.Onboarding)
                            ) {
                                isFinishingOnboarding = true

                                scope.launch {
                                    try {
                                        preferences.setOnboardingDone()

                                        val destination = if (authRepo.isLoggedIn()) {
                                            Screen.Home
                                        } else {
                                            Screen.Login
                                        }

                                        navController.navigateFrom(
                                            source = Screen.Onboarding,
                                            destination = destination,
                                            removeSource = true
                                        )
                                    } catch (exception: CancellationException) {
                                        throw exception
                                    } catch (exception: Exception) {
                                        Log.e(
                                            NAVIGATION_LOG_TAG,
                                            "Failed to complete onboarding",
                                            exception
                                        )

                                        val message =
                                            if (
                                                currentLanguageCode.equals(
                                                    "ar",
                                                    ignoreCase = true
                                                )
                                            ) {
                                                "تعذر إكمال الإعداد. حاول مرة أخرى."
                                            } else {
                                                "Couldn't complete setup. Please try again."
                                            }

                                        Toast.makeText(
                                            applicationContext,
                                            message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } finally {
                                        isFinishingOnboarding = false
                                    }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    val loginContent: @Composable () -> Unit = {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigateFrom(
                                    source = Screen.Login,
                                    destination = Screen.Home,
                                    clearBackStack = true
                                )
                            },
                            onNavigateToSignUp = {
                                navController.navigateFrom(
                                    source = Screen.Login,
                                    destination = Screen.SignUp
                                )
                            }
                        )
                    }

                    if (activityResultRegistryOwner != null) {
                        CompositionLocalProvider(
                            LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
                            content = loginContent
                        )
                    } else {
                        loginContent()
                    }
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onSignUpSuccess = {
                            navController.navigateFrom(
                                source = Screen.SignUp,
                                destination = Screen.Home,
                                clearBackStack = true
                            )
                        },

                        onBack = {
                            if (navController.isCurrentScreen(Screen.SignUp)) {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    MainScreen(
                        onSignOut = {
                            authRepo.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onToggleTheme = {
                            scope.launch { preferences.setDarkTheme(!isDarkTheme) }
                        },

                        onToggleLanguage = {
                            val nextLanguage = if (currentLanguageCode.equals("ar", ignoreCase = true)) "en" else "ar"
                            currentLanguageCode = nextLanguage

                            scope.launch {
                                preferences.setLanguageCode(nextLanguage)
                            }

                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(nextLanguage)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Ignores callbacks from screens that are no longer current.
 */
private fun NavHostController.isCurrentScreen(
    screen: Screen
): Boolean {
    return currentDestination?.route == screen.route
}

/**
 * Centralizes navigation and back-stack handling.
 */
private fun NavHostController.navigateFrom(
    source: Screen,
    destination: Screen,
    removeSource: Boolean = false,
    clearBackStack: Boolean = false
) {
    if (!isCurrentScreen(source)) {
        return
    }

    navigate(destination.route) {
        launchSingleTop = true

        when {
            clearBackStack -> {
                popUpTo(graph.id) {
                    inclusive = true
                }
            }

            removeSource -> {
                popUpTo(source.route) {
                    inclusive = true
                }
            }
        }
    }
}