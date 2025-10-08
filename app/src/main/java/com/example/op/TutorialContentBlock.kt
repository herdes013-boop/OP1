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
     * ✅ OPRAVA: Univerzálny blok pre obrázky.
     * Dokáže pracovať s obrázkami z galérie (uriString) aj z interných zdrojov (imageRes).
     * @param uriString Textová reprezentácia URI adresy obrázku (pre obrázky z galérie).
     * @param imageRes ID obrázku z 'drawable' zdrojov (pre predvolené návody).
     */
    data class ImageBlock(
        val uriString: String? = null,
        @DrawableRes val imageRes: Int? = null
    ) : TutorialContentBlock()
}
