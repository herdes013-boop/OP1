package com.example.op

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // STAV PRE DETAIL OBRAZOVKU
    private val _selectedContact = MutableStateFlow<ContactItem?>(null)
    val selectedContact = _selectedContact.asStateFlow()

    fun loadContactDetail(contactId: Int) {
        viewModelScope.launch {
            _selectedContact.value = contacts.firstOrNull { it.id == contactId }
        }
    }

    fun clearSelectedContact() {
        _selectedContact.value = null
    }

    // 2. FILTRE A STAVY
    private val availableChannels = mutableStateListOf("Jednotka", "Dvojka", "Sport", DEFAULT_CHANNEL)
    val channelOptions: List<String> by derivedStateOf {
        listOf(ALL_CHANNELS_FILTER) + availableChannels
    }
    private val _selectedTabFilter = MutableStateFlow(ALL_CHANNELS_FILTER)
    val selectedTabFilter = _selectedTabFilter.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    val displayedContacts: List<ContactItem> by derivedStateOf {
        val currentFilter = selectedTabFilter.value // ✅ PRIDALI SME .value
        val currentQuery = searchQuery.trim().lowercase()
        if (currentFilter != ALL_CHANNELS_FILTER) { // Teraz už porovnávame String so Stringom
            return@derivedStateOf emptyList()
        }
        contacts.filter { contact ->
            val matchesQuery = if (currentQuery.isBlank()) true else
                contact.getFullName().lowercase().contains(currentQuery) ||
                        contact.function.orEmpty().lowercase().contains(currentQuery)
            matchesQuery
        }
    }

    // =====================================================================
    //          ✅ ZAČIATOK SEKCIE PRE DÁTA KANÁLOV ✅
    // =====================================================================

    // Nový stav pre dáta kanála
    private val _channelFunctions = MutableStateFlow<List<ChannelFunction>>(emptyList())
    val channelFunctions = _channelFunctions.asStateFlow()

    var isEditMode by mutableStateOf(false)
        private set

    fun toggleEditMode() {
        isEditMode = !isEditMode
    }

    /**
     * Pridá novú funkciu do zoznamu.
     * Ako názov použije dodaný text.
     */
    fun addChannelFunction(title: String) {
        // Vytvoríme novú funkciu s unikátnym ID a zadaným názvom
        val newFunction = ChannelFunction(
            id = "func_${System.currentTimeMillis()}", // ID zaručí, že každá funkcia je jedinečná
            title = title,
            assignedPeople = emptyList() // Začína bez priradených osôb
        )

        // K existujúcemu zoznamu funkcií pridáme túto novú
        _channelFunctions.value = _channelFunctions.value + newFunction
    }

    /**
     * Zmaže funkciu zo zoznamu podľa jej ID.
     */
    fun removeChannelFunction(functionId: String) {
        // Vytvoríme nový zoznam, v ktorom budú všetky funkcie OKREM tej, ktorú chceme zmazať
        _channelFunctions.value = _channelFunctions.value.filterNot { it.id == functionId }
    }
    fun moveChannelFunction(from: Int, to: Int) {
        // Kontrola, či sú indexy platné pre aktuálny zoznam
        val currentFunctions = _channelFunctions.value
        if (from < 0 || to < 0 || from >= currentFunctions.size || to >= currentFunctions.size) {
            return
        }

        _channelFunctions.update {
            it.toMutableList().apply {
                val item = removeAt(from)
                add(to, item)
            }
        }
        // Poznámka: Toto zatiaľ mení poradie iba v pamäti.
    }

    // Načítanie dát pre zvolený kanál
    private fun loadChannelFunctions(channelName: String) {
        // Z našej mapy ukážkových dát vyberieme tie správne
        // Ak pre daný kanál dáta nemáme, výsledok bude prázdny zoznam
        val sampleData = createSampleChannelData(contacts)
        _channelFunctions.value = sampleData[channelName] ?: emptyList()
    }

    // Funkcia na vytvorenie dočasných dát. Používa reálne kontakty zo zoznamu.
    private fun createSampleChannelData(allContacts: List<ContactItem>): Map<String, List<ChannelFunction>> {
        val contact1 = allContacts.getOrNull(0)
        val contact2 = allContacts.getOrNull(1)
        val contact3 = allContacts.getOrNull(2)

        if (contact1 == null || contact2 == null || contact3 == null) return emptyMap()

        return mapOf(
            "Jednotka" to listOf(
                ChannelFunction(
                    title = "Kameramani",
                    assignedPeople = listOf(
                        AssignedPerson(contactId = contact1.id.toString(), name = contact1.getFullName(), phone = contact1.phone, notes = "Hlavná kamera"),
                        AssignedPerson(contactId = contact2.id.toString(), name = contact2.getFullName(), phone = contact2.phone, notes = "Ručná kamera")
                    )
                ),
                ChannelFunction(
                    title = "Zvukári",
                    assignedPeople = listOf(
                        AssignedPerson(contactId = contact3.id.toString(), name = contact3.getFullName(), phone = contact3.phone, notes = "")
                    )
                ),
                ChannelFunction(title = "Strihači", assignedPeople = emptyList())
            ),
            "24" to listOf(
                ChannelFunction(
                    title = "Redaktori",
                    assignedPeople = listOf(
                        AssignedPerson(contactId = contact2.id.toString(), name = contact2.getFullName(), phone = contact2.phone, notes = "Ranná zmena")
                    )
                ),
                ChannelFunction(title = "Vydavatelia", assignedPeople = emptyList())
            )
        )
    }

    // =====================================================================
    //          ✅ KONIEC SEKCIE PRE DÁTA KANÁLOV ✅
    // =====================================================================


    // 3. AKCIE PRE FORMULÁR
    var formId by mutableStateOf(0)
    var formFirstName by mutableStateOf("")
    var formLastName by mutableStateOf("")
    var formChannel by mutableStateOf(DEFAULT_CHANNEL)
    var formFunction by mutableStateOf("")
    var formPhone by mutableStateOf("")
    var formEmail by mutableStateOf("")
    var formNotes by mutableStateOf("")

    // 4. FUNKCIE NA AKTUALIZÁCIU STAVU
    fun updateFirstName(newValue: String) { formFirstName = newValue }
    fun updateLastName(newValue: String) { formLastName = newValue }
    fun updateFunction(newValue: String) { formFunction = newValue }
    fun updatePhone(newValue: String) { formPhone = newValue }
    fun updateEmail(newValue: String) { formEmail = newValue }
    fun updateNotes(newValue: String) { formNotes = newValue }
    fun updateChannel(newValue: String) { formChannel = newValue }

    // 5. OBSLUHA ULOŽENIA KONTAKTOV
    fun resetForm() {
        formId = 0
        formFirstName = ""
        formLastName = ""
        formChannel = availableChannels.firstOrNull() ?: DEFAULT_CHANNEL
        formFunction = ""
        formPhone = ""
        formEmail = ""
        formNotes = ""
    }

    fun saveNewContact() {
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
        if (_selectedContact.value?.id == contact.id) {
            clearSelectedContact()
        }
    }

    // Funkcie pre aktualizáciu filtrov z ContactsScreen
    fun updateSelectedTabFilter(newFilter: String) {
        _selectedTabFilter.value = newFilter // Použijeme privátnu premennú s podčiarkovníkom

        // Logika pre načítanie dát pre záložku "Jednotka"
        if (newFilter == "Jednotka") {
            val stv1Functions = listOf(
                ChannelFunction(id = "func1", title = "Intendant", assignedPeople = emptyList()),
                ChannelFunction(id = "func2", title = "Programové zmeny v PROVYSe", assignedPeople = emptyList()),
                ChannelFunction(id = "func3", title = "Denné vysielacie plány 1", assignedPeople = emptyList()),
                ChannelFunction(id = "func4", title = "Denné vysielacie plány 2", assignedPeople = emptyList())
            )
            // Aktualizujeme stav, ktorý sa zobrazuje na obrazovke
            _channelFunctions.value = stv1Functions // Použijeme privátnu premennú s podčiarkovníkom
        } else {
            // Pre ostatné záložky zatiaľ necháme zoznam prázdny
            _channelFunctions.value = emptyList() // Použijeme privátnu premennú s podčiarkovníkom
        }
    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
    }

    // Metódy pre správu kanálov
    fun addChannel(channel: String) {
        val trimmedChannel = channel.trim()
        if (trimmedChannel.isNotBlank() && !availableChannels.contains(trimmedChannel)) {
            availableChannels.add(trimmedChannel)
        }
    }

    fun removeChannel(channel: String) {
        if (channel == ALL_CHANNELS_FILTER || channel == DEFAULT_CHANNEL) return
        availableChannels.remove(channel)
        val contactsToUpdate = contacts.filter { it.channel == channel }
        contactsToUpdate.forEach { contact ->
            val index = contacts.indexOf(contact)
            contacts[index] = contact.copy(channel = DEFAULT_CHANNEL)
        }
        if (selectedTabFilter.value == channel) {
            updateSelectedTabFilter(ALL_CHANNELS_FILTER) // Použijeme našu upravenú funkciu
        }
    }
}
