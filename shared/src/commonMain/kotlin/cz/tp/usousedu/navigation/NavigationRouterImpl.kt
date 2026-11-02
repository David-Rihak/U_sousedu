package cz.tp.usousedu.navigation

import androidx.navigation.NavController
import androidx.navigation.NavHostController

class NavigationRouterImpl(
    private val navController: NavController
) : INavigationRouter {

    override fun toLoginScreen() {
        navController.navigate(ScreenDestination.Login)
    }

    override fun toAfterLoginScreen() {
        navController.navigate(ScreenDestination.AfterLoginTest)
    }


    override fun navigateBack() {
        navController.popBackStack()
    }
}
