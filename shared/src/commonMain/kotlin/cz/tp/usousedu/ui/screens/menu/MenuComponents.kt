package cz.tp.usousedu.ui.screens.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.tp.usousedu.data.model.Cafe
import cz.tp.usousedu.data.model.MenuCategory
import cz.tp.usousedu.data.model.Product

internal object MenuColors {
    val background = Color(0xFFFFF4E6)
    val text = Color(0xFF3A2419)
    val mutedText = Color(0xFF826451)
    val activeNavigation = Color(0xFFE2572B)
    val white = Color.White
    val selectorTrack = Color(0xFFF6EADB)
    val imageBackground = Color(0xFFFFEBC2)
    val imageStripe = Color(0xFFFCE0D2)
    val badgeBackground = Color(0xFFDDEFE3)
    val badgeText = Color(0xFF177453)
}

@Composable
internal fun CafeSelector(
    cafes: List<Cafe>,
    selectedCafeId: String?,
    onCafeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        cafes.forEach { cafe ->
            val isSelected = selectedCafeId == cafe.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) MenuColors.text else MenuColors.white)
                    .clickable(role = Role.Tab) { onCafeSelected(cafe.id) }
                    .semantics { selected = isSelected }
                    .defaultMinSize(minHeight = 44.dp)
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = cafe.shortName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MenuColors.white else MenuColors.text,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun CategorySelector(
    selectedCategory: MenuCategory,
    onCategorySelected: (MenuCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MenuColors.selectorTrack)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        MenuCategory.entries.forEach { category ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MenuColors.white else Color.Transparent)
                    .clickable(role = Role.Tab) { onCategorySelected(category) }
                    .semantics { selected = isSelected }
                    .defaultMinSize(minHeight = 44.dp)
                    .padding(horizontal = 2.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = category.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MenuColors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun MenuProductCard(product: Product) {
    Surface(
        shape = RoundedCornerShape(19.dp),
        color = MenuColors.white,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            ProductPhotoPlaceholder()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MenuColors.text,
                        maxLines = 2,
                    )
                    if (product.isPopular) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MenuColors.badgeBackground)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "Oblíbené",
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MenuColors.badgeText,
                            )
                        }
                    }
                }
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MenuColors.mutedText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = "${product.priceCzk} Kč",
                modifier = Modifier.widthIn(min = 43.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MenuColors.text,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ProductPhotoPlaceholder() {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(MenuColors.imageBackground),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val spacing = 21.dp.toPx()
            val stripeWidth = 10.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = MenuColors.imageStripe,
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = stripeWidth,
                )
                x += spacing
            }
        }
        Text(
            text = "foto",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MenuColors.mutedText,
        )
    }
}

@Composable
internal fun EmptyMenuMessage() {
    Surface(
        color = MenuColors.white,
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(
            text = "V této kategorii zatím nemáme žádné položky.",
            color = MenuColors.mutedText,
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            textAlign = TextAlign.Center,
        )
    }
}
