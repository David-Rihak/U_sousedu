package cz.tp.usousedu.ui.screens

import cz.tp.usousedu.data.auth.AuthError

fun Throwable.toAuthMessage(): String = when (this) {
    AuthError.InvalidCredentials -> "Wrong email or password."
    AuthError.EmailTaken -> "This email is already registered."
    AuthError.WeakPassword -> "Password is too weak."
    AuthError.Network -> "No connection. Try again."
    AuthError.NotSignedIn -> "You are not signed in."
    AuthError.NotSupported -> "This sign-in method is not available yet."
    else -> "Something went wrong."
}
