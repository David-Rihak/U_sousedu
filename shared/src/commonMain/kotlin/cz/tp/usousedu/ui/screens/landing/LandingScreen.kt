package cz.tp.usousedu.ui.screens.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.data.auth.UserRole
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.ui.components.BaseScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LandingScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<LandingViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BaseScreen { paddingValues ->
        LandingScreenContent(
            paddingValues = paddingValues,
            state = state,
            navigation = navigation,
            actions = viewModel,
        )
    }
}

@Composable
fun LandingScreenContent(
    paddingValues: PaddingValues,
    state: LandingUIState,
    navigation: INavigationRouter,
    actions: LandingActions,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("you are in")

        if (state is LandingUIState.Loaded) {
            Text(state.user.email, style = MaterialTheme.typography.titleMedium)
            Text("Role", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.forEach { role ->
                    FilterChip(
                        selected = state.user.role == role,
                        onClick = { actions.setRole(role) },
                        label = { Text(role.displayName()) },
                    )
                }
            }
        }

        OutlinedButton(onClick = { actions.logout { navigation.toLoginScreen() } }) {
            Text("Log out")
        }
    }
}

private fun UserRole.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
