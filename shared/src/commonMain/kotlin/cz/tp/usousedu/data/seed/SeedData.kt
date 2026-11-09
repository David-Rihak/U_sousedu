package cz.tp.usousedu.data.seed

import cz.tp.usousedu.data.firestore.CafeDto
import cz.tp.usousedu.data.firestore.EventDto
import cz.tp.usousedu.data.firestore.LoyaltyCardDto
import cz.tp.usousedu.data.firestore.ProductDto
import cz.tp.usousedu.data.firestore.RewardDto
import cz.tp.usousedu.data.loyalty.LoyaltyCatalog
import cz.tp.usousedu.data.menu.MenuCatalog
import cz.tp.usousedu.data.news.NewsCatalog
import dev.gitlive.firebase.firestore.Timestamp
import kotlin.time.ExperimentalTime

/**
 * Test content for Firestore, keyed by document ID (see C:\Work\TP\datovy_model.txt).
 * Products and rewards come from the mock catalogs, so the data lives in one place.
 * Addresses and opening hours are placeholders.
 */
internal object SeedData {

    const val NAMESTI = "namesti"
    const val U_PARKU = "u-parku"
    const val NADRAZI = "nadrazi"

    val cafes: Map<String, CafeDto> = mapOf(
        NAMESTI to CafeDto(
            name = "U sousedů – Náměstí",
            shortName = "Náměstí",
            address = "Náměstí 12, Praha",
            openingHours = listOf("Po–Pá 7:00–19:00", "So–Ne 8:00–18:00"),
            order = 1,
        ),
        U_PARKU to CafeDto(
            name = "U sousedů – U parku",
            shortName = "U parku",
            address = "Parková 5, Praha",
            openingHours = listOf("Po–Ne 8:00–20:00"),
            order = 2,
        ),
        NADRAZI to CafeDto(
            name = "U sousedů – Nádraží",
            shortName = "Nádraží",
            address = "Nádražní 30, Praha",
            openingHours = listOf("Po–Pá 6:30–19:00", "So–Ne 8:00–16:00"),
            order = 3,
        ),
    )

    val products: Map<String, ProductDto> = MenuCatalog.products.associate { product ->
        product.id to ProductDto(
            name = product.name,
            description = product.description,
            priceCzk = product.priceCzk,
            category = product.category.name,
            // Stable order (by café order), so re-seeding gives identical documents.
            availableAt = listOf(NAMESTI, U_PARKU, NADRAZI).filter { it in product.availableAt },
            isPopular = product.isPopular,
            order = product.order,
        )
    }

    @OptIn(ExperimentalTime::class)
    val events: Map<String, EventDto> = NewsCatalog.events.associate { event ->
        event.id to EventDto(
            title = event.title,
            description = event.description,
            startsAt = Timestamp(event.startsAt.epochSeconds, event.startsAt.nanosecondsOfSecond),
            cafeId = event.cafeId,
            category = event.category.name,
            bannerStyle = event.bannerStyle.name,
        )
    }

    val rewards: Map<String, RewardDto> = LoyaltyCatalog.rewards.associate { reward ->
        reward.id to RewardDto(
            name = reward.name,
            description = reward.description,
            requiredStamps = reward.requiredStamps,
            order = reward.order,
        )
    }

    /** Card for the signed-in user, same state as the mock ("6 z 10"). */
    val loyaltyCard = LoyaltyCardDto(
        stamps = LoyaltyCatalog.card.stamps,
        stampsGoal = LoyaltyCatalog.card.stampsGoal,
    )
}
