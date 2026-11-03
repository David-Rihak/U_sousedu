package cz.tp.usousedu.ui.screens.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.AuthProvider
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.auth.IGoogleIdentityProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface LandingActions {
    fun logout(identityProvider: IGoogleIdentityProvider, onLoggedOut: () -> Unit)
}

class LandingViewModel(
    private val authRepository: IAuthRepository,
) : ViewModel(), LandingActions {

    val uiState: StateFlow<LandingUIState> = authRepository.currentUser
        .map { user ->
            if (user != null) LandingMockData.forUser(user) else LandingUIState.Loading
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LandingUIState.Loading)

    override fun logout(identityProvider: IGoogleIdentityProvider, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            val provider = authRepository.currentUser.first()?.provider
            authRepository.signOut()
            if (provider == AuthProvider.GOOGLE) {
                identityProvider.clearCredentialState()
            }
            onLoggedOut()
        }
    }

}
