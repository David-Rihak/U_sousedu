package cz.tp.usousedu.data.news

import cz.tp.usousedu.data.model.CafeEvent
import cz.tp.usousedu.data.model.NewsBannerStyle
import cz.tp.usousedu.data.model.NewsCategory
import cz.tp.usousedu.data.seed.SeedData.NAMESTI
import cz.tp.usousedu.data.seed.SeedData.U_PARKU
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

/**
 * Mock events: source of the Firestore seed and of [FakeNewsRepository] (previews, tests).
 * Dates are in December 2026 so they stay upcoming until the end of the semester (decision D.3).
 */
@OptIn(ExperimentalTime::class)
object NewsCatalog {
    val PRAGUE: TimeZone = TimeZone.of("Europe/Prague")

    val events: List<CafeEvent> = listOf(
        CafeEvent(
            id = "coffee-tasting",
            title = "Společná degustace kávy",
            description = "Ochutnejte nové kávy a užijte si pohodový podvečer.",
            startsAt = LocalDateTime(2026, 12, 11, 17, 0).toInstant(PRAGUE),
            cafeId = NAMESTI,
            category = NewsCategory.FRIENDS,
            bannerStyle = NewsBannerStyle.MINT,
        ),
        CafeEvent(
            id = "gingerbread-workshop",
            title = "Dětské pečení perníčků",
            description = "Malé zástěry zapůjčíme, perníčky si děti odnesou domů.",
            startsAt = LocalDateTime(2026, 12, 12, 10, 0).toInstant(PRAGUE),
            cafeId = U_PARKU,
            category = NewsCategory.KIDS,
            bannerStyle = NewsBannerStyle.APRICOT,
        ),
        CafeEvent(
            id = "mothers-brunch",
            title = "Maminkovský brunch",
            description = "Klidné dopoledne, dětský koutek s dozorem a brunch menu.",
            startsAt = LocalDateTime(2026, 12, 17, 9, 30).toInstant(PRAGUE),
            cafeId = NAMESTI,
            category = NewsCategory.KIDS,
            bannerStyle = NewsBannerStyle.MINT,
        ),
        CafeEvent(
            id = "board-games",
            title = "Večer deskových her",
            description = "Přijďte si zahrát s přáteli. Hry a něco dobrého připravíme.",
            startsAt = LocalDateTime(2026, 12, 18, 18, 0).toInstant(PRAGUE),
            cafeId = U_PARKU,
            category = NewsCategory.FRIENDS,
            bannerStyle = NewsBannerStyle.APRICOT,
        ),
    )
}
