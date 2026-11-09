package cz.tp.usousedu.data.model

/** The user's stamp card (Firestore loyaltyCards/{uid}); one card for the whole network. */
data class LoyaltyCard(
    val stamps: Int,
    val stampsGoal: Int,
) {
    companion object {
        const val DEFAULT_GOAL = 10

        /** Card of a user who has no stamps yet (no document in Firestore). */
        val EMPTY = LoyaltyCard(stamps = 0, stampsGoal = DEFAULT_GOAL)
    }
}

/** A reward that can be claimed for stamps (Firestore rewards/{id}). */
data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val requiredStamps: Int,
    val order: Int = 0,
) {
    fun isAvailableFor(card: LoyaltyCard): Boolean = card.stamps >= requiredStamps
}
