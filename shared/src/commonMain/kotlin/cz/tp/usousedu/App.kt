package cz.tp.usousedu

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import cz.tp.usousedu.navigation.NavGraph
import cz.tp.usousedu.navigation.INavigationRouter
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val navigationRouter = koinInject<INavigationRouter>()

        LaunchedEffect(navController) {
            navigationRouter.setNavController(navController)
        }

        NavGraph(navController = navController)
    }
}
