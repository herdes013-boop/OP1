package com.example.op

import java.util.UUID

/**
 * Reprezentuje jednu osobu priradenú ku konkrétnej funkcii v kanáli.
 *
 * @param id Unikátne ID tohto priradenia.
 * @param contactId Odkaz na ID kontaktu z hlavného zoznamu (z ContactItem). Toto je kľúčové pre prepojenie.
 * @param name Meno kontaktu (duplikujeme ho tu pre jednoduchšie zobrazenie).
 * @param phone Telefónne číslo (tiež pre jednoduchšie zobrazenie).
 * @param notes Poznámky špecifické pre túto osobu v rámci tejto funkcie.
 */
data class AssignedPerson(
    val id: String = UUID.randomUUID().toString(),
    val contactId: String,
    val name: String,
    val phone: String?,
    var notes: String = ""
)

/**
 * Reprezentuje jednu funkciu v rámci kanála (napr. "Kameramani").
 *
 * @param id Unikátne ID funkcie.
 * @param title Názov funkcie (napr. "Kameramani", "Zvukári").
 * @param assignedPeople Zoznam osôb, ktoré sú priradené k tejto funkcii.
 */
data class ChannelFunction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val assignedPeople: List<AssignedPerson>
)
