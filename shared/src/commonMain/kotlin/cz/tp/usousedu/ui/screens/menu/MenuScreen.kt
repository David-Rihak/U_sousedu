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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.MainBottomNavigation

@Composable
fun MenuScreen(navigation: INavigationRouter) {
    var branchKey by rememberSaveable { mutableStateOf(MenuBranch.PARK.name) }
    var categoryKey by rememberSaveable { mutableStateOf(MenuCategory.COFFEE.name) }
    val branch = MenuBranch.entries.firstOrNull { it.name == branchKey } ?: MenuBranch.PARK
    val category = MenuCategory.entries.firstOrNull { it.name == categoryKey } ?: MenuCategory.COFFEE

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
                branch = branch,
                category = category,
                onBranchSelected = { branchKey = it.name },
                onCategorySelected = { categoryKey = it.name },
            )
        }
    }
}

@Composable
fun MenuScreenContent(
    paddingValues: PaddingValues,
    branch: MenuBranch,
    category: MenuCategory,
    onBranchSelected: (MenuBranch) -> Unit,
    onCategorySelected: (MenuCategory) -> Unit,
) {
    val currentProducts = MenuCatalog.forSelection(branch, category)

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
        item {
            BranchSelector(
                selectedBranch = branch,
                onBranchSelected = onBranchSelected,
                modifier = Modifier.padding(bottom = 5.dp),
            )
        }
        item {
            CategorySelector(
                selectedCategory = category,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            )
        }
        if (currentProducts.isEmpty()) {
            item {
                EmptyMenuMessage()
            }
        } else {
            items(currentProducts, key = { it.id }) { product ->
                MenuProductCard(product = product)
            }
        }
    }
}

@Preview
@Composable
fun MenuScreenPreview() {
    MaterialTheme {
        MenuScreenContent(
            paddingValues = PaddingValues(0.dp),
            branch = MenuBranch.PARK,
            category = MenuCategory.COFFEE,
            onBranchSelected = {},
            onCategorySelected = {},
        )
    }
}
