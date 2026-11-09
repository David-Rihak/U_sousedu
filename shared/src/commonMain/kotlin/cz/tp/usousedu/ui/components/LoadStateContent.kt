package cz.tp.usousedu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shared states for screens that load data. Colors follow the warm palette of Menu/Aktuality.
internal object LoadStateColors {
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val accent = Color(0xFFE2572B)
    val card = Color.White
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = LoadStateColors.accent)
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = LoadStateColors.card,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                color = LoadStateColors.mutedText,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(LoadStateColors.accent)
                    .clickable(role = Role.Button, onClick = onRetry)
                    .defaultMinSize(minHeight = 44.dp)
                    .padding(horizontal = 20.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Zkusit znovu",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Striped "foto" box used until real photos exist (same look as in Menu). */
@Composable
fun StripedPhotoPlaceholder(
    modifier: Modifier = Modifier,
    background: Color = Color(0xFFFFEBC2),
    stripe: Color = Color(0xFFFCE0D2),
    label: String = "foto",
) {
    Box(
        modifier = modifier.background(background),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val spacing = 21.dp.toPx()
            val stripeWidth = 10.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = stripe,
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = stripeWidth,
                )
                x += spacing
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = LoadStateColors.mutedText,
        )
    }
}
