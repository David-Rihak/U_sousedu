package cz.tp.usousedu.data.auth

// Failures every IAuthRepository implementation maps its own exceptions to.
sealed class AuthError(message: String) : Exception(message) {
    object InvalidCredentials : AuthError("Invalid credentials")
    object EmailTaken : AuthError("Email already registered")
    object WeakPassword : AuthError("Password is too weak")
    object Network : AuthError("Network error")
    object NotSignedIn : AuthError("Not signed in")
    object NotSupported : AuthError("Sign-in method not supported")
}
