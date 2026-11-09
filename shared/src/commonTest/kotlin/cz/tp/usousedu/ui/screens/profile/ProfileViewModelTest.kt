package cz.tp.usousedu.ui.screens.profile

import cz.tp.usousedu.data.auth.AuthUser
import cz.tp.usousedu.data.auth.UserRole
import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.data.seed.SeedData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileViewModelTest {
    private val cafes = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }.sortedBy { it.order }
    private val user = AuthUser(id = "uid-1", email = "test@test.com")

    @Test
    fun roleLabelOnlyForStaff() {
        assertNull(ProfileViewModel.staffRoleLabel(UserRole.CUSTOMER))
        assertEquals("Administrátor", ProfileViewModel.staffRoleLabel(UserRole.ADMIN))
        assertEquals("Majitel", ProfileViewModel.staffRoleLabel(UserRole.OWNER))
        assertNull(ProfileViewModel.buildState(user, cafes).roleLabel)
        assertEquals("Majitel", ProfileViewModel.buildState(user.copy(role = UserRole.OWNER), cafes).roleLabel)
    }

    @Test
    fun cafeIsFavoriteElseFirst() {
        val favorite = ProfileViewModel.buildState(user.copy(favoriteCafeId = SeedData.U_PARKU), cafes)
        assertEquals("U sousedů – U parku", favorite.cafe?.name)
        assertEquals(SeedData.NAMESTI, ProfileViewModel.buildState(user, cafes).cafe?.id)
        assertEquals(SeedData.NAMESTI, ProfileViewModel.buildState(user.copy(favoriteCafeId = "gone"), cafes).cafe?.id)
        assertNull(ProfileViewModel.buildState(user, emptyList()).cafe)
    }

    @Test
    fun pickerGetsAllCafesInOrder() {
        val loaded = ProfileViewModel.buildState(user, cafes)
        assertEquals(listOf(SeedData.NAMESTI, SeedData.U_PARKU, SeedData.NADRAZI), loaded.cafes.map { it.id })
    }

    @Test
    fun emailIsShown() {
        assertEquals("test@test.com", ProfileViewModel.buildState(user, cafes).email)
    }
}
