package cz.tp.usousedu.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import cz.tp.usousedu.data.profile.IUserProfileRepository
import cz.tp.usousedu.database.UserDao
import cz.tp.usousedu.database.UserEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Mock auth backed by Room (accounts) and DataStore (the signed-in user id).
 * Replace with a Firebase implementation of [IAuthRepository] in the DI module.
 */
class LocalAuthRepository(
    private val dao: UserDao,
    private val sessionStore: DataStore<Preferences>,
) : IAuthRepository, IUserProfileRepository {

    // Follows the user's row, so profile changes (e.g. the role) are emitted too.
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<AuthUser?> = sessionStore.data
        .map { it[USER_ID] }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeById(id).map { it?.toAuthUser() }
        }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        val user = dao.findByCredentials(email.trim(), PasswordHasher.hash(password))
            ?: return Result.failure(AuthError.InvalidCredentials)
        saveSession(user.id)
        return Result.success(user.toAuthUser())
    }

    override suspend fun register(email: String, password: String): Result<AuthUser> {
        val entity = UserEntity(login = email.trim(), passwordHash = PasswordHasher.hash(password))
        // The only expected failure is the unique index on users.login.
        val id = try {
            dao.insert(entity)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(AuthError.EmailTaken)
        }
        saveSession(id)
        return Result.success(entity.copy(id = id).toAuthUser())
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        Result.failure(AuthError.NotSupported)

    override suspend fun signOut() {
        sessionStore.edit { it.remove(USER_ID) }
    }

    override suspend fun setRole(role: UserRole): Result<AuthUser> {
        val id = sessionStore.data.first()[USER_ID] ?: return Result.failure(AuthError.NotSignedIn)
        dao.updateRole(id, role.code)
        return dao.findById(id)?.let { Result.success(it.toAuthUser()) }
            ?: Result.failure(AuthError.NotSignedIn)
    }

    // The Room table has no column for it; the local mock is kept only as a fallback.
    override suspend fun setFavoriteCafe(cafeId: String): Result<AuthUser> =
        Result.failure(AuthError.NotSupported)

    /** Local-only: creates the mock account when the table is empty, without signing it in. */
    suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            dao.insert(UserEntity(login = MOCK_EMAIL, passwordHash = PasswordHasher.hash(MOCK_PASSWORD)))
        }
    }

    private suspend fun saveSession(userId: Long) {
        sessionStore.edit { it[USER_ID] = userId }
    }

    private fun UserEntity.toAuthUser() =
        AuthUser(id = id.toString(), email = login, role = UserRole.fromCode(role))

    private companion object {
        val USER_ID = longPreferencesKey("user_id")
        const val MOCK_EMAIL = "test@test.com"
        const val MOCK_PASSWORD = "test1234"
    }
}
