package cz.tp.usousedu.ui.screens.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.CafePickerBottomSheet
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.LoadingContent
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

    LandingTheme {
        BaseScreen(
            containerColor = LandingColors.background,
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

        // Both sheets are inside LandingTheme, so they take the warm colors too.
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
}

// Same palette as Menu, Aktuality, Kavárny and Profil.
internal object LandingColors {
    val background = Color(0xFFFFF4E6)
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val accent = Color(0xFFE2572B)
    val white = Color.White
    val stampBorder = Color(0xFFE9D9C8)
    val track = Color(0xFFF6EADB)
    val badgeBackground = Color(0xFFDDEFE3)
    val badgeText = Color(0xFF177453)
}

/** Warm color scheme; the components below read it through MaterialTheme.colorScheme. */
@Composable
fun LandingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = LandingColors.accent,
            onPrimary = LandingColors.white,
            background = LandingColors.background,
            surface = LandingColors.white,
            onSurface = LandingColors.text,
            // White secondaryContainer = no tinted indicator in the bottom navigation (as in Menu).
            secondaryContainer = LandingColors.white,
            onSecondaryContainer = LandingColors.accent,
            surfaceContainer = LandingColors.white,
            surfaceContainerLow = LandingColors.white,
            surfaceContainerHigh = LandingColors.white,
            surfaceVariant = LandingColors.track,
            onSurfaceVariant = LandingColors.mutedText,
            outline = LandingColors.stampBorder,
            outlineVariant = LandingColors.stampBorder,
        ),
        content = content,
    )
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
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state) {
            LandingUIState.Loading -> item { LoadingContent() }

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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LandingColors.text,
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
