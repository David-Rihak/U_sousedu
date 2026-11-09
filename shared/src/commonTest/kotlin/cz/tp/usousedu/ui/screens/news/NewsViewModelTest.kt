package cz.tp.usousedu.ui.screens.news

import cz.tp.usousedu.data.cafes.toCafe
import cz.tp.usousedu.data.model.NewsCategory
import cz.tp.usousedu.data.news.NewsCatalog
import cz.tp.usousedu.data.seed.SeedData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class NewsViewModelTest {
    private val prague = NewsCatalog.PRAGUE
    private val cafes = SeedData.cafes.map { (id, dto) -> dto.toCafe(id) }

    private fun at(day: Int, hour: Int, minute: Int = 0) =
        LocalDateTime(2026, 12, day, hour, minute).toInstant(prague)

    @Test
    fun formatsCzechDateTexts() {
        val texts = EventDateFormat.format(LocalDateTime(2026, 12, 11, 17, 0))
        assertEquals(EventDateTexts(day = "11", month = "PRO", schedule = "Pá 17:00"), texts)
        assertEquals("Čt 9:30", EventDateFormat.format(LocalDateTime(2026, 12, 17, 9, 30)).schedule)
        assertEquals("ZÁŘ", EventDateFormat.format(LocalDateTime(2026, 9, 28, 10, 0)).month)
    }

    @Test
    fun formatsInPragueTimeNotUtc() {
        // 17:00 UTC on 18 Dec is 18:00 in Prague (winter time, UTC+1).
        val texts = EventDateFormat.format(LocalDateTime(2026, 12, 18, 17, 0).toInstant(kotlinx.datetime.TimeZone.UTC), prague)
        assertEquals("Pá 18:00", texts.schedule)
    }

    @Test
    fun allMockEventsAreUpcomingBeforeDecember() {
        val items = NewsViewModel.upcomingItems(NewsCatalog.events, cafes, NewsFilter.ALL, at(1, 12), prague)
        assertEquals(
            listOf("coffee-tasting", "gingerbread-workshop", "mothers-brunch", "board-games"),
            items.map { it.event.id },
        )
        assertEquals(listOf("Náměstí", "U parku", "Náměstí", "U parku"), items.map { it.cafeName })
    }

    @Test
    fun hidesPastEventsButKeepsTodaysOnes() {
        // 12 Dec at 20:00: the tasting (11 Dec) is over, the workshop (12 Dec 10:00) is still shown.
        val items = NewsViewModel.upcomingItems(NewsCatalog.events, cafes, NewsFilter.ALL, at(12, 20), prague)
        assertEquals(listOf("gingerbread-workshop", "mothers-brunch", "board-games"), items.map { it.event.id })
    }

    @Test
    fun filterKeepsOnlyMatchingCategory() {
        val kids = NewsViewModel.upcomingItems(NewsCatalog.events, cafes, NewsFilter.KIDS, at(1, 12), prague)
        assertTrue(kids.isNotEmpty())
        assertTrue(kids.all { it.event.category == NewsCategory.KIDS })
    }

    @Test
    fun unknownCafeGivesEmptyName() {
        val event = NewsCatalog.events.first().copy(cafeId = "deleted-cafe")
        val items = NewsViewModel.upcomingItems(listOf(event), cafes, NewsFilter.ALL, at(1, 12), prague)
        assertEquals("", items.single().cafeName)
    }

    @Test
    fun seedEventsMatchCatalog() {
        assertEquals(NewsCatalog.events.map { it.id }.toSet(), SeedData.events.keys)
        val boardGames = SeedData.events.getValue("board-games")
        assertEquals(at(18, 18).epochSeconds, boardGames.startsAt.seconds)
        assertEquals(SeedData.U_PARKU, boardGames.cafeId)
    }
}
