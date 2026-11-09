package cz.tp.usousedu.data.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Stored in Firestore by [name]. Never rename existing entries. */
enum class NewsCategory(val title: String) {
    KIDS("Pro děti"),
    FRIENDS("S přáteli"),
}

/** Color variant of the event banner. Stored in Firestore by [name]. */
enum class NewsBannerStyle {
    APRICOT,
    MINT,
}

/** One event in Aktuality (Firestore events/{id}). Day/month/time texts are derived in the UI. */
@OptIn(ExperimentalTime::class)
data class CafeEvent(
    val id: String,
    val title: String,
    val description: String,
    val startsAt: Instant,
    val cafeId: String,
    val category: NewsCategory,
    val bannerStyle: NewsBannerStyle,
)
