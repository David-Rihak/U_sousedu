package cz.tp.usousedu.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGoogleIdentityProvider(): IGoogleIdentityProvider = remember {
    object : IGoogleIdentityProvider {
        override suspend fun signIn(): Result<GoogleIdentity> =
            Result.failure(AuthError.NotSupported)

        override suspend fun clearCredentialState() = Unit
    }
}
