package com.example.op

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.ViewModel

// --------------------------------------------------
// VIEW MODEL PRE KONTAKTY
// --------------------------------------------------

class ContactsViewModel : ViewModel() {

    // Predvolená hodnota pre kanál, ak sa kanál odstráni
    private val DEFAULT_CHANNEL = "24"
    private val ALL_CHANNELS_FILTER = "Všetky"

    // 1. DÁTA: Zoznam kontaktov
    val contacts = mutableStateListOf<ContactItem>(
        ContactItem(
            id = 1, firstName = "Ján", lastName = "Novák", function = "Redaktor",
            phone = "+421900111222", email = "jan@novak.sk", channel = "Jednotka", notes = null
        ),
        ContactItem(
            id = 2, firstName = "Anna", lastName = "Malá", function = "Kamera",
            phone = "+421900333444", email = "anna@mala.sk", channel = "Dvojka", notes = "Pracuje len na nočných zmenách."
        ),
        ContactItem(
            id = 3, firstName = "Peter", lastName = "Kováč", function = "IT Podpora",
            phone = "+421900555666", email = "peter@kovac.sk", channel = DEFAULT_CHANNEL, notes = null
        ),
        ContactItem(
            id = 4, firstName = "Marek", lastName = "Varga", function = "Hlásateľ",
            phone = "+421900777888", email = "marek@varga.sk", channel = "Jednotka", notes = null
        ),
        ContactItem(
            id = 5, firstName = "Zuzana", lastName = "Nová", function = "Produkcia",
            phone = "+421900999000", email = "zuzana@nova.sk", channel = "Sport", notes = "Kontaktovať len cez email."
        )
    )

    // 2. FILTRE A STAVY

    // Zoznam dostupných kanálov okrem fixného "Všetky"
    // ❌ PÔVODNÝ KÓD
    private val availableChannels = mutableStateListOf("Jednotka", "Dvojka", "Sport", DEFAULT_CHANNEL)

    // DerivedState pre všetky možnosti (vrátane filtračnej "Všetky")
    val channelOptions: List<String> by derivedStateOf {
        listOf(ALL_CHANNELS_FILTER) + availableChannels
    }
    var selectedTabFilter by mutableStateOf(ALL_CHANNELS_FILTER)
        private set
    var searchQuery by mutableStateOf("")
        private set

    val displayedContacts: List<ContactItem> by derivedStateOf {
        val currentFilter = selectedTabFilter
        val currentQuery = searchQuery.trim().lowercase()

        // ----------------------------------------------------------------------
        // ✅ NOVÁ LOGIKA FILTROVANIA:
        // 1. Iba záložka "Všetky" (ALL_CHANNELS_FILTER) umožňuje zobraziť kontakty.
        // 2. Akákoľvek iná záložka vráti prázdny zoznam (zaistí to 'matchesTab').
        // ----------------------------------------------------------------------
        val matchesTab = currentFilter == ALL_CHANNELS_FILTER

        if (!matchesTab) {
            return@derivedStateOf emptyList() // Ak nie je vybrané "Všetky", vrátime prázdny zoznam
        }

        // ----------------------------------------------------------------------

        contacts.filter { contact ->
            // Ak je vybrané "Všetky", kontakty sa filtrujú len podľa vyhľadávania (matchesQuery)
            val matchesQuery = if (currentQuery.isBlank()) true else
                contact.getFullName().lowercase().contains(currentQuery) ||
                        contact.function.orEmpty().lowercase().contains(currentQuery)

            matchesQuery
        }
    }

    // 3. AKCIE PRE FORMULÁR (Stavové premenné pre AddContactScreen/EditContactScreen)

    var formId by mutableStateOf(0)
    var formFirstName by mutableStateOf("")
    var formLastName by mutableStateOf("")
    // Nastavíme predvolený kanál na DEFAULT_CHANNEL
    var formChannel by mutableStateOf(DEFAULT_CHANNEL)
    var formFunction by mutableStateOf("")
    var formPhone by mutableStateOf("")
    var formEmail by mutableStateOf("")
    var formNotes by mutableStateOf("")

    // 4. FUNKCIE NA AKTUALIZÁCIU STAVU (Settery)

    fun updateFirstName(newValue: String) { formFirstName = newValue }
    fun updateLastName(newValue: String) { formLastName = newValue }
    fun updateFunction(newValue: String) { formFunction = newValue }
    fun updatePhone(newValue: String) { formPhone = newValue }
    fun updateEmail(newValue: String) { formEmail = newValue }
    fun updateNotes(newValue: String) { formNotes = newValue }
    fun updateChannel(newValue: String) { formChannel = newValue }

    // 5. OBSLUHA ULOŽENIA KONTAKTOV

    /**
     * Resetuje stav formulára.
     */
    fun resetForm() {
        formId = 0
        formFirstName = ""
        formLastName = ""
        formChannel = availableChannels.firstOrNull() ?: DEFAULT_CHANNEL // Bezpečný reset
        formFunction = ""
        formPhone = ""
        formEmail = ""
        formNotes = ""
    }

    /**
     * Uloží nový kontakt (používa form state).
     */
    fun saveNewContact() {
        // ... (Logika zostáva rovnaká)
        val newContactItem = ContactItem(
            id = contacts.maxOfOrNull { it.id }?.plus(1) ?: 1,
            firstName = formFirstName.trim(),
            lastName = formLastName.trim(),
            function = formFunction.trim().ifBlank { null },
            phone = formPhone.trim().ifBlank { null },
            email = formEmail.trim().ifBlank { null },
            channel = formChannel,
            notes = formNotes.trim().ifBlank { null }
        )
        contacts.add(newContactItem)
        resetForm()
    }

    // Metódy pre EditContactScreen
    fun getContactById(id: Int): ContactItem? {
        return contacts.find { it.id == id }
    }

    fun updateContact(contact: ContactItem) {
        val index = contacts.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            contacts[index] = contact
        }
    }

    fun removeContact(contact: ContactItem) {
        contacts.remove(contact)
    }

    // Funkcie pre aktualizáciu filtrov z ContactsScreen
    fun updateSelectedTabFilter(newFilter: String) {
        selectedTabFilter = newFilter
    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
    }

    // Metódy pre správu kanálov

    /**
     * Pridá nový kanál do zoznamu, ak už neexistuje.
     */
    fun addChannel(channel: String) {
        val trimmedChannel = channel.trim()
        if (trimmedChannel.isNotBlank() && !availableChannels.contains(trimmedChannel)) {
            availableChannels.add(trimmedChannel)
        }
    }

    /**
     * Odstráni kanál a aktualizuje všetky kontakty, ktoré ho používali, na predvolený kanál.
     */
    fun removeChannel(channel: String) {
        if (channel == ALL_CHANNELS_FILTER || channel == DEFAULT_CHANNEL) {
            // Predvolený kanál a kanál filtra nemôžu byť odstránené
            return
        }

        // 1. Odstránime kanál zo zoznamu možností
        availableChannels.remove(channel)

        // 2. Aktualizujeme kontakty, ktoré používali odstránený kanál
        val contactsToUpdate = contacts.filter { it.channel == channel }
        contactsToUpdate.forEach { contact ->
            val index = contacts.indexOf(contact)
            // Vytvoríme kópiu s novým kanálom (DEFAULT_CHANNEL)
            contacts[index] = contact.copy(channel = DEFAULT_CHANNEL)
        }

        // 3. Ak bol odstránený kanál vybraný ako filter, resetujeme filter
        if (selectedTabFilter == channel) {
            selectedTabFilter = ALL_CHANNELS_FILTER
        }
    }
}
