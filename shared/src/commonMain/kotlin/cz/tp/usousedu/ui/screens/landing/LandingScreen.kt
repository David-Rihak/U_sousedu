package cz.tp.usousedu.ui.screens.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.MainBottomNavigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LandingScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<LandingViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showQrCode by rememberSaveable { mutableStateOf(false) }

    BaseScreen(
        bottomBar = {
            MainBottomNavigation(
                selectedDestination = ScreenDestination.Landing,
                onDestinationSelected = navigation::toMainDestination,
            )
        },
    ) { paddingValues ->
        LandingScreenContent(
            paddingValues = paddingValues,
            state = state,
            onShowQrCode = { showQrCode = true },
        )
    }

    val loadedState = state as? LandingUIState.Loaded
    if (showQrCode && loadedState != null) {
        QrCodeBottomSheet(
            qrPayload = loadedState.qrPayload,
            userIdentifier = loadedState.user.id,
            onDismissRequest = { showQrCode = false },
        )
    }
}

@Composable
fun LandingScreenContent(
    paddingValues: PaddingValues,
    state: LandingUIState,
    onShowQrCode: () -> Unit,
) {
    when (state) {
        LandingUIState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        }

        is LandingUIState.Loaded -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    LandingHeader(selectedCafe = state.selectedCafe)
                }
                item {
                    LoyaltyCard(
                        earnedStamps = state.earnedStamps,
                        maximumStamps = state.maximumStamps,
                    )
                }
                item {
                    ShowQrCodeButton(onClick = onShowQrCode)
                }
                item {
                    Text(
                        text = "Odměny",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }
                if (state.rewards.isEmpty()) {
                    item {
                        Text(
                            text = "Zatím nemáte žádné dostupné odměny.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.rewards, key = { it.id }) { reward ->
                        RewardCard(reward = reward)
                    }
                }
            }
        }
    }
}
