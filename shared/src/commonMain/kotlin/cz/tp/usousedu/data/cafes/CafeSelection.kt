package cz.tp.usousedu.data.cafes

import cz.tp.usousedu.data.model.Cafe

/**
 * Which café to show: the one [pickedId] by the user on this screen, else the [favoriteId],
 * else the first one. IDs of cafés that no longer exist are ignored.
 * Shared by Menu, Kavárny and Karta so they always agree.
 */
fun resolveCafeId(cafes: List<Cafe>, pickedId: String?, favoriteId: String?): String? {
    val ids = cafes.map { it.id }
    return pickedId?.takeIf { it in ids } ?: favoriteId?.takeIf { it in ids } ?: ids.firstOrNull()
}
