package cz.tp.usousedu.data.cafes

import cz.tp.usousedu.data.firestore.CafeDto
import cz.tp.usousedu.data.firestore.FirestoreCollections
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.seed.SeedData
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface ICafeRepository {
    /** Active cafés sorted by `order`. Emits again whenever the data changes. */
    fun observeCafes(): Flow<List<Cafe>>
}

class FirestoreCafeRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : ICafeRepository {

    // Filtered and sorted in the app: a where + orderBy on different fields would need an index.
    override fun observeCafes(): Flow<List<Cafe>> =
        firestore.collection(FirestoreCollections.CAFES).snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { it.toCafe() }.sortedBy { it.order }
        }

    private fun DocumentSnapshot.toCafe(): Cafe? {
        // A malformed document is skipped instead of failing the whole list.
        val dto = runCatching { data<CafeDto>() }.getOrNull() ?: return null
        if (!dto.isActive || dto.name.isBlank()) return null
        return dto.toCafe(id)
    }
}

/** Seed data, for previews and tests. */
class FakeCafeRepository(
    private val cafes: List<Cafe> = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }.sortedBy { it.order },
) : ICafeRepository {
    override fun observeCafes(): Flow<List<Cafe>> = flowOf(cafes)
}

internal fun CafeDto.toCafe(id: String) = Cafe(
    id = id,
    name = name,
    shortName = shortName.ifBlank { name },
    address = address,
    openingHours = openingHours,
    order = order,
)
