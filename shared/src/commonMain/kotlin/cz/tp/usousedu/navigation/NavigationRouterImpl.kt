package cz.tp.usousedu.navigation

import androidx.navigation.NavController

class NavigationRouterImpl(
    private val navController: NavController
) : INavigationRouter {

    override fun toLoginScreen() {
        navController.navigate(ScreenDestination.Login) {
            popUpTo(ScreenDestination.Landing) { inclusive = true }
        }
    }

    override fun toRegisterScreen() {
        navController.navigate(ScreenDestination.Register)
    }

    override fun toLandingScreen() {
        navController.navigate(ScreenDestination.Landing) {
            popUpTo(ScreenDestination.Login) { inclusive = true }
        }
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}
