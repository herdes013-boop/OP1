package com.example.op

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    // SEKCA PRE ZOZNAM HESIEL (PasswordItem)
    // =================================================================

    private val _passwordList = MutableStateFlow(
        listOf(
            PasswordItem("1", "Google", "uzivatel@gmail.com", "silneHeslo123", "Hlavný účet"),
            PasswordItem("2", "Facebook", "uzivatel@facebook.com", "ineHeslo", null),
            PasswordItem("3", "Bank", "IBAN:123456", "superTajne", "Účet na úspory")
        )
    )
    val passwordList = _passwordList.asStateFlow()

    // =================================================================
    // SEKCA: Stav pre Detail obrazovku
    // =================================================================

    private val _selectedPassword = MutableStateFlow<PasswordItem?>(null)
    val selectedPassword = _selectedPassword.asStateFlow()

    fun loadPasswordDetail(passwordId: String) {
        viewModelScope.launch {
            _selectedPassword.value = _passwordList.value.firstOrNull { it.id == passwordId }
        }
    }

    // =================================================================
    // SEKCA: Stav pre Add/Edit formulár
    // =================================================================

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

    val isPasswordFormValid: Boolean
        get() = _passwordName.value.isNotBlank()

    val hasUnsavedChanges: Boolean
        get() {
            return if (originalPasswordItem != null) { // Režim úprav
                originalPasswordItem?.name != _passwordName.value ||
                        (originalPasswordItem?.username ?: "") != _passwordUsername.value ||
                        originalPasswordItem?.password != _passwordValue.value ||
                        (originalPasswordItem?.notes ?: "") != _passwordNotes.value
            } else { // Režim pridávania nového hesla
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
            originalPasswordItem = password // Uložíme si originál pre porovnanie
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

    // --- FUNKCIA GENERÁTORA HESIEL ---
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*"

    fun generateAndSetRandomPassword(length: Int = 12) {
        _passwordValue.value = (1..length)
            .map { charset.random(Random) }
            .joinToString("")
    }

    // --- CRUD OPERÁCIE ---
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
        // Ak mažeme práve zobrazené heslo, resetujeme aj stav pre detail
        if (_selectedPassword.value?.id == id) {
            _selectedPassword.value = null
        }
    }

    // =================================================================
    // SEKCA PRE IP ADRESY (IpItem) - bez zmeny
    // =================================================================

    private val _ipList = MutableStateFlow(
        listOf(
            IpItem(id = "4", name = "Domáci Router", ipAddress = "192.168.1.1"),
            IpItem(id = "5", name = "Pracovný Server", ipAddress = "10.0.0.52")
        )
    )
    val ipList = _ipList.asStateFlow()

    fun addIpAddress(name: String, ipAddress: String) {
        val newItem = IpItem(id = getNextId(), name = name, ipAddress = ipAddress)
        _ipList.update { currentList -> currentList + newItem }
    }
}
