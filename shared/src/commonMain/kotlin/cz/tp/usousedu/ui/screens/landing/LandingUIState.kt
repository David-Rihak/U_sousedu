package cz.tp.usousedu.ui.screens.landing

import cz.tp.usousedu.data.auth.AuthUser

sealed interface LandingUIState {
    data object Loading : LandingUIState
    data class Loaded(val user: AuthUser) : LandingUIState
}
