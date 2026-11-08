package cz.tp.usousedu.ui.screens.news

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Mirrors the warm palette already present in MenuComponents.kt. */
internal object NewsColors {
    val background = Color(0xFFFFF4E6)
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val accent = Color(0xFFE9542D)
    val white = Color.White
    val kidsBanner = Color(0xFFFFEABF)
    val kidsStripe = Color(0xFFF9DDD6)
    val friendsBanner = Color(0xFFFFEBC0)
    val friendsStripe = Color(0xFFD8EFE6)
}

@Composable
internal fun NewsFilterRow(
    selectedFilter: NewsFilter,
    onFilterSelected: (NewsFilter) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        NewsFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) NewsColors.text else NewsColors.white)
                    .clickable(role = Role.Tab) { onFilterSelected(filter) }
                    .semantics { selected = isSelected }
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = filter.title,
                    color = if (isSelected) NewsColors.white else NewsColors.text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun NewsEventCard(
    event: CafeEvent,
    onReminderClick: (CafeEvent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(23.dp),
        color = NewsColors.white,
    ) {
        Column {
            EventImagePlaceholder(event)
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NewsColors.text,
                )
                Text(
                    text = "${event.schedule} · ${event.cafe}",
                    fontSize = 12.sp,
                    color = NewsColors.mutedText,
                )
                Text(
                    text = event.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = NewsColors.text,
                )
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NewsColors.accent, RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) { onReminderClick(event) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Připomenout",
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NewsColors.accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventImagePlaceholder(event: CafeEvent) {
    val isApricot = event.bannerStyle == NewsBannerStyle.APRICOT
    val bannerColor = if (isApricot) NewsColors.kidsBanner else NewsColors.friendsBanner
    val stripeColor = if (isApricot) NewsColors.kidsStripe else NewsColors.friendsStripe

    Box(
        modifier = Modifier.fillMaxWidth().height(99.dp),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(color = bannerColor)
            val step = 23.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = stripeColor,
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = 11.dp.toPx(),
                )
                x += step
            }
        }

        Text(
            text = "foto akce",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = NewsColors.mutedText,
        )

        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = NewsColors.white,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text = event.day,
                    color = NewsColors.text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = event.month,
                    color = NewsColors.text,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 11.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            color = NewsColors.text,
            shape = RoundedCornerShape(100.dp),
        ) {
            Text(
                text = event.category.title,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = NewsColors.white,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
