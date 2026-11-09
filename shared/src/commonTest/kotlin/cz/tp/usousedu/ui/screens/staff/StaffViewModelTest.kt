package cz.tp.usousedu.ui.screens.staff

import cz.tp.usousedu.data.loyalty.StampLimitException
import cz.tp.usousedu.data.model.Customer
import cz.tp.usousedu.data.model.LoyaltyCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StaffViewModelTest {
    @Test
    fun queryIsTrimmedAndLowercase() {
        assertEquals("test@test.com", StaffViewModel.normalizeQuery("  Test@TEST.com "))
        assertEquals("", StaffViewModel.normalizeQuery("   "))
    }

    @Test
    fun emptyResultIsNoMatch() {
        assertEquals(CustomerSearchState.NoMatch, StaffViewModel.searchResult(emptyList()))
        val customers = listOf(Customer(id = "1", email = "test@test.com"))
        val result = assertIs<CustomerSearchState.Results>(StaffViewModel.searchResult(customers))
        assertEquals(customers, result.customers)
    }

    @Test
    fun messagesForStampChanges() {
        val card = LoyaltyCard(stamps = 7, stampsGoal = 10)
        assertEquals("Razítko přidáno (7/10).", StaffViewModel.stampChangeMessage(1, card))
        assertEquals("Razítko odebráno (7/10).", StaffViewModel.stampChangeMessage(-1, card))
        assertEquals("Počet razítek je už na hranici karty.", StaffViewModel.stampChangeErrorMessage(StampLimitException()))
        assertEquals(
            "Změnu se nepodařilo uložit. Zkuste to znovu.",
            StaffViewModel.stampChangeErrorMessage(RuntimeException("PERMISSION_DENIED")),
        )
    }
}
