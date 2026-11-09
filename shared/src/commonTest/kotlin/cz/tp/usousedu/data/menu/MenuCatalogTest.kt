package cz.tp.usousedu.data.menu

import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.model.MenuCategory
import cz.tp.usousedu.data.seed.SeedData
import cz.tp.usousedu.data.cafes.resolveCafeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MenuCatalogTest {
    private val cafeIds = listOf(SeedData.NAMESTI, SeedData.U_PARKU, SeedData.NADRAZI)

    @Test
    fun parkCoffeeMatchesReference() {
        val coffees = MenuCatalog.forSelection(SeedData.U_PARKU, MenuCategory.COFFEE)
        assertEquals(listOf("Espresso", "Cappuccino", "Flat white", "Caffè latte"), coffees.map { it.name })
        assertEquals(listOf(49, 69, 79, 75), coffees.map { it.priceCzk })
        assertTrue(coffees.single { it.name == "Cappuccino" }.isPopular)
    }

    @Test
    fun everyCafeAndCategoryHasProducts() {
        cafeIds.forEach { cafeId ->
            MenuCategory.entries.forEach { category ->
                val visible = MenuCatalog.forSelection(cafeId, category)
                assertTrue(visible.isNotEmpty(), "Empty selection: $cafeId / $category")
                assertTrue(visible.all { cafeId in it.availableAt && it.category == category })
            }
        }
    }

    @Test
    fun cafesChangeVisibleMenu() {
        val square = MenuCatalog.forSelection(SeedData.NAMESTI, MenuCategory.COFFEE)
        val station = MenuCatalog.forSelection(SeedData.NADRAZI, MenuCategory.COFFEE)
        assertFalse(square == station)
        assertTrue(square.any { it.id == "batch-brew" })
        assertTrue(station.any { it.id == "americano" })
    }

    @Test
    fun productsHaveValidUniqueIdsPricesAndCafes() {
        val products = MenuCatalog.products
        assertEquals(products.size, products.map { it.id }.distinct().size)
        assertTrue(products.all {
            it.id.isNotBlank() && it.name.isNotBlank() && it.description.isNotBlank() &&
                it.priceCzk > 0 && it.availableAt.isNotEmpty() && cafeIds.containsAll(it.availableAt)
        })
        assertEquals(products.indices.map { it + 1 }, products.map { it.order })
    }

    @Test
    fun seedProductsMatchCatalog() {
        assertEquals(MenuCatalog.products.map { it.id }.toSet(), SeedData.products.keys)
        val flatWhite = SeedData.products.getValue("flat-white")
        assertEquals(listOf(SeedData.NAMESTI, SeedData.U_PARKU), flatWhite.availableAt)
        assertEquals("COFFEE", flatWhite.category)
    }

    @Test
    fun cafeResolutionPrefersPickedThenFavoriteThenFirst() {
        val cafes = cafeIds.mapIndexed { i, id -> Cafe(id, id, id, "", emptyList(), i) }
        assertEquals(SeedData.NADRAZI, resolveCafeId(cafes, SeedData.NADRAZI, SeedData.U_PARKU))
        assertEquals(SeedData.U_PARKU, resolveCafeId(cafes, null, SeedData.U_PARKU))
        assertEquals(SeedData.NAMESTI, resolveCafeId(cafes, null, null))
        assertEquals(SeedData.NAMESTI, resolveCafeId(cafes, "deleted-cafe", "also-deleted"))
        assertNull(resolveCafeId(emptyList(), null, SeedData.U_PARKU))
    }
}
