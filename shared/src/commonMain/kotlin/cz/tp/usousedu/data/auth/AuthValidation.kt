package cz.tp.usousedu.data.auth

// Same rules Firebase Auth enforces, so behaviour doesn't change after the switch.
const val MIN_PASSWORD_LENGTH = 6

fun isValidEmail(email: String): Boolean {
    val at = email.indexOf('@')
    return at > 0 &&
        email.indexOf('.', at) > at + 1 &&
        !email.endsWith('.') &&
        email.none { it.isWhitespace() }
}
