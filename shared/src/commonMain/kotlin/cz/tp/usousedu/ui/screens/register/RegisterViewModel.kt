package cz.tp.usousedu.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.auth.MIN_PASSWORD_LENGTH
import cz.tp.usousedu.data.auth.isValidEmail
import cz.tp.usousedu.ui.screens.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: IAuthRepository,
) : ViewModel(), RegisterActions {

    private val _uiState: MutableStateFlow<RegisterUIState> = MutableStateFlow(RegisterUIState.Default)
    val uiState: StateFlow<RegisterUIState> = _uiState.asStateFlow()

    override fun register(email: String, password: String, passwordConfirmation: String) {
        val validationError = when {
            !isValidEmail(email.trim()) -> "Enter a valid email."
            password.length < MIN_PASSWORD_LENGTH -> "Password must have at least $MIN_PASSWORD_LENGTH characters."
            password != passwordConfirmation -> "Passwords do not match."
            else -> null
        }
        if (validationError != null) {
            _uiState.value = RegisterUIState.Error(validationError)
            return
        }
        viewModelScope.launch {
            _uiState.value = RegisterUIState.Loading
            _uiState.value = authRepository.register(email, password).fold(
                onSuccess = { RegisterUIState.Success },
                onFailure = { RegisterUIState.Error(it.toAuthMessage()) },
            )
        }
    }
}
