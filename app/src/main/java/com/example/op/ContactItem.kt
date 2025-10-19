package com.example.op

import java.util.UUID

/**
 * Dátový model pre jeden kontakt v zozname.
 * Všetky polia, ktoré nemusia byť povinné (okrem ID, Meno, Priezvisko),
 * sú definované ako nullable (String?).
 */
data class ContactFunction(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
data class ContactItem(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val functionIds: List<String> = emptyList(), // <-- ✅ ZMENENÉ: Zoznam IDčok funkcií
    val phone: String?,
    val email: String?,
    val channel: String,
    val notes: String?
) {
    /**
     * Pomocná funkcia na získanie celého mena.
     */
    fun getFullName(): String {
        return "$firstName $lastName".trim()
    }
}
