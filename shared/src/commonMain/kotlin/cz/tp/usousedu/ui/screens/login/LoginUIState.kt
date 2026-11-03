package cz.tp.usousedu.ui.screens.login

sealed interface LoginUIState {
    data object Default : LoginUIState
    data object Loading : LoginUIState
    data class Error(val message: String) : LoginUIState
    data object Success : LoginUIState
}
