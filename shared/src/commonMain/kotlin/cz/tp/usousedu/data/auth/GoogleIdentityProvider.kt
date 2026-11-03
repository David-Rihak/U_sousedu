package cz.tp.usousedu.data.auth

import androidx.compose.runtime.Composable

data class GoogleIdentity(
    val idToken: String,
    val subject: String,
    val email: String,
    val displayName: String?,
)

interface IGoogleIdentityProvider {
    suspend fun signIn(): Result<GoogleIdentity>
    suspend fun clearCredentialState()
}

@Composable
expect fun rememberGoogleIdentityProvider(): IGoogleIdentityProvider
