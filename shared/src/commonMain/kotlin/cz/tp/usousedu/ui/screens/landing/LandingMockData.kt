package cz.tp.usousedu.ui.screens.landing

import cz.tp.usousedu.data.auth.AuthUser

internal object LandingMockData {
    private const val EARNED_STAMPS = 6
    private const val MAXIMUM_STAMPS = 10

    fun forUser(user: AuthUser): LandingUIState.Loaded = LandingUIState.Loaded(
        user = user,
        earnedStamps = EARNED_STAMPS,
        maximumStamps = MAXIMUM_STAMPS,
        rewards = listOf(
            RewardItem(
                id = "dessert-discount",
                name = "Sleva 20 % na dezert",
                description = "Odměnu lze uplatnit u obsluhy při další návštěvě.",
                requiredStamps = 5,
                isAvailable = true,
            ),
            RewardItem(
                id = "free-coffee",
                name = "Káva zdarma",
                description = "Libovolná klasická káva z nabídky zdarma.",
                requiredStamps = 10,
                isAvailable = false,
            ),
        ),
        // Demo payload only. A backend-issued, short-lived token will replace it later.
        qrPayload = "usousedu-demo://loyalty/user/${user.id}",
        selectedCafe = CafeInfo(
            name = "U sousedů – Centrum",
            address = "Náměstí 12, Praha",
        ),
    )
}
