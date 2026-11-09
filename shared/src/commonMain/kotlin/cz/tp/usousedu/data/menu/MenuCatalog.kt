package cz.tp.usousedu.data.menu

import cz.tp.usousedu.data.model.MenuCategory
import cz.tp.usousedu.data.model.Product
import cz.tp.usousedu.data.seed.SeedData.NADRAZI
import cz.tp.usousedu.data.seed.SeedData.NAMESTI
import cz.tp.usousedu.data.seed.SeedData.U_PARKU

/** Mock menu: source of the Firestore seed and of [FakeMenuRepository] (previews, tests). */
object MenuCatalog {
    private val allCafes = setOf(NAMESTI, U_PARKU, NADRAZI)
    private val squareAndPark = setOf(NAMESTI, U_PARKU)
    private val parkAndStation = setOf(U_PARKU, NADRAZI)

    val products: List<Product> = listOf(
        Product("espresso", "Espresso", "Naše směs z pražírny od vedle", 49, MenuCategory.COFFEE, allCafes),
        Product("cappuccino", "Cappuccino", "Espresso, napěněné mléko", 69, MenuCategory.COFFEE, allCafes, isPopular = true),
        Product("flat-white", "Flat white", "Dvojité espresso, jemná pěna", 79, MenuCategory.COFFEE, squareAndPark),
        Product("caffe-latte", "Caffè latte", "I s ovesným mlékem", 75, MenuCategory.COFFEE, parkAndStation),
        Product("batch-brew", "Filtrovaná káva", "Čerstvě připravená filtrovaná káva", 65, MenuCategory.COFFEE, setOf(NAMESTI)),
        Product("americano", "Americano", "Espresso doplněné horkou vodou", 59, MenuCategory.COFFEE, setOf(NADRAZI)),
        Product("cocoa", "Horká čokoláda", "Poctivá čokoláda se šlehačkou", 79, MenuCategory.DRINKS, allCafes, isPopular = true),
        Product("tea", "Sypaný čaj", "Výběr černých a ovocných čajů", 59, MenuCategory.DRINKS, allCafes),
        Product("lemonade", "Domácí limonáda", "Podle aktuální nabídky", 69, MenuCategory.DRINKS, squareAndPark),
        Product("juice", "Jablečný mošt", "Osvěžující jablečný nápoj", 55, MenuCategory.DRINKS, parkAndStation),
        Product("cheesecake", "Cheesecake", "Krémový dort s ovocem", 99, MenuCategory.CAKES, allCafes, isPopular = true),
        Product("carrot-cake", "Mrkvový dort", "S jemným krémem", 89, MenuCategory.CAKES, squareAndPark),
        Product("chocolate-cake", "Čokoládový dort", "Výrazná čokoládová chuť", 95, MenuCategory.CAKES, parkAndStation),
        Product("cookie", "Domácí sušenka", "Sladká drobnost ke kávě", 39, MenuCategory.CAKES, allCafes),
        Product("babyccino", "Babyccino", "Teplé našlehané mléko s kakaem", 35, MenuCategory.KIDS, allCafes),
        Product("kids-cocoa", "Dětské kakao", "Menší porce kakaa", 45, MenuCategory.KIDS, allCafes),
        Product("fruit-cup", "Ovocný kelímek", "Čerstvé sezónní ovoce", 55, MenuCategory.KIDS, squareAndPark),
        Product("mini-cookie", "Mini sušenky", "Malé sušenky pro děti", 39, MenuCategory.KIDS, parkAndStation),
    ).mapIndexed { index, product -> product.copy(order = index + 1) }

    fun forSelection(cafeId: String, category: MenuCategory): List<Product> =
        products.filter { cafeId in it.availableAt }.inCategory(category)
}

/** Products of one category in menu order. Shared by the ViewModel and the fake repository. */
fun List<Product>.inCategory(category: MenuCategory): List<Product> =
    filter { it.category == category }.sortedBy { it.order }
