package cz.tp.usousedu.data.menu

import cz.tp.usousedu.data.firestore.FirestoreCollections
import cz.tp.usousedu.data.firestore.ProductDto
import cz.tp.usousedu.data.model.MenuCategory
import cz.tp.usousedu.data.model.Product
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface IMenuRepository {
    /** Active products served in [cafeId], all categories, sorted by `order`. */
    fun observeProducts(cafeId: String): Flow<List<Product>>
}

class FirestoreMenuRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : IMenuRepository {

    // Only the café is queried; the category is filtered in the app (no composite index needed).
    override fun observeProducts(cafeId: String): Flow<List<Product>> =
        firestore.collection(FirestoreCollections.PRODUCTS)
            .where { "availableAt" contains cafeId }
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.toProduct() }.sortedBy { it.order } }

    private fun DocumentSnapshot.toProduct(): Product? {
        // Malformed documents and unknown categories are skipped, not fatal.
        val dto = runCatching { data<ProductDto>() }.getOrNull() ?: return null
        val category = MenuCategory.entries.firstOrNull { it.name == dto.category } ?: return null
        if (!dto.isActive || dto.name.isBlank()) return null
        return Product(
            id = id,
            name = dto.name,
            description = dto.description,
            priceCzk = dto.priceCzk,
            category = category,
            availableAt = dto.availableAt.toSet(),
            isPopular = dto.isPopular,
            order = dto.order,
        )
    }
}

/** Mock menu, for previews and tests. */
class FakeMenuRepository(
    private val products: List<Product> = MenuCatalog.products,
) : IMenuRepository {
    override fun observeProducts(cafeId: String): Flow<List<Product>> =
        flowOf(products.filter { cafeId in it.availableAt }.sortedBy { it.order })
}
