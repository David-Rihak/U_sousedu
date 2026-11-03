package cz.tp.usousedu.data.auth

import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    /** The signed-in user, or null. Emits on every sign in / sign out. */
    val currentUser: Flow<AuthUser?>

    /** Failure carries an [AuthError]. */
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
}
