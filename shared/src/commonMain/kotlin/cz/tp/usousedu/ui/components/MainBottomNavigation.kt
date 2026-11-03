package cz.tp.usousedu.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cz.tp.usousedu.navigation.ScreenDestination
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import usousedu.shared.generated.resources.Res
import usousedu.shared.generated.resources.ic_card
import usousedu.shared.generated.resources.ic_cafe
import usousedu.shared.generated.resources.ic_menu
import usousedu.shared.generated.resources.ic_news
import usousedu.shared.generated.resources.ic_profile

private data class MainNavigationItem(
    val destination: ScreenDestination,
    val label: String,
    val icon: DrawableResource,
)

@Composable
fun MainBottomNavigation(
    selectedDestination: ScreenDestination,
    onDestinationSelected: (ScreenDestination) -> Unit,
) {
    val items = listOf(
        MainNavigationItem(ScreenDestination.Landing, "Karta", Res.drawable.ic_card),
        MainNavigationItem(ScreenDestination.Menu, "Menu", Res.drawable.ic_menu),
        MainNavigationItem(ScreenDestination.News, "Aktuality", Res.drawable.ic_news),
        MainNavigationItem(ScreenDestination.Cafes, "Kavárny", Res.drawable.ic_cafe),
        MainNavigationItem(ScreenDestination.Profile, "Profil", Res.drawable.ic_profile),
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedDestination == item.destination,
                onClick = {
                    if (selectedDestination != item.destination) {
                        onDestinationSelected(item.destination)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}
