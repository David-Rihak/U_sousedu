package cz.tp.usousedu.ui.screens.landing

import cz.tp.usousedu.data.auth.AuthUser
import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.data.loyalty.LoyaltyCatalog
import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.data.seed.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LandingViewModelTest {
    private val cafes = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }.sortedBy { it.order }
    private val user = AuthUser(id = "uid-1", email = "test@test.com")

    private fun state(stamps: Int, favorite: String? = null) = LandingViewModel.buildState(
        user = user.copy(favoriteCafeId = favorite),
        card = LoyaltyCard(stamps = stamps, stampsGoal = 10),
        rewards = LoyaltyCatalog.rewards,
        cafes = cafes,
    )

    private fun LandingUIState.Loaded.available() = rewards.filter { it.isAvailable }.map { it.id }

    @Test
    fun rewardAvailabilityFollowsStamps() {
        assertEquals(emptyList(), state(4).available())
        assertEquals(listOf("dessert-discount"), state(5).available())
        assertEquals(listOf("dessert-discount"), state(9).available())
        assertEquals(listOf("dessert-discount", "free-coffee"), state(10).available())
    }

    @Test
    fun missingCardMeansZeroOfTen() {
        val loaded = LandingViewModel.buildState(user, LoyaltyCard.EMPTY, LoyaltyCatalog.rewards, cafes)
        assertEquals(0, loaded.earnedStamps)
        assertEquals(10, loaded.maximumStamps)
        assertTrue(loaded.rewards.none { it.isAvailable })
    }

    @Test
    fun headerShowsFavoriteCafeElseFirst() {
        assertEquals(SeedData.NADRAZI, state(6, favorite = SeedData.NADRAZI).selectedCafe?.id)
        assertEquals(SeedData.NAMESTI, state(6, favorite = null).selectedCafe?.id)
        assertEquals(SeedData.NAMESTI, state(6, favorite = "deleted-cafe").selectedCafe?.id)
    }

    @Test
    fun noCafesMeansNoHeaderCafe() {
        val loaded = LandingViewModel.buildState(user, LoyaltyCatalog.card, LoyaltyCatalog.rewards, emptyList())
        assertNull(loaded.selectedCafe)
    }

    @Test
    fun qrPayloadContainsUserId() {
        assertEquals("usousedu-demo://loyalty/user/uid-1", state(6).qrPayload)
    }

    @Test
    fun seedLoyaltyMatchesCatalog() {
        assertEquals(LoyaltyCatalog.rewards.map { it.id }.toSet(), SeedData.rewards.keys)
        assertEquals(5, SeedData.rewards.getValue("dessert-discount").requiredStamps)
        assertEquals(6, SeedData.loyaltyCard.stamps)
        assertEquals(10, SeedData.loyaltyCard.stampsGoal)
    }
}
