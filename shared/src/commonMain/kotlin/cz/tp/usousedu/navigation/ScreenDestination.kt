package cz.tp.usousedu.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenDestination {
    @Serializable
    data object Login : ScreenDestination

    //This can be deleted after login implementation
    @Serializable
    data object AfterLoginTest : ScreenDestination

}
