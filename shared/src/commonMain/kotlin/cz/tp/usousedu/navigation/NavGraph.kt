package cz.tp.usousedu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController

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

        }

        composable<ScreenDestination.AfterLoginTest> {

        }
    }
}
