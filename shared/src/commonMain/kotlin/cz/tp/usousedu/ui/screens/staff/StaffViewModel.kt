package cz.tp.usousedu.ui.screens.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.auth.isStaff
import cz.tp.usousedu.data.customers.ICustomerRepository
import cz.tp.usousedu.data.loyalty.ILoyaltyRepository
import cz.tp.usousedu.data.loyalty.StampLimitException
import cz.tp.usousedu.data.model.Customer
import cz.tp.usousedu.data.model.LoyaltyCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Staff screen ("Režim obsluhy"): simulates scanning a customer's QR code by searching them by
// e-mail, then adds / removes stamps on their card.

sealed interface CustomerSearchState {
    /** Nothing typed yet. */
    data object Idle : CustomerSearchState
    data object Searching : CustomerSearchState
    data object NoMatch : CustomerSearchState
    data class Results(val customers: List<Customer>) : CustomerSearchState
    data object Error : CustomerSearchState
}

sealed interface SelectedCardState {
    data object Loading : SelectedCardState
    data class Loaded(val card: LoyaltyCard) : SelectedCardState
    data object Error : SelectedCardState
}

sealed interface StaffUIState {
    data object Loading : StaffUIState

    /** Signed-in user is not ADMIN / OWNER. */
    data object NotAllowed : StaffUIState

    data class Ready(
        val query: String = "",
        val search: CustomerSearchState = CustomerSearchState.Idle,
        /** Chosen customer; null = the search is shown. */
        val selected: Customer? = null,
        val card: SelectedCardState = SelectedCardState.Loading,
        /** A stamp change is being written; buttons are disabled meanwhile. */
        val isSaving: Boolean = false,
    ) : StaffUIState
}

interface StaffActions {
    fun onQueryChange(query: String)
    fun retrySearch()
    fun selectCustomer(customer: Customer)
    fun clearSelection()
    fun changeStamps(delta: Int)
    fun messageShown()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class StaffViewModel(
    private val authRepository: IAuthRepository,
    private val customerRepository: ICustomerRepository,
    private val loyaltyRepository: ILoyaltyRepository,
) : ViewModel(), StaffActions {

    private val query = MutableStateFlow("")
    private val searchRetry = MutableStateFlow(0)
    private val selected = MutableStateFlow<Customer?>(null)
    private val isSaving = MutableStateFlow(false)

    private val _message = MutableStateFlow<String?>(null)
    /** One-off message for a snackbar (result of a stamp change). */
    val message: StateFlow<String?> = _message.asStateFlow()

    private val searchState: Flow<CustomerSearchState> =
        combine(query.map(::normalizeQuery).distinctUntilChanged().debounce(SEARCH_DEBOUNCE_MS), searchRetry) { q, _ -> q }
            .flatMapLatest { q ->
                if (q.isEmpty()) {
                    flowOf(CustomerSearchState.Idle)
                } else {
                    flow<CustomerSearchState> {
                        emit(CustomerSearchState.Searching)
                        emit(searchResult(customerRepository.searchByEmail(q)))
                    }.catch { emit(CustomerSearchState.Error) }
                }
            }
            .onStart { emit(CustomerSearchState.Idle) }

    private val cardState: Flow<SelectedCardState> = selected
        .flatMapLatest { customer ->
            if (customer == null) {
                flowOf(SelectedCardState.Loading)
            } else {
                loyaltyRepository.observeCard(customer.id)
                    .map<LoyaltyCard, SelectedCardState> { SelectedCardState.Loaded(it) }
                    .onStart { emit(SelectedCardState.Loading) }
                    .catch { emit(SelectedCardState.Error) }
            }
        }

    private val readyState: Flow<StaffUIState.Ready> =
        combine(query, searchState, selected, cardState, isSaving) { q, search, customer, card, saving ->
            StaffUIState.Ready(query = q, search = search, selected = customer, card = card, isSaving = saving)
        }

    val uiState: StateFlow<StaffUIState> = authRepository.currentUser
        .map { it?.role?.isStaff == true }
        .distinctUntilChanged()
        .flatMapLatest { allowed -> if (allowed) readyState else flowOf(StaffUIState.NotAllowed) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StaffUIState.Loading)

    override fun onQueryChange(query: String) {
        this.query.value = query
    }

    override fun retrySearch() {
        searchRetry.value++
    }

    override fun selectCustomer(customer: Customer) {
        selected.value = customer
    }

    override fun clearSelection() {
        selected.value = null
    }

    override fun changeStamps(delta: Int) {
        val customer = selected.value ?: return
        if (isSaving.value) return
        isSaving.value = true
        viewModelScope.launch {
            val staffId = authRepository.currentUser.first()?.id
            _message.value = if (staffId == null) {
                "Nejste přihlášeni."
            } else {
                loyaltyRepository.changeStamps(customer.id, delta, staffId).fold(
                    onSuccess = { stampChangeMessage(delta, it) },
                    onFailure = { stampChangeErrorMessage(it) },
                )
            }
            isSaving.value = false
        }
    }

    override fun messageShown() {
        _message.value = null
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L

        /** E-mails are compared lowercase (Firebase Auth stores them that way). */
        fun normalizeQuery(query: String): String = query.trim().lowercase()

        fun searchResult(customers: List<Customer>): CustomerSearchState =
            if (customers.isEmpty()) CustomerSearchState.NoMatch else CustomerSearchState.Results(customers)

        fun stampChangeMessage(delta: Int, card: LoyaltyCard): String {
            val action = if (delta >= 0) "Razítko přidáno" else "Razítko odebráno"
            return "$action (${card.stamps}/${card.stampsGoal})."
        }

        fun stampChangeErrorMessage(error: Throwable): String = when (error) {
            is StampLimitException -> "Počet razítek je už na hranici karty."
            else -> "Změnu se nepodařilo uložit. Zkuste to znovu."
        }
    }
}
