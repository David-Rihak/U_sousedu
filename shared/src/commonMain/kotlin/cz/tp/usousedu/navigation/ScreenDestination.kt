package cz.tp.usousedu.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenDestination {
    @Serializable
    data object Login : ScreenDestination

    @Serializable
    data object Register : ScreenDestination

    @Serializable
    data object Landing : ScreenDestination
}
