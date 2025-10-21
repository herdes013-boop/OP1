package com.example.op

import androidx.annotation.DrawableRes
import java.util.UUID

/**
 * Dátová trieda, ktorá reprezentuje jeden návod.
 *
 * @param id Unikátny identifikátor návodu.
 * @param title Názov návodu.
 * @param category Kategória, do ktorej návod patrí.
 * @param contentBlocks Zoznam blokov (texty a obrázky), ktoré tvoria obsah návodu.
 */
data class TutorialItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categories: List<String>, // <-- TOTO JE ZMENA
    val contentBlocks: List<TutorialContentBlock>
)
