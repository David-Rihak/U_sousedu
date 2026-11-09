package cz.tp.usousedu.ui.screens.news

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Texts shown on an event card, e.g. "11" / "PRO" / "Pá 17:00". */
data class EventDateTexts(val day: String, val month: String, val schedule: String)

/** Czech date texts for event cards, computed in the given time zone (Prague by default). */
object EventDateFormat {
    private val months = listOf("LED", "ÚNO", "BŘE", "DUB", "KVĚ", "ČVN", "ČVC", "SRP", "ZÁŘ", "ŘÍJ", "LIS", "PRO")
    private val weekdays = listOf("Po", "Út", "St", "Čt", "Pá", "So", "Ne")

    @OptIn(ExperimentalTime::class)
    fun format(startsAt: Instant, timeZone: TimeZone): EventDateTexts =
        format(startsAt.toLocalDateTime(timeZone))

    fun format(dateTime: LocalDateTime): EventDateTexts {
        val minute = dateTime.minute.toString().padStart(2, '0')
        return EventDateTexts(
            day = dateTime.date.day.toString(),
            month = months[dateTime.month.ordinal],
            schedule = "${weekdays[dateTime.date.dayOfWeek.ordinal]} ${dateTime.hour}:$minute",
        )
    }
}
