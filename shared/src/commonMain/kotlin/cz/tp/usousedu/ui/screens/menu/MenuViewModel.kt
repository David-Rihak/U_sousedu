package cz.tp.usousedu.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.cafes.ICafeRepository
import cz.tp.usousedu.data.cafes.resolveCafeId
import cz.tp.usousedu.data.menu.IMenuRepository
import cz.tp.usousedu.data.menu.inCategory
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.model.MenuCategory
import cz.tp.usousedu.data.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

sealed interface MenuUIState {
    data object Loading : MenuUIState
    data class Error(val message: String) : MenuUIState
    data class Loaded(
        val cafes: List<Cafe>,
        val selectedCafeId: String?,
        val selectedCategory: MenuCategory,
        /** Products of the selected café and category. */
        val products: List<Product>,
    ) : MenuUIState
}

interface MenuActions {
    fun selectCafe(cafeId: String)
    fun selectCategory(category: MenuCategory)
    fun retry()
}

class MenuViewModel(
    private val cafeRepository: ICafeRepository,
    private val menuRepository: IMenuRepository,
    private val authRepository: IAuthRepository,
) : ViewModel(), MenuActions {

    private val retryTrigger = MutableStateFlow(0)
    /** Café picked in Menu; null = the user's favorite. Browsing does not change the favorite. */
    private val pickedCafeId = MutableStateFlow<String?>(null)
    private val selectedCategory = MutableStateFlow(MenuCategory.COFFEE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MenuUIState> = retryTrigger
        .flatMapLatest {
            combine(
                cafeRepository.observeCafes(),
                authRepository.currentUser.map { it?.favoriteCafeId }.distinctUntilChanged(),
                pickedCafeId,
            ) { cafes, favoriteId, pickedId ->
                CafeSelection(cafes, resolveCafeId(cafes, pickedId, favoriteId))
            }
                .distinctUntilChanged()
                .flatMapLatest { selection -> loadedState(selection) }
                .onStart { emit(MenuUIState.Loading) }
                .catch { emit(MenuUIState.Error("Menu se nepodařilo načíst.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUIState.Loading)

    // Products are queried per café; switching the category only re-filters them.
    private fun loadedState(selection: CafeSelection): Flow<MenuUIState> {
        val cafeId = selection.cafeId
        val products = if (cafeId == null) flowOf(emptyList()) else menuRepository.observeProducts(cafeId)
        return combine(products, selectedCategory) { all, category ->
            MenuUIState.Loaded(selection.cafes, cafeId, category, all.inCategory(category))
        }
    }

    override fun selectCafe(cafeId: String) {
        pickedCafeId.value = cafeId
    }

    override fun selectCategory(category: MenuCategory) {
        selectedCategory.value = category
    }

    override fun retry() {
        retryTrigger.value++
    }

    private data class CafeSelection(val cafes: List<Cafe>, val cafeId: String?)
}
