package cz.tp.usousedu.data.loyalty

import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.data.model.Reward

/** Mock loyalty data: source of the Firestore seed and of the fake repositories (previews, tests). */
object LoyaltyCatalog {
    /** Same state the Karta screen showed before Firestore ("6 z 10"). */
    val card = LoyaltyCard(stamps = 6, stampsGoal = LoyaltyCard.DEFAULT_GOAL)

    val rewards: List<Reward> = listOf(
        Reward(
            id = "dessert-discount",
            name = "Sleva 20 % na dezert",
            description = "Odměnu lze uplatnit u obsluhy při další návštěvě.",
            requiredStamps = 5,
            order = 1,
        ),
        Reward(
            id = "free-coffee",
            name = "Káva zdarma",
            description = "Libovolná klasická káva z nabídky zdarma.",
            requiredStamps = 10,
            order = 2,
        ),
    )
}
