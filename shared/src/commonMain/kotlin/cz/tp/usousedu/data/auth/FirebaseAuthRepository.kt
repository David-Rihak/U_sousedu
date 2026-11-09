package cz.tp.usousedu.data.auth

import cz.tp.usousedu.data.profile.IUserProfileRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

/**
 * Firebase Auth for accounts, Firestore document `users/{uid}` for profile data
 * (role, favorite café).
 * Firebase Auth keeps the session on its own, so no DataStore is needed here.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : IAuthRepository, IUserProfileRepository {

    // Follows users/{uid}, so a role change in Firestore is emitted right away.
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<AuthUser?> = auth.authStateChanged
        .map { user -> user?.let { SignedIn(it.uid, it.email.orEmpty()) } }
        .distinctUntilChanged()
        .flatMapLatest { signedIn ->
            if (signedIn == null) {
                flowOf(null)
            } else {
                userDoc(signedIn.uid).snapshots
                    .map { signedIn.toAuthUser(it.profile()) }
                    // Unreadable profile (e.g. denied by rules) must not sign the user out.
                    .catch { emit(signedIn.toAuthUser(Profile())) }
            }
        }
        .distinctUntilChanged()

    override suspend fun signIn(email: String, password: String): Result<AuthUser> = runAuth {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).user
            ?: throw AuthError.InvalidCredentials
        SignedIn(user.uid, user.email.orEmpty()).toAuthUser(readProfile(user.uid))
    }

    override suspend fun register(email: String, password: String): Result<AuthUser> = runAuth {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).user
            ?: throw AuthError.InvalidCredentials
        val profile = UserDocument(email = user.email.orEmpty(), role = UserRole.CUSTOMER.name)
        userDoc(user.uid).set(profile)
        AuthUser(id = user.uid, email = profile.email, role = UserRole.CUSTOMER)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        Result.failure(AuthError.NotSupported)

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun setRole(role: UserRole): Result<AuthUser> =
        updateProfileField(FIELD_ROLE, role.name)

    override suspend fun setFavoriteCafe(cafeId: String): Result<AuthUser> =
        updateProfileField(FIELD_FAVORITE_CAFE, cafeId)

    /** Merge-writes one field, so it works even when users/{uid} does not exist yet. */
    private suspend fun updateProfileField(field: String, value: String): Result<AuthUser> = runAuth {
        val user = auth.currentUser ?: throw AuthError.NotSignedIn
        userDoc(user.uid).set(mapOf(field to value), merge = true)
        SignedIn(user.uid, user.email.orEmpty()).toAuthUser(readProfile(user.uid))
    }

    private fun userDoc(uid: String) = firestore.collection(USERS).document(uid)

    private suspend fun readProfile(uid: String): Profile = try {
        userDoc(uid).get().profile()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Profile()
    }

    private fun DocumentSnapshot.profile(): Profile {
        if (!exists) return Profile()
        val roleName = get<String?>(FIELD_ROLE)
        return Profile(
            role = UserRole.entries.firstOrNull { it.name == roleName } ?: UserRole.CUSTOMER,
            favoriteCafeId = get<String?>(FIELD_FAVORITE_CAFE)?.takeIf { it.isNotBlank() },
        )
    }

    /** Runs [block] and maps Firebase exceptions to [AuthError]. */
    private inline fun runAuth(block: () -> AuthUser): Result<AuthUser> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toAuthError())
    }

    private fun Exception.toAuthError(): AuthError = when (this) {
        is AuthError -> this
        // Must stay before InvalidCredentials: on Android it is a subclass of it.
        is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword
        is FirebaseAuthUserCollisionException -> AuthError.EmailTaken
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> AuthError.InvalidCredentials
        // FirebaseNetworkException, FirebaseTooManyRequestsException and anything unexpected:
        // no dedicated AuthError yet, "try again" fits all of them.
        else -> AuthError.Network
    }

    private data class SignedIn(val uid: String, val email: String) {
        fun toAuthUser(profile: Profile) = AuthUser(
            id = uid,
            email = email,
            role = profile.role,
            favoriteCafeId = profile.favoriteCafeId,
        )
    }

    /** Fields of users/{uid} that end up in [AuthUser]. */
    private data class Profile(
        val role: UserRole = UserRole.CUSTOMER,
        val favoriteCafeId: String? = null,
    )

    /** Shape of the Firestore document users/{uid}. */
    @Serializable
    private data class UserDocument(
        val email: String,
        val role: String,
        val createdAt: BaseTimestamp = Timestamp.ServerTimestamp,
    )

    private companion object {
        const val USERS = "users"
        const val FIELD_ROLE = "role"
        const val FIELD_FAVORITE_CAFE = "favoriteCafeId"
    }
}
