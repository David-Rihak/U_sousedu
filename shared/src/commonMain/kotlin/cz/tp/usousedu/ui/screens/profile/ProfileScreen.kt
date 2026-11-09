package cz.tp.usousedu.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.data.seed.SeedData
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.ScreenDestination
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.CafePickerBottomSheet
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.LoadingContent
import cz.tp.usousedu.ui.components.MainBottomNavigation
import cz.tp.usousedu.ui.screens.debug.DebugSeedSection
import org.koin.compose.viewmodel.koinViewModel

// Same palette as Menu, Aktuality and Kavárny.
internal object ProfileColors {
    val background = Color(0xFFFFF4E6)
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val accent = Color(0xFFE2572B)
    val white = Color.White
    val divider = Color(0xFFF6EADB)
    val badgeBackground = Color(0xFFDDEFE3)
    val badgeText = Color(0xFF177453)
}

@Composable
fun ProfileScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var showCafePicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ProfileColors.accent,
            onPrimary = ProfileColors.white,
            surface = ProfileColors.white,
            secondaryContainer = ProfileColors.white,
            onSecondaryContainer = ProfileColors.accent,
            onSurface = ProfileColors.text,
            surfaceContainer = ProfileColors.white,
            surfaceContainerLow = ProfileColors.white,
            onSurfaceVariant = ProfileColors.mutedText,
        ),
    ) {
        BaseScreen(
            containerColor = ProfileColors.background,
            bottomBar = {
                MainBottomNavigation(
                    selectedDestination = ScreenDestination.Profile,
                    onDestinationSelected = navigation::toMainDestination,
                )
            },
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                ProfileScreenContent(
                    paddingValues = paddingValues,
                    state = state,
                    actions = viewModel,
                    onLoggedOut = navigation::toLoginScreen,
                    onChangeCafe = { showCafePicker = true },
                    debugSection = { DebugSeedSection(modifier = Modifier.padding(top = 20.dp)) },
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(paddingValues),
                )
            }
        }

        // Same picker as on Karta; it is inside this MaterialTheme, so it takes the warm colors.
        val loadedState = state as? ProfileUIState.Loaded
        if (showCafePicker && loadedState != null) {
            CafePickerBottomSheet(
                cafes = loadedState.cafes,
                selectedCafeId = loadedState.cafe?.id,
                onCafeSelected = { cafeId ->
                    viewModel.changeCafe(cafeId)
                    showCafePicker = false
                },
                onDismissRequest = { showCafePicker = false },
            )
        }
    }
}

@Composable
fun ProfileScreenContent(
    paddingValues: PaddingValues,
    state: ProfileUIState,
    actions: ProfileActions,
    onLoggedOut: () -> Unit,
    onChangeCafe: () -> Unit,
    debugSection: @Composable () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ProfileColors.background).padding(paddingValues),
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Profil",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ProfileColors.text,
            )
        }

        when (state) {
            ProfileUIState.Loading -> item { LoadingContent() }

            is ProfileUIState.Error -> {
                item { ErrorContent(message = state.message, onRetry = actions::retry) }
                // Signing out must work even when the profile cannot be loaded.
                item { LogoutButton(onClick = { actions.logout(onLoggedOut) }) }
            }

            is ProfileUIState.Loaded -> {
                item {
                    ProfileCard(
                        state = state,
                        onChangeCafe = onChangeCafe,
                        canChangeCafe = state.cafes.size > 1,
                    )
                }
                item { LogoutButton(onClick = { actions.logout(onLoggedOut) }) }
                item { debugSection() }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    state: ProfileUIState.Loaded,
    onChangeCafe: () -> Unit,
    canChangeCafe: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(19.dp),
        color = ProfileColors.white,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Přihlášený účet",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ProfileColors.mutedText,
                )
                Text(
                    text = state.email,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileColors.text,
                )
                if (state.roleLabel != null) RoleBadge(state.roleLabel)
            }

            HorizontalDivider(color = ProfileColors.divider)

            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Moje kavárna",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileColors.mutedText,
                    )
                    Text(
                        text = state.cafe?.name ?: "Zatím žádná kavárna",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileColors.text,
                    )
                }
                if (canChangeCafe) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(role = Role.Button, onClick = onChangeCafe)
                            .defaultMinSize(minHeight = 44.dp)
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Změnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfileColors.accent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ProfileColors.badgeBackground)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ProfileColors.badgeText,
        )
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(ProfileColors.white)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Odhlásit se",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileColors.accent,
        )
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val cafe = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }.first()
    MaterialTheme {
        ProfileScreenContent(
            paddingValues = PaddingValues(0.dp),
            state = ProfileUIState.Loaded(email = "test@test.com", roleLabel = "Administrátor", cafe = cafe),
            actions = object : ProfileActions {
                override fun logout(onLoggedOut: () -> Unit) {}
                override fun retry() {}
                override fun changeCafe(cafeId: String) {}
                override fun messageShown() {}
            },
            onLoggedOut = {},
            onChangeCafe = {},
        )
    }
}
