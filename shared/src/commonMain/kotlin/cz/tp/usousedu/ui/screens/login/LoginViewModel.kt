package cz.tp.usousedu.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.AuthError
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.auth.IGoogleIdentityProvider
import cz.tp.usousedu.ui.screens.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: IAuthRepository,
) : ViewModel(), LoginActions {

    private val _uiState: MutableStateFlow<LoginUIState> = MutableStateFlow(LoginUIState.Default)
    val uiState: StateFlow<LoginUIState> = _uiState.asStateFlow()

    override fun login(email: String, password: String) {
        if (_uiState.value is LoginUIState.Loading) return
        if (email.isBlank() || password.isEmpty()) {
            _uiState.value = LoginUIState.Error("Fill in email and password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUIState.Loading
            _uiState.value = authRepository.signIn(email, password).fold(
                onSuccess = { LoginUIState.Success },
                onFailure = { LoginUIState.Error(it.toAuthMessage()) },
            )
        }
    }

    override fun loginWithGoogle(identityProvider: IGoogleIdentityProvider) {
        if (_uiState.value is LoginUIState.Loading) return
        viewModelScope.launch {
            _uiState.value = LoginUIState.Loading
            val identity = identityProvider.signIn().getOrElse { error ->
                _uiState.value = if (error === AuthError.GoogleSignInCancelled) {
                    LoginUIState.Default
                } else {
                    LoginUIState.Error(error.toAuthMessage())
                }
                return@launch
            }
            _uiState.value = authRepository.signInWithGoogle(identity).fold(
                onSuccess = { LoginUIState.Success },
                onFailure = { LoginUIState.Error(it.toAuthMessage()) },
            )
        }
    }
}
