package cz.tp.usousedu.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom

private const val GOOGLE_WEB_CLIENT_ID =
    "318571004015-kvmj0d126svjas13pu6mtfbriqlld04l.apps.googleusercontent.com"

@Composable
actual fun rememberGoogleIdentityProvider(): IGoogleIdentityProvider {
    val context = LocalContext.current
    return remember(context) { AndroidGoogleIdentityProvider(context.findActivity()) }
}

private class AndroidGoogleIdentityProvider(
    private val activity: Activity?,
) : IGoogleIdentityProvider {

    override suspend fun signIn(): Result<GoogleIdentity> {
        val hostActivity = activity ?: return Result.failure(AuthError.GoogleSignInFailed)
        val credentialManager = CredentialManager.create(hostActivity)
        val option = GetSignInWithGoogleOption.Builder(GOOGLE_WEB_CLIENT_ID)
            .setNonce(generateNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val credential = credentialManager.getCredential(hostActivity, request).credential
            if (
                credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Result.failure(AuthError.InvalidGoogleCredential)
            } else {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(
                    GoogleIdentity(
                        idToken = googleCredential.idToken,
                        subject = googleCredential.uniqueId,
                        email = googleCredential.email.orEmpty(),
                        displayName = googleCredential.displayName?.takeIf { it.isNotBlank() },
                    ),
                )
            }
        } catch (_: GetCredentialCancellationException) {
            Result.failure(AuthError.GoogleSignInCancelled)
        } catch (_: NoCredentialException) {
            Result.failure(AuthError.GoogleAccountUnavailable)
        } catch (_: GoogleIdTokenParsingException) {
            Result.failure(AuthError.InvalidGoogleCredential)
        } catch (_: GetCredentialException) {
            Result.failure(AuthError.GoogleSignInFailed)
        }
    }

    override suspend fun clearCredentialState() {
        val hostActivity = activity ?: return
        try {
            CredentialManager.create(hostActivity).clearCredentialState(
                ClearCredentialStateRequest(),
            )
        } catch (_: ClearCredentialException) {
            // The local session is already cleared; the next sign-in remains explicit.
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun generateNonce(): String {
    val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
