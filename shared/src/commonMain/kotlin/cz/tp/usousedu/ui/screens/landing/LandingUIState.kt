package cz.tp.usousedu.ui.screens.landing

import cz.tp.usousedu.data.auth.AuthUser

sealed interface LandingUIState {
    data object Loading : LandingUIState

    data class Loaded(
        val user: AuthUser,
        val earnedStamps: Int,
        val maximumStamps: Int,
        val rewards: List<RewardItem>,
        val qrPayload: String,
        val selectedCafe: CafeInfo,
    ) : LandingUIState
}

data class RewardItem(
    val id: String,
    val name: String,
    val description: String,
    val requiredStamps: Int,
    val isAvailable: Boolean,
)

data class CafeInfo(
    val name: String,
    val address: String,
)
