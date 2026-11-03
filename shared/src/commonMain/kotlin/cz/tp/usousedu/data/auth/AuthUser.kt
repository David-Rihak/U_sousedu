package cz.tp.usousedu.data.auth

data class AuthUser(
    val id: String,
    val email: String,
    val role: UserRole = UserRole.CUSTOMER,
    val provider: AuthProvider = AuthProvider.LOCAL,
    val displayName: String? = null,
)

enum class AuthProvider {
    LOCAL,
    GOOGLE,
}
