package cz.tp.usousedu.data.seed

import cz.tp.usousedu.data.firestore.FirestoreCollections
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

/**
 * Debug-only: writes [SeedData] to Firestore in one atomic batch.
 * Documents have fixed IDs and are overwritten, so running it again creates no duplicates.
 */
class FirestoreSeeder(
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    /** Returns the number of written documents. */
    suspend fun seed(): Int {
        val batch = firestore.batch()
        var count = 0

        fun doc(collection: String, id: String) = firestore.collection(collection).document(id).also { count++ }

        SeedData.cafes.forEach { (id, cafe) -> batch.set(doc(FirestoreCollections.CAFES, id), cafe) }
        SeedData.products.forEach { (id, product) -> batch.set(doc(FirestoreCollections.PRODUCTS, id), product) }
        SeedData.events.forEach { (id, event) -> batch.set(doc(FirestoreCollections.EVENTS, id), event) }
        SeedData.rewards.forEach { (id, reward) -> batch.set(doc(FirestoreCollections.REWARDS, id), reward) }
        auth.currentUser?.let { user ->
            batch.set(doc(FirestoreCollections.LOYALTY_CARDS, user.uid), SeedData.loyaltyCard)
        }

        batch.commit()
        return count
    }
}
