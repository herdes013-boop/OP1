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
)

/**
 * Dátová trieda reprezentujúca položku s IP adresou.
 * @param id Unikátny identifikátor.
 * @param name Názov alebo popis zariadenia/služby.
 * @param ipAddress Samotná IP adresa v textovom formáte.
 */
data class IpItem(
    val id: String,
    val name: String,
    val ipAddress: String
)
