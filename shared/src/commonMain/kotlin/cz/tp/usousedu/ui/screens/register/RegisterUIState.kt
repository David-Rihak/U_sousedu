package cz.tp.usousedu.ui.screens.register

sealed interface RegisterUIState {
    data object Default : RegisterUIState
    data object Loading : RegisterUIState
    data class Error(val message: String) : RegisterUIState
    data object Success : RegisterUIState
}
