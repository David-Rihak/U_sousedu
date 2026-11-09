package cz.tp.usousedu.ui.screens.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.CafePickerBottomSheet
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.MainBottomNavigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LandingScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<LandingViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var showQrCode by rememberSaveable { mutableStateOf(false) }
    var showCafePicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    BaseScreen(
        bottomBar = {
            MainBottomNavigation(
                selectedDestination = ScreenDestination.Landing,
                onDestinationSelected = navigation::toMainDestination,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LandingScreenContent(
                state = state,
                actions = viewModel,
                onShowQrCode = { showQrCode = true },
                onChangeCafe = { showCafePicker = true },
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    val loadedState = state as? LandingUIState.Loaded
    if (showQrCode && loadedState != null) {
        QrCodeBottomSheet(
            qrPayload = loadedState.qrPayload,
            userIdentifier = loadedState.user.id,
            onDismissRequest = { showQrCode = false },
        )
    }
    if (showCafePicker && loadedState != null) {
        CafePickerBottomSheet(
            cafes = loadedState.cafes,
            selectedCafeId = loadedState.selectedCafe?.id,
            onCafeSelected = { cafeId ->
                viewModel.changeCafe(cafeId)
                showCafePicker = false
            },
            onDismissRequest = { showCafePicker = false },
        )
    }
}

@Composable
fun LandingScreenContent(
    state: LandingUIState,
    actions: LoyaltyCardActions,
    onShowQrCode: () -> Unit,
    onChangeCafe: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            LandingUIState.Loading -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is LandingUIState.Error -> item {
                ErrorContent(message = state.message, onRetry = actions::retry)
            }

            is LandingUIState.Loaded -> {
                item {
                    LandingHeader(
                        selectedCafe = state.selectedCafe,
                        onChangeCafe = onChangeCafe,
                        canChangeCafe = state.cafes.size > 1,
                    )
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
