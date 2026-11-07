package cz.tp.usousedu.ui.screens.menu

enum class MenuBranch(val title: String) {
    SQUARE("Náměstí"),
    PARK("U parku"),
    STATION("Nádraží"),
}

enum class MenuCategory(val title: String) {
    COFFEE("Káva"),
    DRINKS("Nápoje"),
    CAKES("Dorty"),
    KIDS("Pro děti"),
}

data class MenuProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceCzk: Int,
    val category: MenuCategory,
    val availableAt: Set<MenuBranch>,
    val isPopular: Boolean = false,
)

object MenuCatalog {
    private val allBranches = MenuBranch.entries.toSet()
    private val squareAndPark = setOf(MenuBranch.SQUARE, MenuBranch.PARK)
    private val parkAndStation = setOf(MenuBranch.PARK, MenuBranch.STATION)

    val products: List<MenuProduct> = listOf(
        MenuProduct("espresso", "Espresso", "Naše směs z pražírny od vedle", 49, MenuCategory.COFFEE, allBranches),
        MenuProduct("cappuccino", "Cappuccino", "Espresso, napěněné mléko", 69, MenuCategory.COFFEE, allBranches, isPopular = true),
        MenuProduct("flat-white", "Flat white", "Dvojité espresso, jemná pěna", 79, MenuCategory.COFFEE, squareAndPark),
        MenuProduct("caffe-latte", "Caffè latte", "I s ovesným mlékem", 75, MenuCategory.COFFEE, parkAndStation),
        MenuProduct("batch-brew", "Filtrovaná káva", "Čerstvě připravená filtrovaná káva", 65, MenuCategory.COFFEE, setOf(MenuBranch.SQUARE)),
        MenuProduct("americano", "Americano", "Espresso doplněné horkou vodou", 59, MenuCategory.COFFEE, setOf(MenuBranch.STATION)),
        MenuProduct("cocoa", "Horká čokoláda", "Poctivá čokoláda se šlehačkou", 79, MenuCategory.DRINKS, allBranches, isPopular = true),
        MenuProduct("tea", "Sypaný čaj", "Výběr černých a ovocných čajů", 59, MenuCategory.DRINKS, allBranches),
        MenuProduct("lemonade", "Domácí limonáda", "Podle aktuální nabídky", 69, MenuCategory.DRINKS, squareAndPark),
        MenuProduct("juice", "Jablečný mošt", "Osvěžující jablečný nápoj", 55, MenuCategory.DRINKS, parkAndStation),
        MenuProduct("cheesecake", "Cheesecake", "Krémový dort s ovocem", 99, MenuCategory.CAKES, allBranches, isPopular = true),
        MenuProduct("carrot-cake", "Mrkvový dort", "S jemným krémem", 89, MenuCategory.CAKES, squareAndPark),
        MenuProduct("chocolate-cake", "Čokoládový dort", "Výrazná čokoládová chuť", 95, MenuCategory.CAKES, parkAndStation),
        MenuProduct("cookie", "Domácí sušenka", "Sladká drobnost ke kávě", 39, MenuCategory.CAKES, allBranches),
        MenuProduct("babyccino", "Babyccino", "Teplé našlehané mléko s kakaem", 35, MenuCategory.KIDS, allBranches),
        MenuProduct("kids-cocoa", "Dětské kakao", "Menší porce kakaa", 45, MenuCategory.KIDS, allBranches),
        MenuProduct("fruit-cup", "Ovocný kelímek", "Čerstvé sezónní ovoce", 55, MenuCategory.KIDS, squareAndPark),
        MenuProduct("mini-cookie", "Mini sušenky", "Malé sušenky pro děti", 39, MenuCategory.KIDS, parkAndStation),
    )

    fun forSelection(branch: MenuBranch, category: MenuCategory): List<MenuProduct> =
        products.filter { it.category == category && branch in it.availableAt }
}
