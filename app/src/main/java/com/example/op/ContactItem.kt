package com.example.op

/**
 * Dátový model pre jeden kontakt v zozname.
 * Všetky polia, ktoré nemusia byť povinné (okrem ID, Meno, Priezvisko),
 * sú definované ako nullable (String?).
 */
data class ContactItem(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val function: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val channel: String? = null,
    val notes: String? = null
) {
    /**
     * Pomocná funkcia na získanie celého mena.
     */
    fun getFullName(): String {
        return "$firstName $lastName".trim()
    }
}
