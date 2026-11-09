package cz.tp.usousedu.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.AuthUser
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.auth.UserRole
import cz.tp.usousedu.data.auth.isStaff
import cz.tp.usousedu.data.cafes.ICafeRepository
import cz.tp.usousedu.data.cafes.resolveCafeId
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.profile.IUserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProfileUIState {
    data object Loading : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    data class Loaded(
        val email: String,
        /** Shown only for staff roles; null for a regular customer. */
        val roleLabel: String?,
        /** Café shown on the card (favorite, else the first one); null when there are no cafés. */
        val cafe: Cafe?,
        /** All cafés, for the "Změnit" picker. */
        val cafes: List<Cafe> = emptyList(),
        /** ADMIN / OWNER: shows the "Režim obsluhy" button. */
        val isStaff: Boolean = false,
    ) : ProfileUIState
}

interface ProfileActions {
    fun logout(onLoggedOut: () -> Unit)
    fun retry()
    fun changeCafe(cafeId: String)
    fun messageShown()
}

class ProfileViewModel(
    private val authRepository: IAuthRepository,
    private val cafeRepository: ICafeRepository,
    private val profileRepository: IUserProfileRepository,
) : ViewModel(), ProfileActions {

    private val retryTrigger = MutableStateFlow(0)

    private val _message = MutableStateFlow<String?>(null)
    /** One-off message for a snackbar (e.g. a failed café change). */
    val message: StateFlow<String?> = _message.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProfileUIState> = retryTrigger
        .flatMapLatest {
            combine(
                authRepository.currentUser.filterNotNull(),
                cafeRepository.observeCafes(),
            ) { user, cafes -> buildState(user, cafes) as ProfileUIState }
                .onStart { emit(ProfileUIState.Loading) }
                .catch { emit(ProfileUIState.Error("Profil se nepodařilo načíst.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUIState.Loading)

    override fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onLoggedOut()
        }
    }

    override fun retry() {
        retryTrigger.value++
    }

    // Same write as on Karta and in Kavárny, so all screens stay in sync.
    override fun changeCafe(cafeId: String) {
        viewModelScope.launch {
            profileRepository.setFavoriteCafe(cafeId).onFailure {
                _message.value = "Kavárnu se nepodařilo změnit. Zkuste to znovu."
            }
        }
    }

    override fun messageShown() {
        _message.value = null
    }

    companion object {
        fun buildState(user: AuthUser, cafes: List<Cafe>): ProfileUIState.Loaded {
            val cafeId = resolveCafeId(cafes, pickedId = null, favoriteId = user.favoriteCafeId)
            return ProfileUIState.Loaded(
                email = user.email,
                roleLabel = staffRoleLabel(user.role),
                isStaff = user.role.isStaff,
                cafe = cafes.firstOrNull { it.id == cafeId },
                cafes = cafes,
            )
        }

        /** Czech label for staff roles; customers get no label (it would only be noise). */
        fun staffRoleLabel(role: UserRole): String? = when (role) {
            UserRole.CUSTOMER -> null
            UserRole.ADMIN -> "Administrátor"
            UserRole.OWNER -> "Majitel"
        }
    }
}
