package cz.tp.usousedu.data.loyalty

import cz.tp.usousedu.data.firestore.FirestoreCollections
import cz.tp.usousedu.data.firestore.LoyaltyCardDto
import cz.tp.usousedu.data.firestore.LoyaltyHistoryDto
import cz.tp.usousedu.data.firestore.RewardDto
import cz.tp.usousedu.data.model.LoyaltyCard
import cz.tp.usousedu.data.model.Reward
import cz.tp.usousedu.data.model.withStampChange
import kotlinx.coroutines.CancellationException
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

    /**
     * Staff only: adds [delta] stamps (negative = removes) to the card of [customerId] and logs it
     * to its history, both in one transaction. Fails with [StampLimitException] when the result
     * would leave 0..stampsGoal.
     */
    suspend fun changeStamps(customerId: String, delta: Int, staffId: String): Result<LoyaltyCard>
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

    override suspend fun changeStamps(customerId: String, delta: Int, staffId: String): Result<LoyaltyCard> = try {
        val cardRef = firestore.collection(FirestoreCollections.LOYALTY_CARDS).document(customerId)
        // Auto-ID created up front; the transaction may run several times but writes the same entry.
        val historyRef = cardRef.collection(FirestoreCollections.HISTORY).document
        val updated = firestore.runTransaction {
            // A missing card is created (EMPTY = 0 stamps, default goal).
            val card = get(cardRef).toCard().withStampChange(delta) ?: throw StampLimitException()
            set(cardRef, LoyaltyCardDto.serializer(), LoyaltyCardDto(stamps = card.stamps, stampsGoal = card.stampsGoal))
            set(
                historyRef,
                LoyaltyHistoryDto.serializer(),
                LoyaltyHistoryDto(
                    type = if (delta >= 0) LoyaltyHistoryDto.TYPE_STAMP_ADDED else LoyaltyHistoryDto.TYPE_STAMP_REMOVED,
                    delta = delta,
                    stampsAfter = card.stamps,
                    staffId = staffId,
                ),
            )
            card
        }
        Result.success(updated)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

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

/** The card would end up below 0 or above stampsGoal. */
class StampLimitException : IllegalStateException("Stamps out of range")

/** Mock card, for previews and tests. */
class FakeLoyaltyRepository(
    private val card: LoyaltyCard = LoyaltyCatalog.card,
) : ILoyaltyRepository {
    override fun observeCard(userId: String): Flow<LoyaltyCard> = flowOf(card)

    override suspend fun changeStamps(customerId: String, delta: Int, staffId: String): Result<LoyaltyCard> =
        card.withStampChange(delta)?.let { Result.success(it) } ?: Result.failure(StampLimitException())
}

/** Mock rewards, for previews and tests. */
class FakeRewardsRepository(
    private val rewards: List<Reward> = LoyaltyCatalog.rewards,
) : IRewardsRepository {
    override fun observeRewards(): Flow<List<Reward>> = flowOf(rewards.sortedBy { it.order })
}
