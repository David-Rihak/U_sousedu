package cz.tp.usousedu.data.model

/** Stored in Firestore by [name], e.g. "COFFEE". Never rename existing entries. */
enum class MenuCategory(val title: String) {
    COFFEE("Káva"),
    DRINKS("Nápoje"),
    CAKES("Dorty"),
    KIDS("Pro děti"),
}

/** One menu item (Firestore products/{id}). */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val priceCzk: Int,
    val category: MenuCategory,
    /** IDs of cafés that serve it. */
    val availableAt: Set<String>,
    val isPopular: Boolean = false,
    val order: Int = 0,
)
