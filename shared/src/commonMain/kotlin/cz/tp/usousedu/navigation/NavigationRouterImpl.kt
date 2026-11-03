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
            launchSingleTop = true
        }
    }

    override fun toMainDestination(destination: ScreenDestination) {
        navController.navigate(destination) {
            popUpTo(ScreenDestination.Landing) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}
