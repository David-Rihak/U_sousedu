package cz.tp.usousedu.ui.screens.news

/** Categories used both by mock events and by the UI filters. */
enum class NewsCategory(val title: String) {
    KIDS("Pro děti"),
    FRIENDS("S přáteli"),
}

enum class NewsFilter(val title: String) {
    ALL("Vše"),
    KIDS("Pro děti"),
    FRIENDS("S přáteli"),
}

enum class NewsBannerStyle {
    APRICOT,
    MINT,
}

/**
 * Plain presentation data: no database or platform-specific APIs.
 * Later a repository can supply List<CafeEvent> from Firebase without
 * changing the card composables.
 */
data class CafeEvent(
    val id: String,
    val title: String,
    val day: String,
    val month: String,
    val schedule: String,
    val cafe: String,
    val description: String,
    val category: NewsCategory,
    val bannerStyle: NewsBannerStyle,
)

/** Temporary design/demo content. The dates are placeholders from the supplied mockup. */
object NewsCatalog {
    val events: List<CafeEvent> = listOf(
        CafeEvent(
            id = "gingerbread-workshop",
            title = "Dětské pečení perníčků",
            day = "28",
            month = "ZÁŘÍ",
            schedule = "So 10:00",
            cafe = "U parku",
            description = "Malé zástěry zapůjčíme, perníčky si děti odnesou domů.",
            category = NewsCategory.KIDS,
            bannerStyle = NewsBannerStyle.APRICOT,
        ),
        CafeEvent(
            id = "mothers-brunch",
            title = "Maminkovský brunch",
            day = "2",
            month = "ŘÍJ",
            schedule = "Čt 9:30",
            cafe = "Náměstí",
            description = "Klidné dopoledne, dětský koutek s dozorem a brunch menu.",
            category = NewsCategory.KIDS,
            bannerStyle = NewsBannerStyle.MINT,
        ),
        CafeEvent(
            id = "board-games",
            title = "Večer deskových her",
            day = "9",
            month = "ŘÍJ",
            schedule = "Pá 18:00",
            cafe = "U parku",
            description = "Přijďte si zahrát s přáteli. Hry a něco dobrého připravíme.",
            category = NewsCategory.FRIENDS,
            bannerStyle = NewsBannerStyle.APRICOT,
        ),
        CafeEvent(
            id = "coffee-tasting",
            title = "Společná degustace kávy",
            day = "16",
            month = "ŘÍJ",
            schedule = "Pá 17:00",
            cafe = "Náměstí",
            description = "Ochutnejte nové kávy a užijte si pohodový podvečer.",
            category = NewsCategory.FRIENDS,
            bannerStyle = NewsBannerStyle.MINT,
        ),
    )

    fun forFilter(filter: NewsFilter): List<CafeEvent> = when (filter) {
        NewsFilter.ALL -> events
        NewsFilter.KIDS -> events.filter { it.category == NewsCategory.KIDS }
        NewsFilter.FRIENDS -> events.filter { it.category == NewsCategory.FRIENDS }
    }
}
