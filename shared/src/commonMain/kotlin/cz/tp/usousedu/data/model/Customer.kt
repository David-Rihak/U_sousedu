package cz.tp.usousedu.data.model

/** A user found by staff (Firestore users/{uid}); [id] is the uid = ID of their loyalty card. */
data class Customer(
    val id: String,
    val email: String,
)
