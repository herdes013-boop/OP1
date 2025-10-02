package com.example.op

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Trieda ContactItem je definovaná v ContactItem.kt.

class ContactsViewModel : ViewModel() {

    // 1. Stav pre kanály ako mutableStateListOf
    val channelOptions = mutableStateListOf(
        "Všetky", // Pridaná možnosť pre zobrazenie všetkých
        "Telefón", "Email", "Sociálne siete", "Osobný kontakt"
    )

    // 2. Stav pre kontakty (mock dáta)
    private val _contacts = mutableStateListOf(
        ContactItem(1, "Ján", "Novák", "0900111222", "jan.novak@corp.sk", "Manažér", "Email", "Stretnutie 15.3."),
        ContactItem(2, "Alena", "Kováčová", "0901333444", "alena.kovacova@corp.sk", "Predajca", "Telefón", "Preferuje volanie"),
        ContactItem(3, "Peter", "Mrkvička", null, null, "IT", "Osobný kontakt", "Len interný kontakt")
    )

    val contacts: List<ContactItem> get() = _contacts

    // Pomocná funkcia pre generovanie ID
    private var nextContactId = _contacts.maxOfOrNull { it.id }?.plus(1) ?: 4

    // -----------------------------------------------------------------
    // FILTERING A VYBRANÝ KANÁL
    // -----------------------------------------------------------------

    // Stav pre vybraný filter kanálov
    var selectedChannel by mutableStateOf(channelOptions.first())
        private set

    // Computed vlastnosť pre zobrazenie filtrovaných kontaktov
    val displayedContacts: List<ContactItem>
        get() = if (selectedChannel == "Všetky") {
            _contacts
        } else {
            _contacts.filter { it.channel == selectedChannel }
        }

    // Funkcia na zmenu filtra
    fun updateSelectedChannel(channel: String) {
        selectedChannel = channel
    }

    // -----------------------------------------------------------------
    // STAV FORMULÁRA PRE PRIDANIE/EDITÁCIU KONTAKTU (bez zmeny, v poriadku)
    // -----------------------------------------------------------------

    var formFirstName by mutableStateOf<String?>(null)
        private set
    var formLastName by mutableStateOf<String?>(null)
        private set
    var formPhone by mutableStateOf<String?>(null)
        private set
    var formEmail by mutableStateOf<String?>(null)
        private set
    var formFunction by mutableStateOf<String?>(null)
        private set
    var formNotes by mutableStateOf<String?>(null)
        private set
    var formChannel by mutableStateOf<String?>(channelOptions.firstOrNull())
        private set

    // -----------------------------------------------------------------
    // COMPUTED VLASTNOSTI PRE BEZPEČNÉ POUŽITIE V UI
    // -----------------------------------------------------------------

    val formFirstNameValue: String get() = formFirstName.orEmpty()
    val formLastNameValue: String get() = formLastName.orEmpty()
    val formPhoneValue: String get() = formPhone.orEmpty()
    val formEmailValue: String get() = formEmail.orEmpty()
    val formFunctionValue: String get() = formFunction.orEmpty()
    val formNotesValue: String get() = formNotes.orEmpty()
    // Používame .orEmpty(), pretože formChannel môže byť null, ak sú channelOptions prázdne
    val formChannelValue: String get() = formChannel.orEmpty()

    // -----------------------------------------------------------------
    // FUNKCIE PRE STAV FORMULÁRA
    // -------------------------------------------------

    fun updateFirstName(value: String) { formFirstName = value }
    fun updateLastName(value: String) { formLastName = value }
    fun updatePhone(value: String) { formPhone = value }
    fun updateEmail(value: String) { formEmail = value }
    fun updateFunction(value: String) { formFunction = value }
    fun updateNotes(value: String) { formNotes = value }
    fun updateChannel(value: String) { formChannel = value }

    fun resetForm() {
        formFirstName = null
        formLastName = null
        formPhone = null
        formEmail = null
        formFunction = null
        formNotes = null
        // Nastavíme formulár na prvú možnosť (teraz "Všetky")
        formChannel = channelOptions.firstOrNull()
    }

    // -----------------------------------------------------------------
    // FUNKCIE PRE KONTAKTY
    // -----------------------------------------------------------------

    fun getContactById(id: Int): ContactItem? {
        return _contacts.find { it.id == id }
    }

    fun loadContactForEdit(id: Int) {
        val contact = getContactById(id)
        if (contact != null) {
            formFirstName = contact.firstName
            formLastName = contact.lastName
            formPhone = contact.phone
            formEmail = contact.email
            formFunction = contact.function
            formNotes = contact.notes
            formChannel = contact.channel
        } else {
            resetForm()
        }
    }

    fun saveEditedContact(id: Int) {
        val index = _contacts.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedContact = ContactItem(
                id = id,
                firstName = formFirstName?.trim(),
                lastName = formLastName?.trim(),
                phone = formPhone?.trim(),
                email = formEmail?.trim(),
                function = formFunction?.trim(),
                notes = formNotes?.trim(),
                channel = formChannel?.trim()
            )
            _contacts[index] = updatedContact
            resetForm()
        }
    }

    fun addContact(contact: ContactItem) {
        val newContact = contact.copy(id = nextContactId++)
        _contacts.add(newContact)
    }

    fun saveNewContact(): Boolean {
        if (formFirstName.isNullOrBlank() && formLastName.isNullOrBlank()) {
            return false
        }

        val newContact = ContactItem(
            id = 0,
            firstName = formFirstName?.trim(),
            lastName = formLastName?.trim(),
            phone = formPhone?.trim(),
            email = formEmail?.trim(),
            function = formFunction?.trim(),
            notes = formNotes?.trim(),
            channel = formChannel?.trim()
        )

        addContact(newContact)
        resetForm()
        return true
    }

    fun updateContact(updatedContact: ContactItem) {
        val index = _contacts.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            _contacts[index] = updatedContact
        }
    }

    fun removeContact(contact: ContactItem) {
        _contacts.remove(contact)
    }

    fun deleteContact(id: Int) {
        val index = _contacts.indexOfFirst { it.id == id }
        if (index != -1) {
            _contacts.removeAt(index)
        }
    }

    // -----------------------------------------------------------------
    // FUNKCIE PRE KANÁLY
    // -----------------------------------------------------------------

    fun addChannel(channel: String) {
        if (channel.isNotBlank() && !channelOptions.contains(channel) && channel != "Všetky") {
            channelOptions.add(channel)
        }
    }

    fun removeChannel(channel: String) {
        if (channelOptions.remove(channel)) {
            val contactsToUpdate = _contacts.filter { it.channel == channel }
            val defaultChannel = channelOptions.firstOrNull() ?: "Všetky" // Nastavíme default na "Všetky"

            contactsToUpdate.forEach { contact ->
                val updatedContact = contact.copy(
                    channel = defaultChannel
                )
                updateContact(updatedContact)
            }
            if (formChannel == channel) {
                formChannel = defaultChannel
            }
            // Ak je odstránený kanál, aktualizujeme aj filter, ak je nastavený na daný kanál
            if (selectedChannel == channel) {
                selectedChannel = defaultChannel
            }
        }
    }
}
