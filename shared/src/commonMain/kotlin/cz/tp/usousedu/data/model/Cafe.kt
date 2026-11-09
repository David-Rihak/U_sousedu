package cz.tp.usousedu.data.model

/** One café of the network (Firestore cafes/{id}). */
data class Cafe(
    val id: String,
    /** Full name, e.g. "U sousedů – Náměstí". */
    val name: String,
    /** Short name for chips and labels, e.g. "Náměstí". */
    val shortName: String,
    val address: String,
    val openingHours: List<String>,
    val order: Int,
)
