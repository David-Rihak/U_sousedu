package cz.tp.usousedu.data.firestore

import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

// Shapes of the Firestore documents, see C:\Work\TP\datovy_model.txt.
// Enums are stored by name (e.g. "COFFEE"); IDs of other documents are plain strings.
// Defaults keep decoding working when a field is missing in the document.

object FirestoreCollections {
    const val USERS = "users"
    const val CAFES = "cafes"
    const val PRODUCTS = "products"
    const val EVENTS = "events"
    const val REWARDS = "rewards"
    const val LOYALTY_CARDS = "loyaltyCards"

    /** Subcollection loyaltyCards/{uid}/history. */
    const val HISTORY = "history"
}

/** users/{uid} – only the fields the staff search needs. */
@Serializable
data class UserDto(
    val email: String = "",
    val role: String = "CUSTOMER",
)

/** cafes/{cafeId} */
@Serializable
data class CafeDto(
    val name: String = "",
    val shortName: String = "",
    val address: String = "",
    val openingHours: List<String> = emptyList(),
    val order: Int = 0,
    val isActive: Boolean = true,
)

/** products/{productId} */
@Serializable
data class ProductDto(
    val name: String = "",
    val description: String = "",
    val priceCzk: Int = 0,
    val category: String = "",
    val availableAt: List<String> = emptyList(),
    val isPopular: Boolean = false,
    val order: Int = 0,
    val isActive: Boolean = true,
)

/** events/{eventId} */
@Serializable
data class EventDto(
    val title: String = "",
    val description: String = "",
    val startsAt: Timestamp = Timestamp(0, 0),
    val cafeId: String = "",
    val category: String = "",
    val bannerStyle: String = "",
    val isPublished: Boolean = true,
)

/** rewards/{rewardId} */
@Serializable
data class RewardDto(
    val name: String = "",
    val description: String = "",
    val requiredStamps: Int = 0,
    val order: Int = 0,
    val isActive: Boolean = true,
)

/** loyaltyCards/{uid} */
@Serializable
data class LoyaltyCardDto(
    val stamps: Int = 0,
    val stampsGoal: Int = 10,
    val updatedAt: BaseTimestamp = Timestamp.ServerTimestamp,
)

/** loyaltyCards/{uid}/history/{autoId} – one staff change of the card (append-only audit log). */
@Serializable
data class LoyaltyHistoryDto(
    /** "STAMP_ADDED" or "STAMP_REMOVED" (later also "REWARD_REDEEMED"). */
    val type: String = "",
    /** Change of stamps, e.g. +1 / -1. */
    val delta: Int = 0,
    val stampsAfter: Int = 0,
    /** uid of the staff member; the rules require it to be the signed-in user. */
    val staffId: String = "",
    val createdAt: BaseTimestamp = Timestamp.ServerTimestamp,
) {
    companion object {
        const val TYPE_STAMP_ADDED = "STAMP_ADDED"
        const val TYPE_STAMP_REMOVED = "STAMP_REMOVED"
    }
}
