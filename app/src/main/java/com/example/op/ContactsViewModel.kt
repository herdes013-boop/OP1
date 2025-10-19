package com.example.op

import androidx.compose.runtime.collectAsState
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
import java.util.UUID // Potrebný import

class ContactsViewModel : ViewModel() {

    private val DEFAULT_CHANNEL = "24"
    private val ALL_CHANNELS_FILTER = "Všetky"

    // =====================================================================
    // ✅ KROK 1: Centrálny zoznam funkcií
    // =====================================================================
    val allContactFunctions = mutableStateListOf<ContactFunction>(
        ContactFunction(id = "f1", name = "Redaktor"),
        ContactFunction(id = "f2", name = "Kamera"),
        ContactFunction(id = "f3", name = "IT Podpora"),
        ContactFunction(id = "f4", name = "Hlásateľ"),
        ContactFunction(id = "f5", name = "Produkcia"),
        ContactFunction(id = "f6", name = "Strihač") // Pridaná funkcia
    )

    fun addContactFunction(name: String) {
        val trimmedName = name.trim()
        // Pridáme funkciu len ak nie je prázdna a ešte neexistuje
        if (trimmedName.isNotBlank() && allContactFunctions.none { it.name.equals(trimmedName, ignoreCase = true) }) {
            allContactFunctions.add(ContactFunction(name = trimmedName))
        }
    }

    fun removeContactFunction(functionId: String) {
        // Odstránime funkciu zo zoznamu
        allContactFunctions.removeIf { it.id == functionId }
        // TODO: Do budúcna by sme tu mali riešiť aj odstránenie ID funkcie zo všetkých kontaktov, ktoré ju mali priradenú.
        // Pre teraz to zjednodušíme.
    }

    // =====================================================================
    // ✅ KROK 2: Aktualizácia dočasných dát pre kontakty
    // =====================================================================
    val contacts = mutableStateListOf<ContactItem>(
        ContactItem(
            id = 1, firstName = "Ján", lastName = "Novák", functionIds = listOf("f1", "f6"), // Ján je Redaktor a Strihač
            phone = "+421900111222", email = "jan@novak.sk", channel = "Jednotka", notes = null
        ),
        ContactItem(
            id = 2, firstName = "Anna", lastName = "Malá", functionIds = listOf("f2"), // Anna je Kamera
            phone = "+421900333444", email = "anna@mala.sk", channel = "Dvojka", notes = "Pracuje len na nočných zmenách."
        ),
        ContactItem(
            id = 3, firstName = "Peter", lastName = "Kováč", functionIds = listOf("f3"), // Peter je IT Podpora
            phone = "+421900555666", email = "peter@kovac.sk", channel = DEFAULT_CHANNEL, notes = null
        ),
        ContactItem(
            id = 4, firstName = "Marek", lastName = "Varga", functionIds = listOf("f4"), // Marek je Hlásateľ
            phone = "+421900777888", email = "marek@varga.sk", channel = "Jednotka", notes = null
        ),
        ContactItem(
            id = 5, firstName = "Zuzana", lastName = "Nová", functionIds = listOf("f5"), // Zuzana je Produkcia
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

    var isFilterDialogVisible by mutableStateOf(false)
        private set

    private val _activeChannelFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeChannelFilters = _activeChannelFilters.asStateFlow()

    fun onFilterDialogDismiss() {
        isFilterDialogVisible = false
    }

    fun onFilterDialogOpen() {
        isFilterDialogVisible = true
    }

    fun onFilterChannelSelected(channel: String, isSelected: Boolean) {
        _activeChannelFilters.update { currentFilters ->
            if (isSelected) currentFilters + channel else currentFilters - channel
        }
    }

    fun clearAllFilters() {
        _activeChannelFilters.value = emptySet()
    }

    // =====================================================================
    // ✅ KROK 3: Úprava filtrovania a zobrazenia
    // =====================================================================
    val displayedContacts: List<ContactItem> by derivedStateOf {
        val currentQuery = searchQuery.trim().lowercase()
        val allFunctions = allContactFunctions // pre prístup vnútri lambdy

        // Ak je vyhľadávanie prázdne, vrátime všetky kontakty
        if (currentQuery.isBlank()) {
            contacts
        } else {
            // Inak filtrujeme podľa textu
            contacts.filter { contact ->
                // Získame mená funkcií pre daný kontakt
                val contactFunctionNames = contact.functionIds.mapNotNull { id ->
                    allFunctions.find { it.id == id }?.name
                }.joinToString(" ")

                contact.getFullName().lowercase().contains(currentQuery) ||
                        contactFunctionNames.lowercase().contains(currentQuery) // Vyhľadávanie v menách funkcií
            }
        }
    }

    // --- SEKCIA PRE DÁTA KANÁLOV ZOSTÁVA ZATIAĽ NEZMENENÁ ---
    // (k nej sa vrátime neskôr, momentálne sa jej nedotýkame)
    private var allChannelData: MutableMap<String, List<ChannelFunction>> = mutableMapOf()
    private val _channelFunctions = MutableStateFlow<List<ChannelFunction>>(emptyList())
    val channelFunctions = _channelFunctions.asStateFlow()
    var isEditMode by mutableStateOf(false)
        private set
    init {
        allChannelData = createSampleChannelData(contacts).toMutableMap()
        loadChannelFunctions(selectedTabFilter.value)
    }
    fun toggleEditMode() { isEditMode = !isEditMode }
    fun addChannelFunction(title: String) {
        val newFunction = ChannelFunction(id = "func_${System.currentTimeMillis()}", title = title, notes = null, assignedPeople = emptyList())
        _channelFunctions.value = _channelFunctions.value + newFunction
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    fun removeChannelFunction(functionId: String) {
        _channelFunctions.value = _channelFunctions.value.filterNot { it.id == functionId }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    fun moveChannelFunction(from: Int, to: Int) {
        val currentFunctions = _channelFunctions.value
        if (from < 0 || to < 0 || from >= currentFunctions.size || to >= currentFunctions.size) return
        _channelFunctions.update { it.toMutableList().apply { add(to, removeAt(from)) } }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    fun updateChannelFunction(functionId: String, newTitle: String, newNotes: String?) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                if (function.id == functionId) function.copy(title = newTitle, notes = newNotes) else function
            }
        }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    fun assignPersonToFunction(functionId: String, contact: ContactItem) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                if (function.id == functionId) {
                    val alreadyAssigned = function.assignedPeople.any { it.contactId == contact.id.toString() }
                    if (alreadyAssigned) {
                        function
                    } else {
                        val newPerson = AssignedPerson(contactId = contact.id.toString(), name = contact.getFullName(), phone = contact.phone, notes = "")
                        function.copy(assignedPeople = function.assignedPeople + newPerson)
                    }
                } else {
                    function
                }
            }
        }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    fun removePersonFromFunction(functionId: String, personId: String) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                if (function.id == functionId) {
                    val updatedPeople = function.assignedPeople.filterNot { it.id == personId }
                    function.copy(assignedPeople = updatedPeople)
                } else {
                    function
                }
            }
        }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    fun updatePersonNoteInFunction(functionId: String, personId: String, newNote: String) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                if (function.id == functionId) {
                    val updatedPeople = function.assignedPeople.map { person ->
                        if (person.id == personId) person.copy(notes = newNote) else person
                    }
                    function.copy(assignedPeople = updatedPeople)
                } else {
                    function
                }
            }
        }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }
    private fun loadChannelFunctions(channelName: String) {
        _channelFunctions.value = allChannelData[channelName] ?: emptyList()
    }
    private fun createSampleChannelData(allContacts: List<ContactItem>): Map<String, List<ChannelFunction>> {
        val contact1 = allContacts.getOrNull(0)
        val contact2 = allContacts.getOrNull(1)
        val contact3 = allContacts.getOrNull(2)
        if (contact1 == null || contact2 == null || contact3 == null) return emptyMap()
        return mapOf(
            "Jednotka" to listOf(
                ChannelFunction(title = "Kameramani", notes = "Všetci kameramani musia byť dostupní 2 hodiny pred začiatkom vysielania.", assignedPeople = listOf(AssignedPerson(contactId = contact1.id.toString(), name = contact1.getFullName(), phone = contact1.phone, notes = "Hlavná kamera"), AssignedPerson(contactId = contact2.id.toString(), name = contact2.getFullName(), phone = contact2.phone, notes = "Ručná kamera"))),
                ChannelFunction(title = "Zvukári", notes = "Skontrolovať mikrofóny pred každým vstupom.", assignedPeople = listOf(AssignedPerson(contactId = contact3.id.toString(), name = contact3.getFullName(), phone = contact3.phone, notes = ""))),
                ChannelFunction(title = "Strihači", notes = null, assignedPeople = emptyList())
            ),
            "24" to listOf(
                ChannelFunction(title = "Redaktori", notes = "Zodpovední za overovanie faktov.", assignedPeople = listOf(AssignedPerson(contactId = contact2.id.toString(), name = contact2.getFullName(), phone = contact2.phone, notes = "Ranná zmena"))),
                ChannelFunction(title = "Vydavatelia", notes = null, assignedPeople = emptyList())
            )
        )
    }

    // =====================================================================
    // ✅ KROK 4: Úprava formulára (form)
    // =====================================================================
    var formId by mutableStateOf(0)
    var formFirstName by mutableStateOf("")
    var formLastName by mutableStateOf("")
    var formChannel by mutableStateOf(DEFAULT_CHANNEL)
    var formFunctionIds by mutableStateOf<List<String>>(emptyList()) // ZMENENÉ: z formFunction na formFunctionIds
    var formPhone by mutableStateOf("")
    var formEmail by mutableStateOf("")
    var formNotes by mutableStateOf("")

    fun updateFirstName(newValue: String) { formFirstName = newValue }
    fun updateLastName(newValue: String) { formLastName = newValue }
    fun updateFunctionIds(newIds: List<String>) { formFunctionIds = newIds } // ZMENENÉ: nová funkcia
    fun updatePhone(newValue: String) { formPhone = newValue }
    fun updateEmail(newValue: String) { formEmail = newValue }
    fun updateNotes(newValue: String) { formNotes = newValue }
    fun updateChannel(newValue: String) { formChannel = newValue }

    fun resetForm() {
        formId = 0
        formFirstName = ""
        formLastName = ""
        formChannel = availableChannels.firstOrNull() ?: DEFAULT_CHANNEL
        formFunctionIds = emptyList() // ZMENENÉ
        formPhone = ""
        formEmail = ""
        formNotes = ""
    }

    // Starú funkciu saveNewContact zatiaľ môžeme nechať, ale nebudeme ju používať
    fun saveNewContact() {
        val newContactItem = ContactItem(
            id = contacts.maxOfOrNull { it.id }?.plus(1) ?: 1,
            firstName = formFirstName.trim(),
            lastName = formLastName.trim(),
            functionIds = formFunctionIds, // ZMENENÉ
            phone = formPhone.trim().ifBlank { null },
            email = formEmail.trim().ifBlank { null },
            channel = formChannel,
            notes = formNotes.trim().ifBlank { null }
        )
        contacts.add(newContactItem)
        resetForm()
    }

    fun addContact(contact: ContactItem) {
        val newContact = contact.copy(id = (contacts.maxOfOrNull { it.id } ?: 0) + 1)
        contacts.add(newContact)
    }

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

    fun updateSelectedTabFilter(newFilter: String) {
        isEditMode = false
        _selectedTabFilter.value = newFilter
        loadChannelFunctions(newFilter)
    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
    }

    fun resetTabToDefault() {
        updateSelectedTabFilter(ALL_CHANNELS_FILTER)
    }

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
            updateSelectedTabFilter(ALL_CHANNELS_FILTER)
        }
    }
}
