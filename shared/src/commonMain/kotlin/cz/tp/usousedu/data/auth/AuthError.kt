package cz.tp.usousedu.data.auth

// Failures every IAuthRepository implementation maps its own exceptions to.
sealed class AuthError(message: String) : Exception(message) {
    object InvalidCredentials : AuthError("Invalid credentials")
    object EmailTaken : AuthError("Email already registered")
    object WeakPassword : AuthError("Password is too weak")
    object Network : AuthError("Network error")
    object NotSignedIn : AuthError("Not signed in")
    object NotSupported : AuthError("Sign-in method not supported")
    object GoogleSignInCancelled : AuthError("Google sign-in cancelled")
    object GoogleAccountUnavailable : AuthError("No Google account available")
    object InvalidGoogleCredential : AuthError("Invalid Google credential")
    object GoogleSignInFailed : AuthError("Google sign-in failed")
}
