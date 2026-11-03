package cz.tp.usousedu.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface IosGoogleSignInCompletion {
    fun onSuccess(
        idToken: String,
        subject: String,
        email: String,
        displayName: String?,
    )

    fun onCancelled()
    fun onFailure()
}

object IosGoogleSignInBridge {
    private var signInHandler: ((IosGoogleSignInCompletion) -> Unit)? = null
    private var signOutHandler: (() -> Unit)? = null

    fun configure(
        signIn: (IosGoogleSignInCompletion) -> Unit,
        signOut: () -> Unit,
    ) {
        signInHandler = signIn
        signOutHandler = signOut
    }

    internal fun signIn(completion: IosGoogleSignInCompletion): Boolean {
        val handler = signInHandler ?: return false
        handler(completion)
        return true
    }

    internal fun signOut() {
        signOutHandler?.invoke()
    }
}

@Composable
actual fun rememberGoogleIdentityProvider(): IGoogleIdentityProvider = remember {
    IosGoogleIdentityProvider()
}

private class IosGoogleIdentityProvider : IGoogleIdentityProvider {
    override suspend fun signIn(): Result<GoogleIdentity> = suspendCoroutine { continuation ->
        val started = IosGoogleSignInBridge.signIn(
            object : IosGoogleSignInCompletion {
                override fun onSuccess(
                    idToken: String,
                    subject: String,
                    email: String,
                    displayName: String?,
                ) {
                    continuation.resume(
                        Result.success(
                            GoogleIdentity(
                                idToken = idToken,
                                subject = subject,
                                email = email,
                                displayName = displayName?.takeIf { it.isNotBlank() },
                            ),
                        ),
                    )
                }

                override fun onCancelled() {
                    continuation.resume(Result.failure(AuthError.GoogleSignInCancelled))
                }

                override fun onFailure() {
                    continuation.resume(Result.failure(AuthError.GoogleSignInFailed))
                }
            },
        )
        if (!started) {
            continuation.resume(Result.failure(AuthError.NotSupported))
        }
    }

    override suspend fun clearCredentialState() {
        IosGoogleSignInBridge.signOut()
    }
}
