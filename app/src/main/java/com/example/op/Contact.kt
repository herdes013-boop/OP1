package com.example.op

/**
 * Dátová trieda reprezentujúca jeden kontakt.
 * @property id Firestore ID dokumentu.
 * @property name Meno kontaktu.
 * @property email Email kontaktu.
 */
data class Contact(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)
