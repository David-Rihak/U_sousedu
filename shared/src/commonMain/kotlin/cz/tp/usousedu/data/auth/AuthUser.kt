package cz.tp.usousedu.data.auth

data class AuthUser(
    val id: String,
    val email: String,
    val role: UserRole = UserRole.CUSTOMER,
    /** Café shown on the loyalty card; null = none chosen yet (use the first café). */
    val favoriteCafeId: String? = null,
)
