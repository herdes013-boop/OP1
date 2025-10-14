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



// =====================================================================

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
            if (isSelected) {
                currentFilters + channel
            } else {
                currentFilters - channel
            }
        }
    }

    fun clearAllFilters() {
        _activeChannelFilters.value = emptySet()
    }
// =============================================================
// ✅ KONIEC NOVÉHO KÓDU
// =============================================================




    val displayedContacts: List<ContactItem> by derivedStateOf {
        val currentQuery = searchQuery.trim().lowercase()

        // Ak je vyhľadávanie prázdne, vrátime všetky kontakty
        if (currentQuery.isBlank()) {
            contacts
        } else {
            // Inak filtrujeme podľa textu
            contacts.filter { contact ->
                contact.getFullName().lowercase().contains(currentQuery) ||
                        contact.function.orEmpty().lowercase().contains(currentQuery)
            }
        }
    }

    // =====================================================================
    //          ✅ ZAČIATOK SEKCIE PRE DÁTA KANÁLOV ✅
    // =====================================================================

    private var allChannelData: MutableMap<String, List<ChannelFunction>> = mutableMapOf()
    private val _channelFunctions = MutableStateFlow<List<ChannelFunction>>(emptyList())
    val channelFunctions = _channelFunctions.asStateFlow()

    var isEditMode by mutableStateOf(false)
        private set
    init {
        // Načítame počiatočné dáta pre všetky kanály iba raz
        allChannelData = createSampleChannelData(contacts).toMutableMap()
        // Načítame dáta pre predvolene zvolenú kartu
        loadChannelFunctions(selectedTabFilter.value)
    }




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
            id = "func_${System.currentTimeMillis()}",
            title = title,
            notes = null, // ✅ DOPLNENÉ
            assignedPeople = emptyList()
        )
        // K existujúcemu zoznamu funkcií pridáme túto novú
        _channelFunctions.value = _channelFunctions.value + newFunction
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }

    /**
     * Zmaže funkciu zo zoznamu podľa jej ID.
     */
    fun removeChannelFunction(functionId: String) {
        // Vytvoríme nový zoznam, v ktorom budú všetky funkcie OKREM tej, ktorú chceme zmazať
        _channelFunctions.value = _channelFunctions.value.filterNot { it.id == functionId }
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
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
        // ✅ TENTO RIADOK PRIDAJTE NA KONIEC KAŽDEJ MODIFIKUJÚCEJ FUNKCIE
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }

    /**
     * Aktualizuje existujúcu funkciu v zozname.
     * @param functionId ID funkcie, ktorú treba aktualizovať.
     * @param newTitle Nový názov funkcie.
     * @param newNotes Nová poznámka k funkcii.
     */
    fun updateChannelFunction(functionId: String, newTitle: String, newNotes: String?) {
        _channelFunctions.update { currentFunctions ->
            // Vytvoríme nový zoznam (mapovaním cez starý)
            currentFunctions.map { function ->
                // Ak nájdeme správnu funkciu podľa ID, vrátime jej aktualizovanú verziu
                if (function.id == functionId) {
                    function.copy(title = newTitle, notes = newNotes)
                } else {
                    // Inak vrátime pôvodnú funkciu bez zmeny
                    function
                }
            }
        }
        // ✅ TENTO RIADOK PRIDAJTE NA KONIEC KAŽDEJ MODIFIKUJÚCEJ FUNKCIE
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }

    /**
     * Priradí existujúci kontakt ku konkrétnej funkcii.
     * @param functionId ID funkcie, do ktorej pridávame osobu.
     * @param contact Pridávaný kontakt (typu ContactItem).
     */
    fun assignPersonToFunction(functionId: String, contact: ContactItem) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                // Nájdi správnu funkciu
                if (function.id == functionId) {
                    // Skontroluj, či už osoba nie je priradená, aby sme nemali duplicity
                    val alreadyAssigned = function.assignedPeople.any { it.contactId == contact.id.toString() }

                    if (alreadyAssigned) {
                        // Ak už je priradená, vráť funkciu bez zmeny
                        function
                    } else {
                        // Vytvor novú priradenú osobu z ContactItem
                        val newPerson = AssignedPerson(
                            contactId = contact.id.toString(),
                            name = contact.getFullName(),
                            phone = contact.phone,
                            notes = "" // Poznámka je na začiatku prázdna
                        )
                        // Vráť kópiu funkcie s novou osobou v zozname
                        function.copy(assignedPeople = function.assignedPeople + newPerson)
                    }
                } else {
                    // Toto nie je funkcia, ktorú hľadáme, vráť ju bez zmeny
                    function
                }
            }
        }
        // ✅ TENTO RIADOK PRIDAJTE NA KONIEC KAŽDEJ MODIFIKUJÚCEJ FUNKCIE
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }

    /**
     * Odstráni priradenú osobu z funkcie.
     * @param functionId ID funkcie, z ktorej odstraňujeme osobu.
     * @param personId ID priradenej osoby (AssignedPerson.id), ktorú treba odstrániť.
     */
    fun removePersonFromFunction(functionId: String, personId: String) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                // Nájdi správnu funkciu
                if (function.id == functionId) {
                    // Vytvor nový zoznam ľudí, kde bude chýbať ten, ktorého chceme zmazať
                    val updatedPeople = function.assignedPeople.filterNot { it.id == personId }
                    // Vráť kópiu funkcie s aktualizovaným zoznamom
                    function.copy(assignedPeople = updatedPeople)
                } else {
                    // Inú funkciu vráť bez zmeny
                    function
                }
            }
        }
        // ✅ TENTO RIADOK PRIDAJTE NA KONIEC KAŽDEJ MODIFIKUJÚCEJ FUNKCIE
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }

    /**
     * Aktualizuje poznámku konkrétnej priradenej osoby v rámci funkcie.
     * @param functionId ID funkcie, kde sa osoba nachádza.
     * @param personId ID priradenej osoby (AssignedPerson.id).
     * @param newNote Nový text poznámky.
     */
    fun updatePersonNoteInFunction(functionId: String, personId: String, newNote: String) {
        _channelFunctions.update { currentFunctions ->
            currentFunctions.map { function ->
                // Nájdi správnu funkciu
                if (function.id == functionId) {
                    // Vnútri funkcie nájdi a aktualizuj správnu osobu
                    val updatedPeople = function.assignedPeople.map { person ->
                        if (person.id == personId) {
                            // Vráť kópiu osoby s novou poznámkou
                            person.copy(notes = newNote)
                        } else {
                            person // Ostatné osoby vráť bez zmeny
                        }
                    }
                    // Vráť kópiu funkcie s aktualizovaným zoznamom ľudí
                    function.copy(assignedPeople = updatedPeople)
                } else {
                    function // Ostatné funkcie vráť bez zmeny
                }
            }
        }
        // ✅ TENTO RIADOK PRIDAJTE NA KONIEC KAŽDEJ MODIFIKUJÚCEJ FUNKCIE
        allChannelData[selectedTabFilter.value] = _channelFunctions.value
    }

    // Načítanie dát pre zvolený kanál
    private fun loadChannelFunctions(channelName: String) {
        // Už nič nevytvárame, len vyberáme z mapy, ktorú držíme v pamäti
        _channelFunctions.value = allChannelData[channelName] ?: emptyList()
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
                    notes = "Všetci kameramani musia byť dostupní 2 hodiny pred začiatkom vysielania.", // ✅ Pridaná poznámka
                    assignedPeople = listOf(
                        AssignedPerson(contactId = contact1.id.toString(), name = contact1.getFullName(), phone = contact1.phone, notes = "Hlavná kamera"),
                        AssignedPerson(contactId = contact2.id.toString(), name = contact2.getFullName(), phone = contact2.phone, notes = "Ručná kamera")
                    )
                ),
                ChannelFunction(
                    title = "Zvukári",
                    notes = "Skontrolovať mikrofóny pred každým vstupom.", // ✅ Pridaná poznámka
                    assignedPeople = listOf(
                        AssignedPerson(contactId = contact3.id.toString(), name = contact3.getFullName(), phone = contact3.phone, notes = "")
                    )
                ),
                ChannelFunction(title = "Strihači", notes = null, assignedPeople = emptyList()) // Bez poznámky
            ),
            "24" to listOf(
                ChannelFunction(
                    title = "Redaktori",
                    notes = "Zodpovední za overovanie faktov.", // ✅ Pridaná poznámka
                    assignedPeople = listOf(
                        AssignedPerson(contactId = contact2.id.toString(), name = contact2.getFullName(), phone = contact2.phone, notes = "Ranná zmena")
                    )
                ),
                ChannelFunction(title = "Vydavatelia", notes = null, assignedPeople = emptyList())
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
        _selectedTabFilter.value = newFilter
        // Po zmene filtra iba znovu načítame dáta pre daný kanál z našej pamäte
        loadChannelFunctions(newFilter)
    }

    fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
    }

    // ✅ TÚTO FUNKCIU PRIDAJTE
    /**
     * Resetuje filter záložiek na predvolenú hodnotu ("Všetky").
     * Volá sa pri opustení sekcie Kontaktov.
     */
    fun resetTabToDefault() {
        // Jednoducho zavoláme existujúcu funkciu so správnou hodnotou
        updateSelectedTabFilter(ALL_CHANNELS_FILTER)
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
