package cz.tp.usousedu

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.navigation.NavGraph
import cz.tp.usousedu.navigation.ScreenDestination
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val authRepository = koinInject<IAuthRepository>()

        var startDestination by remember { mutableStateOf<ScreenDestination?>(null) }

        LaunchedEffect(Unit) {
            startDestination = if (authRepository.currentUser.first() != null) {
                ScreenDestination.Landing
            } else {
                ScreenDestination.Login
            }
        }

        startDestination?.let { start ->
            NavGraph(
                startDestination = start,
                navController = rememberNavController(),
            )
        }
    }
}
