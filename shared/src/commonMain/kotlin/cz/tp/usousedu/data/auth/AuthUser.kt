package cz.tp.usousedu.data.auth

data class AuthUser(
    val id: String,
    val email: String,
    val role: UserRole = UserRole.CUSTOMER,
)
