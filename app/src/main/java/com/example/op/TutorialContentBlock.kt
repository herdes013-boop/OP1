package com.example.op

import androidx.annotation.DrawableRes
import java.util.UUID

/**
 * Uzavretá (sealed) trieda, ktorá reprezentuje jeden "blok" obsahu v návode.
 * Každý blok má unikátne ID, aby sme s ním mohli pracovať v zozname.
 */
sealed class TutorialContentBlock(val id: String = UUID.randomUUID().toString()) {

    /**
     * Blok, ktorý obsahuje textový reťazec.
     * @param text Samotný text.
     */
    data class TextBlock(
        var text: String = ""
    ) : TutorialContentBlock()

    /**
     * Blok, ktorý obsahuje referenciu na obrázok z 'drawable' zdrojov.
     * @param imageRes ID obrázku.
     */
    data class ImageBlock(
        @DrawableRes var imageRes: Int? = null
    ) : TutorialContentBlock()
}
