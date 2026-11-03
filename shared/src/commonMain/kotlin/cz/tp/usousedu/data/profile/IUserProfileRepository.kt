package cz.tp.usousedu.data.profile

import cz.tp.usousedu.data.auth.AuthUser
import cz.tp.usousedu.data.auth.UserRole

// Profile data of the signed-in user, kept apart from IAuthRepository because with Firebase
// it would live in a database (Firestore), not in Firebase Auth.
interface IUserProfileRepository {
    suspend fun setRole(role: UserRole): Result<AuthUser>
}
