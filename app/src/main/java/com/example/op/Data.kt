package com.example.op

/**
 * Dátová trieda reprezentujúca položku s heslom.
 * @param id Unikátny identifikátor.
 * @param name Názov služby alebo položky (napr. "Google").
 * @param username Používateľské meno alebo email.
 * @param password Uložené heslo.
 * @param notes Voliteľné poznámky.
 */
data class PasswordItem(
    val id: String,
    val name: String,
    val username: String?,
    val password: String,
    val notes: String?
) {
    /**
     * Kontroluje, či sa položka zhoduje s vyhľadávacím dopytom.
     * Prehľadáva názov, používateľské meno a poznámky.
     */
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            username,
            notes
        )
        return matchingCombinations.any {
            it?.contains(query, ignoreCase = true) == true
        }
    }
}

/**
 * Dátová trieda reprezentujúca položku s IP adresou.
 * @param id Unikátny identifikátor.
 * @param name Názov alebo popis zariadenia/služby.
 * @param ipAddress Samotná IP adresa v textovom formáte.
 */
data class IpItem(
    val id: String,
    val name: String,
    val ipAddress: String,
    val notes: String? = null // <-- TOTO STE PRIDALI
) {
    /**
     * Kontroluje, či sa položka zhoduje s vyhľadávacím dopytom.
     * Prehľadáva názov a IP adresu.
     */
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            ipAddress,
            notes
        )
        return matchingCombinations.any {
            // Použijeme bezpečné volanie `?.` ako pri heslách
            it?.contains(query, ignoreCase = true) == true
        }
    }
}
