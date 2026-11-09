package cz.tp.usousedu.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.data.menu.MenuCatalog
import cz.tp.usousedu.data.model.MenuCategory
import cz.tp.usousedu.data.seed.SeedData
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.LoadingContent
import cz.tp.usousedu.ui.components.MainBottomNavigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MenuScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<MenuViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = MenuColors.activeNavigation,
            onPrimary = MenuColors.white,
            surface = MenuColors.white,
            secondaryContainer = MenuColors.white,
            onSecondaryContainer = MenuColors.activeNavigation,
            onSurface = MenuColors.text,
            surfaceContainer = MenuColors.white,
            surfaceContainerLow = MenuColors.white,
            onSurfaceVariant = MenuColors.mutedText,
        ),
    ) {
        BaseScreen(
            containerColor = MenuColors.background,
            bottomBar = {
                MainBottomNavigation(
                    selectedDestination = ScreenDestination.Menu,
                    onDestinationSelected = navigation::toMainDestination,
                )
            },
        ) { paddingValues ->
            MenuScreenContent(
                paddingValues = paddingValues,
                state = state,
                actions = viewModel,
            )
        }
    }
}

@Composable
fun MenuScreenContent(
    paddingValues: PaddingValues,
    state: MenuUIState,
    actions: MenuActions,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuColors.background)
            .padding(paddingValues),
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        item {
            Text(
                text = "Menu",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MenuColors.text,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        when (state) {
            MenuUIState.Loading -> item { LoadingContent() }

            is MenuUIState.Error -> item {
                ErrorContent(message = state.message, onRetry = actions::retry)
            }

            is MenuUIState.Loaded -> {
                item {
                    CafeSelector(
                        cafes = state.cafes,
                        selectedCafeId = state.selectedCafeId,
                        onCafeSelected = actions::selectCafe,
                        modifier = Modifier.padding(bottom = 5.dp),
                    )
                }
                item {
                    CategorySelector(
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = actions::selectCategory,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    )
                }
                if (state.products.isEmpty()) {
                    item { EmptyMenuMessage() }
                } else {
                    items(state.products, key = { it.id }) { product ->
                        MenuProductCard(product = product)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MenuScreenPreview() {
    val cafes = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }.sortedBy { it.order }
    val cafeId = cafes.first().id
    MaterialTheme {
        MenuScreenContent(
            paddingValues = PaddingValues(0.dp),
            state = MenuUIState.Loaded(
                cafes = cafes,
                selectedCafeId = cafeId,
                selectedCategory = MenuCategory.COFFEE,
                products = MenuCatalog.forSelection(cafeId, MenuCategory.COFFEE),
            ),
            actions = object : MenuActions {
                override fun selectCafe(cafeId: String) {}
                override fun selectCategory(category: MenuCategory) {}
                override fun retry() {}
            },
        )
    }
}
