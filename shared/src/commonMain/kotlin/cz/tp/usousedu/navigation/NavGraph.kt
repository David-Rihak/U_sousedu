package cz.tp.usousedu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.tp.usousedu.ui.screens.cafes.CafesScreen
import cz.tp.usousedu.ui.screens.landing.LandingScreen
import cz.tp.usousedu.ui.screens.login.LoginScreen
import cz.tp.usousedu.ui.screens.menu.MenuScreen
import cz.tp.usousedu.ui.screens.news.NewsScreen
import cz.tp.usousedu.ui.screens.profile.ProfileScreen
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

        composable<ScreenDestination.Menu> {
            MenuScreen(navigation)
        }

        composable<ScreenDestination.News> {
            NewsScreen(navigation)
        }

        composable<ScreenDestination.Cafes> {
            CafesScreen(navigation)
        }

        composable<ScreenDestination.Profile> {
            ProfileScreen(navigation)
        }
    }
}
