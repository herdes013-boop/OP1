package com.example.op

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswordsViewModel : ViewModel() {

    // Kód pre automatické inkrementovanie ID
    private val currentId = MutableStateFlow(3)
    private fun getNextId(): Int {
        val next = currentId.value + 1
        currentId.value = next
        return next
    }

    // Zoznam hesiel (simulácia databázy)
    private val _passwordList = MutableStateFlow(
        listOf(
            PasswordItem(1, "Google", "uzivatel@gmail.com", "silneHeslo123", "Hlavný účet"),
            PasswordItem(2, "Facebook", "uzivatel@facebook.com", "ineHeslo", null),
            PasswordItem(3, "Bank", "IBAN:123456", "superTajne", "Účet na úspory")
        ).toMutableStateList()
    )
    val passwordList: StateFlow<List<PasswordItem>> = _passwordList

    // ------------------------------------
    // CRUD OPERÁCIE
    // ------------------------------------

    /**
     * Pridá nové heslo do zoznamu.
     */
    fun addPassword(title: String, username: String?, passwordEncrypted: String, notes: String?) {
        viewModelScope.launch {
            val newItem = PasswordItem(
                id = getNextId(),
                title = title,
                username = username,
                passwordEncrypted = passwordEncrypted,
                notes = notes
            )
            _passwordList.value.add(newItem)
        }
    }

    /**
     * Aktualizuje existujúce heslo.
     */
    fun updatePassword(updatedItem: PasswordItem) {
        viewModelScope.launch {
            val list = _passwordList.value
            val index = list.indexOfFirst { it.id == updatedItem.id }
            if (index != -1) {
                list[index] = updatedItem
            }
        }
    }

    /**
     * Odstráni heslo zoznamu podľa ID.
     */
    fun deletePassword(id: Int) {
        viewModelScope.launch {
            _passwordList.value.removeIf { it.id == id }
        }
    }

    /**
     * Nájde heslo podľa ID (pre EditPasswordScreen).
     */
    fun getPasswordById(id: Int): PasswordItem? {
        return _passwordList.value.find { it.id == id }
    }
}