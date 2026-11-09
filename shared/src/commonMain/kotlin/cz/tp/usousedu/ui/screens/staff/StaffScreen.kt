package cz.tp.usousedu.ui.screens.staff

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.data.model.Customer
import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.ErrorContent
import cz.tp.usousedu.ui.components.LoadingContent
import org.koin.compose.viewmodel.koinViewModel

// Same warm palette as Profil, Menu and Aktuality.
private object StaffColors {
    val background = Color(0xFFFFF4E6)
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val accent = Color(0xFFE2572B)
    val white = Color.White
    val disabled = Color(0xFFE9D9C8)
    val stampEmpty = Color(0xFFF6EADB)
}

@Composable
fun StaffScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<StaffViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = StaffColors.accent,
            onPrimary = StaffColors.white,
            surface = StaffColors.white,
            onSurface = StaffColors.text,
            onSurfaceVariant = StaffColors.mutedText,
        ),
    ) {
        BaseScreen(containerColor = StaffColors.background) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                StaffScreenContent(
                    paddingValues = paddingValues,
                    state = state,
                    actions = viewModel,
                    onBack = navigation::navigateBack,
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(paddingValues),
                )
            }
        }
    }
}

@Composable
fun StaffScreenContent(
    paddingValues: PaddingValues,
    state: StaffUIState,
    actions: StaffActions,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(StaffColors.background).padding(paddingValues),
        contentPadding = PaddingValues(start = 26.dp, end = 26.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Header(onBack = onBack) }

        when (state) {
            StaffUIState.Loading -> item { LoadingContent() }

            StaffUIState.NotAllowed -> item { InfoText("Tato obrazovka je jen pro personál.") }

            is StaffUIState.Ready -> {
                val selected = state.selected
                if (selected != null) {
                    item {
                        SelectedCustomerCard(
                            customer = selected,
                            card = state.card,
                            isSaving = state.isSaving,
                            onChangeStamps = actions::changeStamps,
                        )
                    }
                    item { SecondaryButton(text = "Vybrat jiného zákazníka", onClick = actions::clearSelection) }
                } else {
                    item { SearchField(query = state.query, onQueryChange = actions::onQueryChange) }
                    searchResults(state.search, actions)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.searchResults(
    search: CustomerSearchState,
    actions: StaffActions,
) {
    when (search) {
        CustomerSearchState.Idle -> item { InfoText("Začněte psát e-mail zákazníka.") }
        CustomerSearchState.Searching -> item { LoadingContent() }
        CustomerSearchState.NoMatch -> item { InfoText("Žádná shoda") }
        CustomerSearchState.Error -> item {
            ErrorContent(message = "Zákazníky se nepodařilo načíst.", onRetry = actions::retrySearch)
        }
        is CustomerSearchState.Results -> items(search.customers, key = { it.id }) { customer ->
            CustomerRow(customer = customer, onClick = { actions.selectCustomer(customer) })
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .clickable(role = Role.Button, onClick = onBack)
                .defaultMinSize(minHeight = 44.dp)
                .padding(end = 12.dp, top = 11.dp, bottom = 11.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(text = "← Zpět", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StaffColors.accent)
        }
        Text(
            text = "Režim obsluhy",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StaffColors.text,
        )
        Text(
            text = "Simulace načtení QR kódu: najděte zákazníka podle e-mailu.",
            fontSize = 13.sp,
            color = StaffColors.mutedText,
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("E-mail zákazníka") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Search),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = StaffColors.white,
            unfocusedContainerColor = StaffColors.white,
            focusedBorderColor = StaffColors.accent,
            focusedLabelColor = StaffColors.accent,
            cursorColor = StaffColors.accent,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CustomerRow(customer: Customer, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = StaffColors.white,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = customer.email,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = StaffColors.text,
                modifier = Modifier.weight(1f),
            )
            Text(text = "Vybrat", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StaffColors.accent)
        }
    }
}

@Composable
private fun SelectedCustomerCard(
    customer: Customer,
    card: SelectedCardState,
    isSaving: Boolean,
    onChangeStamps: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(19.dp),
        color = StaffColors.white,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Zákazník", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StaffColors.mutedText)
                Text(text = customer.email, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StaffColors.text)
            }

            when (card) {
                SelectedCardState.Loading -> LoadingContent()
                SelectedCardState.Error -> InfoText("Kartu zákazníka se nepodařilo načíst.")
                is SelectedCardState.Loaded -> {
                    StampsSummary(card.card)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StampButton(
                            text = "− 1 razítko",
                            enabled = !isSaving && card.card.stamps > 0,
                            onClick = { onChangeStamps(-1) },
                            modifier = Modifier.weight(1f),
                        )
                        StampButton(
                            text = "+ 1 razítko",
                            enabled = !isSaving && card.card.stamps < card.card.stampsGoal,
                            onClick = { onChangeStamps(1) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StampsSummary(card: LoyaltyCard) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${card.stamps} / ${card.stampsGoal} razítek",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StaffColors.text,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(card.stampsGoal) { index ->
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (index < card.stamps) StaffColors.accent else StaffColors.stampEmpty),
                )
            }
        }
    }
}

@Composable
private fun StampButton(text: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (enabled) StaffColors.accent else StaffColors.disabled)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StaffColors.white)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(StaffColors.white)
            .clickable(role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StaffColors.accent)
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = StaffColors.mutedText,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
    )
}

private val previewActions = object : StaffActions {
    override fun onQueryChange(query: String) {}
    override fun retrySearch() {}
    override fun selectCustomer(customer: Customer) {}
    override fun clearSelection() {}
    override fun changeStamps(delta: Int) {}
    override fun messageShown() {}
}

@Preview
@Composable
fun StaffScreenSearchPreview() {
    MaterialTheme {
        StaffScreenContent(
            paddingValues = PaddingValues(0.dp),
            state = StaffUIState.Ready(
                query = "test",
                search = CustomerSearchState.Results(listOf(Customer(id = "1", email = "test@test.com"))),
            ),
            actions = previewActions,
            onBack = {},
        )
    }
}

@Preview
@Composable
fun StaffScreenSelectedPreview() {
    MaterialTheme {
        StaffScreenContent(
            paddingValues = PaddingValues(0.dp),
            state = StaffUIState.Ready(
                selected = Customer(id = "1", email = "test@test.com"),
                card = SelectedCardState.Loaded(LoyaltyCard(stamps = 6, stampsGoal = 10)),
            ),
            actions = previewActions,
            onBack = {},
        )
    }
}
