package cz.tp.usousedu.data.news

import cz.tp.usousedu.data.firestore.EventDto
import cz.tp.usousedu.data.firestore.FirestoreCollections
import cz.tp.usousedu.data.model.CafeEvent
import cz.tp.usousedu.data.model.NewsBannerStyle
import cz.tp.usousedu.data.model.NewsCategory
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface INewsRepository {
    /** Published events (past ones included) sorted by start time. */
    fun observeEvents(): Flow<List<CafeEvent>>
}

@OptIn(ExperimentalTime::class)
class FirestoreNewsRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : INewsRepository {

    // Filtered and sorted in the app: a where + orderBy on different fields would need an index.
    override fun observeEvents(): Flow<List<CafeEvent>> =
        firestore.collection(FirestoreCollections.EVENTS).snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { it.toEvent() }.sortedBy { it.startsAt }
        }

    private fun DocumentSnapshot.toEvent(): CafeEvent? {
        // Malformed documents and unknown enum values are skipped, not fatal.
        val dto = runCatching { data<EventDto>() }.getOrNull() ?: return null
        if (!dto.isPublished || dto.title.isBlank()) return null
        val category = NewsCategory.entries.firstOrNull { it.name == dto.category } ?: return null
        val banner = NewsBannerStyle.entries.firstOrNull { it.name == dto.bannerStyle } ?: NewsBannerStyle.APRICOT
        return CafeEvent(
            id = id,
            title = dto.title,
            description = dto.description,
            startsAt = Instant.fromEpochSeconds(dto.startsAt.seconds, dto.startsAt.nanoseconds),
            cafeId = dto.cafeId,
            category = category,
            bannerStyle = banner,
        )
    }
}

/** Mock events, for previews and tests. */
@OptIn(ExperimentalTime::class)
class FakeNewsRepository(
    private val events: List<CafeEvent> = NewsCatalog.events,
) : INewsRepository {
    override fun observeEvents(): Flow<List<CafeEvent>> = flowOf(events.sortedBy { it.startsAt })
}
