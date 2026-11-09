package cz.tp.usousedu.ui.screens.cafes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.cafes.ICafeRepository
import cz.tp.usousedu.data.cafes.resolveCafeId
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.profile.IUserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CafesUIState {
    data object Loading : CafesUIState
    data class Error(val message: String) : CafesUIState
    data class Loaded(
        val cafes: List<Cafe>,
        /** The user's café; falls back to the first one when none is chosen. */
        val favoriteCafeId: String?,
        /** Café whose "set as mine" request is running, to disable its button. */
        val savingCafeId: String? = null,
    ) : CafesUIState
}

interface CafesActions {
    fun retry()
    fun setFavorite(cafeId: String)
    fun messageShown()
}

class CafesViewModel(
    private val cafeRepository: ICafeRepository,
    private val authRepository: IAuthRepository,
    private val profileRepository: IUserProfileRepository,
) : ViewModel(), CafesActions {

    private val retryTrigger = MutableStateFlow(0)
    private val savingCafeId = MutableStateFlow<String?>(null)

    private val _message = MutableStateFlow<String?>(null)
    /** One-off message for a snackbar (e.g. a failed save). */
    val message: StateFlow<String?> = _message.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CafesUIState> = retryTrigger
        .flatMapLatest {
            combine(
                cafeRepository.observeCafes(),
                authRepository.currentUser.map { it?.favoriteCafeId },
                savingCafeId,
            ) { cafes, favoriteId, saving ->
                CafesUIState.Loaded(cafes, resolveCafeId(cafes, null, favoriteId), saving) as CafesUIState
            }
                .onStart { emit(CafesUIState.Loading) }
                .catch { emit(CafesUIState.Error("Kavárny se nepodařilo načíst.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CafesUIState.Loading)

    override fun retry() {
        retryTrigger.value++
    }

    override fun setFavorite(cafeId: String) {
        if (savingCafeId.value != null) return
        viewModelScope.launch {
            savingCafeId.value = cafeId
            profileRepository.setFavoriteCafe(cafeId).fold(
                onSuccess = { _message.value = "Kavárna nastavena jako vaše." },
                onFailure = { _message.value = "Kavárnu se nepodařilo uložit. Zkuste to znovu." },
            )
            savingCafeId.value = null
        }
    }

    override fun messageShown() {
        _message.value = null
    }
}
