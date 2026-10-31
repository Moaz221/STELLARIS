package com.example.myapplication.presentation.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

@SuppressLint("DiscouragedApi")
fun getDefaultWebClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    return if (resId != 0) context.getString(resId) else null
}

@Composable
fun rememberGoogleSignInLauncher(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit,
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                onSuccess(idToken)
            } else {
                onError("Failed to retrieve ID token from Google account.")
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google Sign In ApiException code: ${e.statusCode}", e)
            val detailedError = when (e.statusCode) {
                10 -> "DEVELOPER_ERROR (10): SHA-1/SHA-256 fingerprint missing in Firebase Console, or package name mismatch."
                12500 -> "SIGN_IN_FAILED (12500): OAuth consent screen not configured in Google Cloud Console or Google Play Services needs update."
                12501 -> "Sign in was cancelled."
                7 -> "NETWORK_ERROR (7): Please check your internet connection."
                else -> "Google Sign-In failed (Code ${e.statusCode}): ${e.message}"
            }
            if (e.statusCode != 12501) {
                onError(detailedError)
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Sign In unexpected error", e)
            onError(e.message ?: "Google sign in failed")
        }
    }
}

fun launchGoogleSignInIntent(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onError: (String) -> Unit,
) {
    val webClientId = getDefaultWebClientId(context)
    if (webClientId.isNullOrBlank()) {
        onError("Google Sign-In is not configured in google-services.json.")
        return
    }

    try {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            launcher.launch(googleSignInClient.signInIntent)
        }
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Error launching Google sign in intent", e)
        onError(e.message ?: "Failed to launch Google Sign In")
    }
}

suspend fun launchGoogleSignIn(
    context: Context,
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit,
) {
    val webClientId = getDefaultWebClientId(context)
    if (webClientId.isNullOrBlank()) {
        onError("Google Sign-In is not configured in google-services.json. Please enable Google Auth in Firebase Console and download the updated file.")
        return
    }

    try {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )

        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            onSuccess(googleIdTokenCredential.idToken)
        } else {
            onError("Unsupported credential type: ${credential::class.java.name}")
        }
    } catch (e: GetCredentialCancellationException) {
        Log.w("GoogleSignIn", "Google sign in cancelled or failed: ${e.message}", e)
        val msg = e.message.orEmpty()
        if (msg.isNotBlank()) {
            onError("Google sign in cancelled or failed: $msg")
        } else {
            onError("Google sign in was cancelled.")
        }
    } catch (e: NoCredentialException) {
        Log.e("GoogleSignIn", "No credentials available: ${e.message}", e)
        onError("No Google accounts available on this device.")
    } catch (e: Exception) {
        Log.e("GoogleSignIn", "Google sign in error", e)
        onError(e.localizedMessage ?: "Google sign in failed")
    }
}
