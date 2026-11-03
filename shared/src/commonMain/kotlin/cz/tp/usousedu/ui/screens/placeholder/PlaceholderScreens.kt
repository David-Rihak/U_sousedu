package cz.tp.usousedu.ui.screens.placeholder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.MainBottomNavigation
import cz.tp.usousedu.ui.screens.landing.LandingUIState
import cz.tp.usousedu.ui.screens.landing.LandingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainPlaceholderScreen(
    destination: ScreenDestination,
    title: String,
    navigation: INavigationRouter,
) {
    BaseScreen(
        bottomBar = {
            MainBottomNavigation(
                selectedDestination = destination,
                onDestinationSelected = navigation::toMainDestination,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Tato část aplikace bude doplněna později.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ProfilePlaceholderScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<LandingViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BaseScreen(
        bottomBar = {
            MainBottomNavigation(
                selectedDestination = ScreenDestination.Profile,
                onDestinationSelected = navigation::toMainDestination,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Profil", style = MaterialTheme.typography.headlineMedium)
            when (val currentState = state) {
                LandingUIState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                )

                is LandingUIState.Loaded -> {
                    Text(
                        text = currentState.user.email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    )
                    OutlinedButton(
                        onClick = { viewModel.logout(navigation::toLoginScreen) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Odhlásit se")
                    }
                }
            }
        }
    }
}
