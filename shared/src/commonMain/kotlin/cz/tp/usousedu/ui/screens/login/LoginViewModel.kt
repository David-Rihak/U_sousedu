package cz.tp.usousedu.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.IAuthRepository
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

    override fun loginWithGoogle() {
        // TODO: obtain the Google ID token with the platform SDK (Credential Manager on Android,
        //  GoogleSignIn on iOS) and pass it to authRepository.signInWithGoogle(idToken).
        _uiState.value = LoginUIState.Error("Google login is not implemented yet.")
    }
}
