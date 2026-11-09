package cz.tp.usousedu.ui.screens.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.DI.AppConfig
import cz.tp.usousedu.data.seed.FirestoreSeeder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

sealed interface SeedUIState {
    data object Idle : SeedUIState
    data object Running : SeedUIState
    data class Done(val documentCount: Int) : SeedUIState
    data class Error(val message: String) : SeedUIState
}

class SeedViewModel(
    private val seeder: FirestoreSeeder,
    appConfig: AppConfig,
) : ViewModel() {

    val isAvailable: Boolean = appConfig.isDebug

    private val _uiState = MutableStateFlow<SeedUIState>(SeedUIState.Idle)
    val uiState: StateFlow<SeedUIState> = _uiState.asStateFlow()

    fun seed() {
        if (!isAvailable || _uiState.value == SeedUIState.Running) return
        viewModelScope.launch {
            _uiState.value = SeedUIState.Running
            _uiState.value = try {
                SeedUIState.Done(seeder.seed())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SeedUIState.Error(e.message ?: e::class.simpleName ?: "Neznámá chyba")
            }
        }
    }
}

/** Debug-only tools in Profile. Renders nothing in release builds and on iOS. */
@Composable
fun DebugSeedSection(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<SeedViewModel>()
    if (!viewModel.isAvailable) return
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Debug",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = viewModel::seed,
            enabled = state != SeedUIState.Running,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(if (state == SeedUIState.Running) "Nahrávám..." else "Nahrát testovací data")
        }
        val message = when (val current = state) {
            SeedUIState.Idle, SeedUIState.Running -> null
            is SeedUIState.Done -> "Nahráno ${current.documentCount} dokumentů."
            is SeedUIState.Error -> "Chyba: ${current.message}"
        }
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (state is SeedUIState.Error) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
