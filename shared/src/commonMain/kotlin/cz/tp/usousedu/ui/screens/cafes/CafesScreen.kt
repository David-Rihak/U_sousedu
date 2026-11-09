package cz.tp.usousedu.ui.screens.cafes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.data.seed.SeedData
import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.LoadingContent
import cz.tp.usousedu.ui.components.MainBottomNavigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CafesScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<CafesViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    // Same color scheme as Menu, so the bottom navigation looks the same on both tabs.
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CafesColors.accent,
            onPrimary = CafesColors.white,
            surface = CafesColors.white,
            secondaryContainer = CafesColors.white,
            onSecondaryContainer = CafesColors.accent,
            onSurface = CafesColors.text,
            surfaceContainer = CafesColors.white,
            surfaceContainerLow = CafesColors.white,
            onSurfaceVariant = CafesColors.mutedText,
        ),
    ) {
        BaseScreen(
            containerColor = CafesColors.background,
            bottomBar = {
                MainBottomNavigation(
                    selectedDestination = ScreenDestination.Cafes,
                    onDestinationSelected = navigation::toMainDestination,
                )
            },
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                CafesScreenContent(state = state, actions = viewModel)
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
fun CafesScreenContent(
    state: CafesUIState,
    actions: CafesActions,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(CafesColors.background),
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Kavárny",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CafesColors.text,
            )
            Text(
                text = "Vyberte si svou kavárnu, ukážeme ji na věrnostní kartě.",
                fontSize = 13.sp,
                color = CafesColors.mutedText,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
            )
        }

        when (state) {
            CafesUIState.Loading -> item { LoadingContent() }

            is CafesUIState.Error -> item {
                ErrorContent(message = state.message, onRetry = actions::retry)
            }

            is CafesUIState.Loaded -> if (state.cafes.isEmpty()) {
                item { EmptyCafesMessage() }
            } else {
                items(state.cafes, key = { it.id }) { cafe ->
                    CafeCard(
                        cafe = cafe,
                        isFavorite = cafe.id == state.favoriteCafeId,
                        isSaving = cafe.id == state.savingCafeId,
                        onSetFavorite = { actions.setFavorite(cafe.id) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CafesScreenPreview() {
    val cafes = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }
    MaterialTheme {
        CafesScreenContent(
            state = CafesUIState.Loaded(cafes = cafes, favoriteCafeId = cafes.first().id),
            actions = object : CafesActions {
                override fun retry() {}
                override fun setFavorite(cafeId: String) {}
                override fun messageShown() {}
            },
        )
    }
}
