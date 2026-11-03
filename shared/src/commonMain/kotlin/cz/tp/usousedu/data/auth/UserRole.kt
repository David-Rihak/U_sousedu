package cz.tp.usousedu.data.auth

// `code` is what gets persisted (users.role). Never renumber existing entries.
enum class UserRole(val code: Int) {
    CUSTOMER(1),
    ADMIN(2),
    OWNER(3);

    companion object {
        fun fromCode(code: Int): UserRole = entries.firstOrNull { it.code == code } ?: CUSTOMER
    }
}
