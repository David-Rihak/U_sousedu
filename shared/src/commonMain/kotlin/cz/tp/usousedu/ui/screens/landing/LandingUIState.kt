package cz.tp.usousedu.ui.screens.landing

import cz.tp.usousedu.data.auth.AuthUser
import cz.tp.usousedu.data.model.Cafe

sealed interface LandingUIState {
    data object Loading : LandingUIState

    data class Error(val message: String) : LandingUIState

    data class Loaded(
        val user: AuthUser,
        val earnedStamps: Int,
        val maximumStamps: Int,
        val rewards: List<RewardItem>,
        val qrPayload: String,
        /** Café in the header; null only when there are no cafés at all. */
        val selectedCafe: Cafe?,
        /** All cafés, for the "Změnit" picker. */
        val cafes: List<Cafe>,
    ) : LandingUIState
}

/** Reward as shown on the card, with availability already computed from the stamps. */
data class RewardItem(
    val id: String,
    val name: String,
    val description: String,
    val requiredStamps: Int,
    val isAvailable: Boolean,
)
