package cz.tp.usousedu.ui.screens.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.auth.AuthUser
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.cafes.ICafeRepository
import cz.tp.usousedu.data.cafes.resolveCafeId
import cz.tp.usousedu.data.loyalty.ILoyaltyRepository
import cz.tp.usousedu.data.loyalty.IRewardsRepository
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.data.model.Reward
import cz.tp.usousedu.data.profile.IUserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface LoyaltyCardActions {
    fun retry()
    fun changeCafe(cafeId: String)
    fun messageShown()
}

class LandingViewModel(
    private val authRepository: IAuthRepository,
    private val loyaltyRepository: ILoyaltyRepository,
    private val rewardsRepository: IRewardsRepository,
    private val cafeRepository: ICafeRepository,
    private val profileRepository: IUserProfileRepository,
) : ViewModel(), LoyaltyCardActions {

    private val retryTrigger = MutableStateFlow(0)

    private val _message = MutableStateFlow<String?>(null)
    /** One-off message for a snackbar (e.g. a failed café change). */
    val message: StateFlow<String?> = _message.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LandingUIState> = retryTrigger
        .flatMapLatest {
            // Listeners are bound to the user id, so a favorite-café change does not reconnect them.
            authRepository.currentUser
                .map { it?.id }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    if (userId == null) flowOf(LandingUIState.Loading) else loadedState(userId)
                }
                .onStart { emit(LandingUIState.Loading) }
                .catch { emit(LandingUIState.Error("Věrnostní kartu se nepodařilo načíst.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LandingUIState.Loading)

    private fun loadedState(userId: String): Flow<LandingUIState> = combine(
        authRepository.currentUser.filterNotNull(),
        loyaltyRepository.observeCard(userId),
        rewardsRepository.observeRewards(),
        cafeRepository.observeCafes(),
    ) { user, card, rewards, cafes -> buildState(user, card, rewards, cafes) }

    override fun retry() {
        retryTrigger.value++
    }

    override fun changeCafe(cafeId: String) {
        viewModelScope.launch {
            profileRepository.setFavoriteCafe(cafeId).onFailure {
                _message.value = "Kavárnu se nepodařilo změnit. Zkuste to znovu."
            }
        }
    }

    override fun messageShown() {
        _message.value = null
    }

    companion object {
        fun buildState(
            user: AuthUser,
            card: LoyaltyCard,
            rewards: List<Reward>,
            cafes: List<Cafe>,
        ): LandingUIState.Loaded {
            val cafeId = resolveCafeId(cafes, pickedId = null, favoriteId = user.favoriteCafeId)
            return LandingUIState.Loaded(
                user = user,
                earnedStamps = card.stamps,
                maximumStamps = card.stampsGoal,
                rewards = rewards.map { reward ->
                    RewardItem(
                        id = reward.id,
                        name = reward.name,
                        description = reward.description,
                        requiredStamps = reward.requiredStamps,
                        isAvailable = reward.isAvailableFor(card),
                    )
                },
                // Demo payload only. A backend-issued, short-lived token will replace it (task 5.4).
                qrPayload = "usousedu-demo://loyalty/user/${user.id}",
                selectedCafe = cafes.firstOrNull { it.id == cafeId },
                cafes = cafes,
            )
        }
    }
}
