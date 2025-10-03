package com.example.op

/**
 * Enum trieda reprezentujúca dostupné komunikačné kanály pre kontakty.
 *
 * @property displayName Názov kanálu pre zobrazenie v UI.
 */
enum class CommunicationChannel(val displayName: String) {
    PHONE("Telefónne číslo"),
    EMAIL("E-mail"),
    SIGNAL("Signal"),
    TELEGRAM("Telegram"),
    WHATSAPP("WhatsApp"),
    OTHER("Iné (poznámky)")
}
