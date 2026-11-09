package cz.tp.usousedu.data.loyalty

import cz.tp.usousedu.data.firestore.FirestoreCollections
import cz.tp.usousedu.data.firestore.LoyaltyCardDto
import cz.tp.usousedu.data.firestore.RewardDto
import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.data.model.Reward
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface ILoyaltyRepository {
    /** The user's card; [LoyaltyCard.EMPTY] while the user has no card document yet. */
    fun observeCard(userId: String): Flow<LoyaltyCard>
}

interface IRewardsRepository {
    /** Active rewards sorted by `order`. */
    fun observeRewards(): Flow<List<Reward>>
}

class FirestoreLoyaltyRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : ILoyaltyRepository {

    override fun observeCard(userId: String): Flow<LoyaltyCard> =
        firestore.collection(FirestoreCollections.LOYALTY_CARDS).document(userId).snapshots
            .map { it.toCard() }

    private fun DocumentSnapshot.toCard(): LoyaltyCard {
        if (!exists) return LoyaltyCard.EMPTY
        val dto = runCatching { data<LoyaltyCardDto>() }.getOrNull() ?: return LoyaltyCard.EMPTY
        val goal = dto.stampsGoal.takeIf { it > 0 } ?: LoyaltyCard.DEFAULT_GOAL
        return LoyaltyCard(stamps = dto.stamps.coerceAtLeast(0), stampsGoal = goal)
    }
}

class FirestoreRewardsRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : IRewardsRepository {

    // Filtered and sorted in the app: a where + orderBy on different fields would need an index.
    override fun observeRewards(): Flow<List<Reward>> =
        firestore.collection(FirestoreCollections.REWARDS).snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { it.toReward() }.sortedBy { it.order }
        }

    private fun DocumentSnapshot.toReward(): Reward? {
        val dto = runCatching { data<RewardDto>() }.getOrNull() ?: return null
        if (!dto.isActive || dto.name.isBlank()) return null
        return Reward(
            id = id,
            name = dto.name,
            description = dto.description,
            requiredStamps = dto.requiredStamps,
            order = dto.order,
        )
    }
}

/** Mock card, for previews and tests. */
class FakeLoyaltyRepository(
    private val card: LoyaltyCard = LoyaltyCatalog.card,
) : ILoyaltyRepository {
    override fun observeCard(userId: String): Flow<LoyaltyCard> = flowOf(card)
}

/** Mock rewards, for previews and tests. */
class FakeRewardsRepository(
    private val rewards: List<Reward> = LoyaltyCatalog.rewards,
) : IRewardsRepository {
    override fun observeRewards(): Flow<List<Reward>> = flowOf(rewards.sortedBy { it.order })
}
