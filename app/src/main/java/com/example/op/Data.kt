package com.example.op

/**
 * Dátová trieda reprezentujúca položku s heslom. * @param id Unikátny identifikátor.
 * @param name Názov služby alebo položky (napr. "Google").
 * @param username Používateľské meno alebo email.
 * @param password Uložené heslo.
 * @param notes Voliteľné poznámky.
 * @param url Voliteľná URL adresa.  // <-- Pridaná dokumentácia
 */
data class PasswordItem(
    val id: String,
    val name: String,
    val username: String?,
    val password: String,
    val notes: String?,
    // --- ✅ PRIDAJTE TENTO RIADOK ---
    val url: String? = null
) {
    /**
     * Kontroluje, či sa položka zhoduje s vyhľadávacím dopytom.
     * Prehľadáva názov, používateľské meno, poznámky a URL. // <-- Upravená dokumentácia
     */
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            username,
            notes,
            // --- ✅ PRIDAJTE URL AJ DO VYHĽADÁVANIA ---
            url
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
 * @param notes Voliteľné poznámky.
 */
data class IpItem(
    val id: String,
    val name: String,
    val ipAddress: String,
    val notes: String? = null
) {
    /**
     * Kontroluje, či sa položka zhoduje s vyhľadávacím dopytom.
     * Prehľadáva názov, IP adresu a poznámky.
     */
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            ipAddress,
            notes
        )
        return matchingCombinations.any {
            it?.contains(query, ignoreCase = true) == true
        }
    }
}
