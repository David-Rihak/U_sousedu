package cz.tp.usousedu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.tp.usousedu.ui.screens.landing.LandingScreen
import cz.tp.usousedu.ui.screens.login.LoginScreen
import cz.tp.usousedu.ui.screens.register.RegisterScreen

@Composable
fun NavGraph(
    startDestination: ScreenDestination,
    navController: NavHostController = rememberNavController(),
    navigation: INavigationRouter = remember(navController) {
        NavigationRouterImpl(navController)
    },
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<ScreenDestination.Login> {
            LoginScreen(navigation)
        }

        composable<ScreenDestination.Register> {
            RegisterScreen(navigation)
        }

        composable<ScreenDestination.Landing> {
            LandingScreen(navigation)
        }
    }
}
