package com.example.op


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
     * ✅ ZMENA: Blok teraz obsahuje URI adresu obrázku ako textový reťazec (String).
     * @param uriString Textová reprezentácia URI adresy obrázku.
     */
    data class ImageBlock(
        val uriString: String
    ) : TutorialContentBlock()
}
