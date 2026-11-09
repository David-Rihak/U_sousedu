package cz.tp.usousedu.ui.screens.cafes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.ui.components.StripedPhotoPlaceholder

// Same palette as Menu and Aktuality.
internal object CafesColors {
    val background = Color(0xFFFFF4E6)
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val accent = Color(0xFFE2572B)
    val white = Color.White
    val buttonTrack = Color(0xFFF6EADB)
    val badgeBackground = Color(0xFFDDEFE3)
    val badgeText = Color(0xFF177453)
}

@Composable
internal fun CafeCard(
    cafe: Cafe,
    isFavorite: Boolean,
    isSaving: Boolean,
    onSetFavorite: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(19.dp),
        color = CafesColors.white,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            StripedPhotoPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(15.dp)),
                label = "foto kavárny",
            )

            Column(
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 12.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = cafe.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafesColors.text,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isFavorite) FavoriteBadge()
                }
                Text(
                    text = cafe.address,
                    fontSize = 13.sp,
                    color = CafesColors.mutedText,
                )

                if (cafe.openingHours.isNotEmpty()) {
                    Text(
                        text = "Otevírací doba",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CafesColors.mutedText,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    cafe.openingHours.forEach { line ->
                        Text(text = line, fontSize = 13.sp, color = CafesColors.text)
                    }
                }

                if (!isFavorite) {
                    SetFavoriteButton(
                        isSaving = isSaving,
                        onClick = onSetFavorite,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CafesColors.badgeBackground)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = "Moje kavárna",
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CafesColors.badgeText,
        )
    }
}

@Composable
private fun SetFavoriteButton(
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(CafesColors.buttonTrack)
            .clickable(enabled = !isSaving, role = Role.Button, onClick = onClick)
            .defaultMinSize(minHeight = 44.dp)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isSaving) "Ukládám..." else "Nastavit jako moji kavárnu",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CafesColors.accent,
        )
    }
}

@Composable
internal fun EmptyCafesMessage() {
    Surface(color = CafesColors.white, shape = RoundedCornerShape(18.dp)) {
        Text(
            text = "Zatím tu nejsou žádné kavárny.",
            color = CafesColors.mutedText,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
        )
    }
}
