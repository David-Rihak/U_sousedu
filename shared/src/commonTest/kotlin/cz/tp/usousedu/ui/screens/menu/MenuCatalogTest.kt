package cz.tp.usousedu.ui.screens.menu

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MenuCatalogTest {
    @Test
    fun parkCoffeeMatchesReference() {
        val coffees = MenuCatalog.forSelection(MenuBranch.PARK, MenuCategory.COFFEE)
        assertEquals(listOf("Espresso", "Cappuccino", "Flat white", "Caffè latte"), coffees.map { it.name })
        assertEquals(listOf(49, 69, 79, 75), coffees.map { it.priceCzk })
        assertTrue(coffees.single { it.name == "Cappuccino" }.isPopular)
    }

    @Test
    fun everyBranchAndCategoryHasProducts() {
        MenuBranch.entries.forEach { branch ->
            MenuCategory.entries.forEach { category ->
                val visible = MenuCatalog.forSelection(branch, category)
                assertTrue(visible.isNotEmpty(), "Empty selection: $branch / $category")
                assertTrue(visible.all { branch in it.availableAt && it.category == category })
            }
        }
    }

    @Test
    fun branchesChangeVisibleMenu() {
        val square = MenuCatalog.forSelection(MenuBranch.SQUARE, MenuCategory.COFFEE)
        val station = MenuCatalog.forSelection(MenuBranch.STATION, MenuCategory.COFFEE)
        assertFalse(square == station)
        assertTrue(square.any { it.id == "batch-brew" })
        assertTrue(station.any { it.id == "americano" })
    }

    @Test
    fun productsHaveValidUniqueIdsAndPrices() {
        assertEquals(MenuCatalog.products.size, MenuCatalog.products.map { it.id }.distinct().size)
        assertTrue(MenuCatalog.products.all {
            it.id.isNotBlank() && it.name.isNotBlank() && it.description.isNotBlank() &&
                it.priceCzk > 0 && it.availableAt.isNotEmpty()
        })
    }
}
