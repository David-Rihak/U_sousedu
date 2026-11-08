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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.MainBottomNavigation
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(navigation: INavigationRouter) {
    var filterKey by rememberSaveable { mutableStateOf(NewsFilter.ALL.name) }
    val selectedFilter = NewsFilter.entries.firstOrNull { it.name == filterKey } ?: NewsFilter.ALL
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
                    selectedFilter = selectedFilter,
                    onFilterSelected = { filterKey = it.name },
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
    selectedFilter: NewsFilter,
    onFilterSelected: (NewsFilter) -> Unit,
    onReminderClick: (CafeEvent) -> Unit,
) {
    val events = NewsCatalog.forFilter(selectedFilter)

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
        item {
            NewsFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
            )
        }
        if (events.isEmpty()) {
            item {
                Text(
                    text = "V této kategorii zatím nejsou žádné aktuality.",
                    color = NewsColors.mutedText,
                    modifier = Modifier.padding(vertical = 18.dp),
                )
            }
        } else {
            items(events, key = { it.id }) { event ->
                NewsEventCard(event = event, onReminderClick = onReminderClick)
            }
        }
    }
}

@Preview
@Composable
fun NewsScreenPreview() {
    MaterialTheme {
        NewsScreenContent(
            selectedFilter = NewsFilter.ALL,
            onFilterSelected = {},
            onReminderClick = {},
        )
    }
}
