package cz.tp.usousedu.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenDestination {
    @Serializable
    data object Login : ScreenDestination

    @Serializable
    data object Register : ScreenDestination

    @Serializable
    data object Landing : ScreenDestination

    @Serializable
    data object Menu : ScreenDestination

    @Serializable
    data object News : ScreenDestination

    @Serializable
    data object Cafes : ScreenDestination

    @Serializable
    data object Profile : ScreenDestination
}
