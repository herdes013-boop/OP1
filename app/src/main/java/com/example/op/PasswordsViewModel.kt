package com.example.op

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class PasswordsViewModel : ViewModel() {

    // --- ID Management ---
    private var lastId = 5
    private fun getNextId(): String {
        lastId++
        return lastId.toString()
    }

    // =================================================================
    //         ✅ UPRAVENÁ SEKCA: Oddelené vyhľadávanie
    // =================================================================
    // Vyhľadávanie pre Heslá
    private val _passwordSearchText = MutableStateFlow("")
    val passwordSearchText = _passwordSearchText.asStateFlow()

    fun onPasswordSearchTextChange(text: String) {
        _passwordSearchText.value = text
    }

    // Vyhľadávanie pre IP Adresy
    private val _ipSearchText = MutableStateFlow("")
    val ipSearchText = _ipSearchText.asStateFlow()

    fun onIpSearchTextChange(text: String) {
        _ipSearchText.value = text
    }
    // =================================================================


    // =================================================================
    // SEKCA: Stav pre používateľské rozhranie (UI State)
    // =================================================================

    private val _selectedTabIndex = mutableStateOf(0)
    val selectedTabIndex: State<Int> = _selectedTabIndex

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }


    // =================================================================
    // SEKCA PRE ZOZNAM HESIEL (PasswordItem)
    // =================================================================

    private val _passwordList = MutableStateFlow(
        listOf(
            PasswordItem("1", "Google", "uzivatel@gmail.com", "silneHeslo123", "Hlavný účet"),
            PasswordItem("2", "Facebook", "uzivatel@facebook.com", "ineHeslo", null),
            PasswordItem("3", "Bank", "IBAN:123456", "superTajne", "Účet na úspory")
        )
    )

    // ✅ Upravený zoznam, ktorý reaguje na _passwordSearchText
    val passwordList = passwordSearchText
        .combine(_passwordList) { text, passwords ->
            if (text.isBlank()) {
                passwords
            } else {
                passwords.filter { it.doesMatchSearchQuery(text) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _passwordList.value
        )

    // Ostatné funkcie pre heslá zostávajú rovnaké...
    fun getPasswordById(id: String): PasswordItem? {
        return _passwordList.value.find { it.id == id }
    }
    private val _selectedPassword = MutableStateFlow<PasswordItem?>(null)
    val selectedPassword = _selectedPassword.asStateFlow()
    fun loadPasswordDetail(passwordId: String) {
        viewModelScope.launch {
            _selectedPassword.value = _passwordList.value.firstOrNull { it.id == passwordId }
        }
    }
    fun clearSelectedPassword() {
        _selectedPassword.value = null
    }
    private val _passwordName = mutableStateOf("")
    val passwordName: State<String> = _passwordName
    private val _passwordUsername = mutableStateOf("")
    val passwordUsername: State<String> = _passwordUsername
    private val _passwordValue = mutableStateOf("")
    val passwordValue: State<String> = _passwordValue
    private val _passwordNotes = mutableStateOf("")
    val passwordNotes: State<String> = _passwordNotes
    private val _isEditing = mutableStateOf(false)
    val isEditing: State<Boolean> = _isEditing
    private var originalPasswordItem: PasswordItem? = null
    val isPasswordFormValid: Boolean get() = _passwordName.value.isNotBlank()
    val hasUnsavedChanges: Boolean get() {
        return if (originalPasswordItem != null) {
            originalPasswordItem?.name != _passwordName.value ||
                    (originalPasswordItem?.username ?: "") != _passwordUsername.value ||
                    originalPasswordItem?.password != _passwordValue.value ||
                    (originalPasswordItem?.notes ?: "") != _passwordNotes.value
        } else {
            _passwordName.value.isNotEmpty() ||
                    _passwordUsername.value.isNotEmpty() ||
                    _passwordValue.value.isNotEmpty() ||
                    _passwordNotes.value.isNotEmpty()
        }
    }
    fun onPasswordNameChange(newName: String) { _passwordName.value = newName }
    fun onPasswordUsernameChange(newUsername: String) { _passwordUsername.value = newUsername }
    fun onPasswordValueChange(newValue: String) { _passwordValue.value = newValue }
    fun onPasswordNotesChange(newNotes: String) { _passwordNotes.value = newNotes }
    fun loadPasswordForEditing(passwordId: String) {
        val password = _passwordList.value.firstOrNull { it.id == passwordId }
        if (password != null) {
            originalPasswordItem = password
            _passwordName.value = password.name
            _passwordUsername.value = password.username ?: ""
            _passwordValue.value = password.password
            _passwordNotes.value = password.notes ?: ""
            _isEditing.value = true
        }
    }
    fun savePassword() {
        if (!isPasswordFormValid) return
        if (_isEditing.value) {
            val updatedItem = originalPasswordItem!!.copy(
                name = _passwordName.value,
                username = _passwordUsername.value.ifBlank { null },
                password = _passwordValue.value,
                notes = _passwordNotes.value.ifBlank { null }
            )
            updatePassword(updatedItem)
        } else {
            addPassword(
                title = _passwordName.value,
                username = _passwordUsername.value.ifBlank { null },
                passwordEncrypted = _passwordValue.value,
                notes = _passwordNotes.value.ifBlank { null }
            )
        }
        resetPasswordForm()
    }
    fun resetPasswordForm() {
        originalPasswordItem = null
        _passwordName.value = ""
        _passwordUsername.value = ""
        _passwordValue.value = ""
        _passwordNotes.value = ""
        _isEditing.value = false
    }
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
    fun generateAndSetRandomPassword(length: Int = 12) {
        _passwordValue.value = (1..length)
            .map { charset.random(Random) }
            .joinToString("")
    }
    private fun addPassword(title: String, username: String?, passwordEncrypted: String, notes: String?) {
        val newItem = PasswordItem(
            id = getNextId(),
            name = title,
            username = username,
            password = passwordEncrypted,
            notes = notes
        )
        _passwordList.update { currentList -> currentList + newItem }
    }
    private fun updatePassword(updatedItem: PasswordItem) {
        _passwordList.update { currentList ->
            currentList.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }
    fun deletePassword(id: String) {
        _passwordList.update { currentList ->
            currentList.filterNot { it.id == id }
        }
        if (_selectedPassword.value?.id == id) {
            _selectedPassword.value = null
        }
    }


    // =================================================================
    // SEKCA PRE IP ADRESY (IpItem)
    // =================================================================

    private val _ipList = MutableStateFlow(
        listOf(
            IpItem(id = "4", name = "Domáci Router", ipAddress = "192.168.1.1", notes = "Heslo: admin"),
            IpItem(id = "5", name = "Pracovný Server", ipAddress = "10.0.0.52", notes = null) // alebo notes = "Firemný server"
        )
    )

    // ✅ Upravený zoznam, ktorý reaguje na _ipSearchText
    val ipList = ipSearchText
        .combine(_ipList) { text, ips ->
            if (text.isBlank()) {
                ips
            } else {
                ips.filter { it.doesMatchSearchQuery(text) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _ipList.value
        )

    fun addIpAddress(name: String, ipAddress: String, notes: String?) {
        val newItem = IpItem(
            id = getNextId(),
            name = name,
            ipAddress = ipAddress,
            notes = notes // <-- TOTO SME PRIDALI
        )
        _ipList.update { currentList -> currentList + newItem }
    }
    fun updateIpAddress(updatedItem: IpItem) {
        _ipList.update { currentList ->
            currentList.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }

    fun getIpAddressById(id: String): IpItem? {
        return _ipList.value.find { it.id == id }
    }

    // ✅ TOTO STE PRÁVE PRIDALI ✅
    fun deleteIpAddress(ipId: String) {
        _ipList.update { currentList ->
            currentList.filterNot { it.id == ipId }
        }
    }
}
