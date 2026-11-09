package cz.tp.usousedu.ui.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.data.model.CafeEvent
import cz.tp.usousedu.data.news.NewsCatalog
import cz.tp.usousedu.data.seed.SeedData
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.LoadingContent
import cz.tp.usousedu.ui.components.MainBottomNavigation
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@Composable
fun NewsScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<NewsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = NewsColors.accent,
            onPrimary = NewsColors.white,
            surface = NewsColors.white,
            secondaryContainer = NewsColors.white,
            onSecondaryContainer = NewsColors.accent,
            onSurface = NewsColors.text,
            surfaceContainer = NewsColors.white,
            surfaceContainerLow = NewsColors.white,
            onSurfaceVariant = NewsColors.mutedText,
        ),
    ) {
        BaseScreen(
            containerColor = NewsColors.background,
            bottomBar = {
                MainBottomNavigation(
                    selectedDestination = ScreenDestination.News,
                    onDestinationSelected = navigation::toMainDestination,
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                NewsScreenContent(
                    state = state,
                    actions = viewModel,
                    onReminderClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Připomínky zatím nejsou dostupné (demo)."
                            )
                        }
                    },
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

/** Can be previewed and tested independently of navigation, Koin and Firebase. */
@Composable
fun NewsScreenContent(
    state: NewsUIState,
    actions: NewsActions,
    onReminderClick: (CafeEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NewsColors.background),
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 21.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item {
            Text(
                text = "Aktuality v kavárnách",
                modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp),
                fontSize = 25.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NewsColors.text,
            )
        }
        when (state) {
            NewsUIState.Loading -> item { LoadingContent() }

            is NewsUIState.Error -> item {
                ErrorContent(message = state.message, onRetry = actions::retry)
            }

            is NewsUIState.Loaded -> {
                item {
                    NewsFilterRow(
                        selectedFilter = state.filter,
                        onFilterSelected = actions::selectFilter,
                    )
                }
                if (state.events.isEmpty()) {
                    item {
                        Text(
                            text = "V této kategorii zatím nejsou žádné nadcházející akce.",
                            color = NewsColors.mutedText,
                            modifier = Modifier.padding(vertical = 18.dp),
                        )
                    }
                } else {
                    items(state.events, key = { it.event.id }) { item ->
                        NewsEventCard(item = item, onReminderClick = onReminderClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun NewsScreenPreview() {
    val events = NewsViewModel.upcomingItems(
        events = NewsCatalog.events,
        cafes = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) },
        filter = NewsFilter.ALL,
        now = NewsCatalog.events.first().startsAt,
        timeZone = NewsCatalog.PRAGUE,
    )
    MaterialTheme {
        NewsScreenContent(
            state = NewsUIState.Loaded(NewsFilter.ALL, events),
            actions = object : NewsActions {
                override fun selectFilter(filter: NewsFilter) {}
                override fun retry() {}
            },
            onReminderClick = {},
        )
    }
}
