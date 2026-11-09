package cz.tp.usousedu.data.customers

import cz.tp.usousedu.data.firestore.FirestoreCollections
import cz.tp.usousedu.data.firestore.UserDto
import cz.tp.usousedu.data.model.Customer
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

interface ICustomerRepository {
    /** Users whose e-mail starts with [emailPrefix] (expected lowercase), sorted by e-mail. Staff only. */
    suspend fun searchByEmail(emailPrefix: String): List<Customer>
}

class FirestoreCustomerRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : ICustomerRepository {

    // Firestore has no "contains" search; a prefix range on one field uses the automatic index.
    // Users without an e-mail in users/{uid} (accounts created only in the Console) are not found.
    override suspend fun searchByEmail(emailPrefix: String): List<Customer> =
        firestore.collection(FirestoreCollections.USERS)
            .orderBy(FIELD_EMAIL)
            .startAt(emailPrefix)
            .endAt(emailPrefix + PREFIX_END)
            .limit(MAX_RESULTS)
            .get()
            .documents
            .mapNotNull { doc ->
                val dto = runCatching { doc.data<UserDto>() }.getOrNull() ?: return@mapNotNull null
                dto.email.takeIf { it.isNotBlank() }?.let { Customer(id = doc.id, email = it) }
            }

    private companion object {
        const val FIELD_EMAIL = "email"
        const val PREFIX_END = ""
        const val MAX_RESULTS = 20
    }
}

/** In-memory customers, for previews and tests. */
class FakeCustomerRepository(
    private val customers: List<Customer> = listOf(
        Customer(id = "customer-1", email = "test@test.com"),
        Customer(id = "customer-2", email = "jana.novakova@example.com"),
    ),
) : ICustomerRepository {
    override suspend fun searchByEmail(emailPrefix: String): List<Customer> =
        customers.filter { it.email.startsWith(emailPrefix) }.sortedBy { it.email }
}
