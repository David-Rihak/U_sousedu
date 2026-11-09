package cz.tp.usousedu.data.loyalty

import cz.tp.usousedu.data.auth.UserRole
import cz.tp.usousedu.data.auth.isStaff
import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.data.model.withStampChange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StampChangeTest {
    @Test
    fun stampsStayWithinCard() {
        val card = LoyaltyCard(stamps = 6, stampsGoal = 10)
        assertEquals(7, card.withStampChange(1)?.stamps)
        assertEquals(5, card.withStampChange(-1)?.stamps)
        assertEquals(10, card.withStampChange(4)?.stamps)
        assertNull(card.withStampChange(5))
        assertEquals(0, LoyaltyCard(stamps = 1, stampsGoal = 10).withStampChange(-1)?.stamps)
        assertNull(LoyaltyCard.EMPTY.withStampChange(-1))
        assertNull(LoyaltyCard(stamps = 10, stampsGoal = 10).withStampChange(1))
    }

    @Test
    fun onlyAdminAndOwnerAreStaff() {
        assertFalse(UserRole.CUSTOMER.isStaff)
        assertTrue(UserRole.ADMIN.isStaff)
        assertTrue(UserRole.OWNER.isStaff)
    }
}
