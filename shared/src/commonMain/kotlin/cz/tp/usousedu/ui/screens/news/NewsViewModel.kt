package cz.tp.usousedu.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.tp.usousedu.data.cafes.ICafeRepository
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.model.CafeEvent
import cz.tp.usousedu.data.model.NewsCategory
import cz.tp.usousedu.data.news.INewsRepository
import cz.tp.usousedu.data.news.NewsCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Filter chips in Aktuality (UI only, not stored anywhere). */
enum class NewsFilter(val title: String, private val category: NewsCategory?) {
    ALL("Vše", null),
    KIDS("Pro děti", NewsCategory.KIDS),
    FRIENDS("S přáteli", NewsCategory.FRIENDS);

    fun matches(eventCategory: NewsCategory): Boolean = category == null || category == eventCategory
}

/** Event ready for the card: date texts and café name already resolved. */
data class NewsEventItem(
    val event: CafeEvent,
    val dateTexts: EventDateTexts,
    val cafeName: String,
)

sealed interface NewsUIState {
    data object Loading : NewsUIState
    data class Error(val message: String) : NewsUIState
    data class Loaded(val filter: NewsFilter, val events: List<NewsEventItem>) : NewsUIState
}

interface NewsActions {
    fun selectFilter(filter: NewsFilter)
    fun retry()
}

@OptIn(ExperimentalTime::class)
class NewsViewModel(
    private val newsRepository: INewsRepository,
    private val cafeRepository: ICafeRepository,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = NewsCatalog.PRAGUE,
) : ViewModel(), NewsActions {

    private val retryTrigger = MutableStateFlow(0)
    private val selectedFilter = MutableStateFlow(NewsFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NewsUIState> = retryTrigger
        .flatMapLatest {
            combine(
                newsRepository.observeEvents(),
                cafeRepository.observeCafes(),
                selectedFilter,
            ) { events, cafes, filter ->
                NewsUIState.Loaded(filter, upcomingItems(events, cafes, filter, clock.now(), timeZone)) as NewsUIState
            }
                .onStart { emit(NewsUIState.Loading) }
                .catch { emit(NewsUIState.Error("Aktuality se nepodařilo načíst.")) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewsUIState.Loading)

    override fun selectFilter(filter: NewsFilter) {
        selectedFilter.value = filter
    }

    override fun retry() {
        retryTrigger.value++
    }

    companion object {
        /**
         * Events from today on (whole day, in [timeZone]) matching [filter], sorted by start.
         * An event that already started today is still shown.
         */
        fun upcomingItems(
            events: List<CafeEvent>,
            cafes: List<Cafe>,
            filter: NewsFilter,
            now: Instant,
            timeZone: TimeZone,
        ): List<NewsEventItem> {
            val today = now.toLocalDateTime(timeZone).date
            val cafeNames = cafes.associate { it.id to it.shortName }
            return events
                .filter { filter.matches(it.category) }
                .filter { it.startsAt.toLocalDateTime(timeZone).date >= today }
                .sortedBy { it.startsAt }
                .map { event ->
                    NewsEventItem(
                        event = event,
                        dateTexts = EventDateFormat.format(event.startsAt, timeZone),
                        cafeName = cafeNames[event.cafeId].orEmpty(),
                    )
                }
        }
    }
}
