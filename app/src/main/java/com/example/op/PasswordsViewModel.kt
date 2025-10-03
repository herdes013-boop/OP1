package com.example.op

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
    // SEKCA PRE HESLÁ (PasswordItem)
    // =================================================================

    // OPRAVA: Používame MutableStateFlow s obyčajným List, bez .toMutableStateList()
    private val _passwordList = MutableStateFlow(
        listOf(
            PasswordItem("1", "Google", "uzivatel@gmail.com", "silneHeslo123", "Hlavný účet"),
            PasswordItem("2", "Facebook", "uzivatel@facebook.com", "ineHeslo", null),
            PasswordItem("3", "Bank", "IBAN:123456", "superTajne", "Účet na úspory")
        )
    )
    // Používame .asStateFlow() na vystavenie imutabilnej verzie, ktorú UI nemôže meniť
    val passwordList = _passwordList.asStateFlow()

    // --- FUNKCIA GENERÁTORA HESIEL ---
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*"

    fun generateRandomPassword(length: Int = 12): String {
        return (1..length)
            .map { charset.random(Random) }
            .joinToString("")
    }

    // --- CRUD OPERÁCIE PRE HESLÁ (upravené pre imutabilitu) ---
    fun addPassword(title: String, username: String?, passwordEncrypted: String, notes: String?) {
        val newItem = PasswordItem(
            id = getNextId(),
            name = title,
            username = username,
            password = passwordEncrypted,
            notes = notes
        )
        // OPRAVA: Vytvárame nový zoznam (+) namiesto modifikácie starého (.add)
        _passwordList.update { currentList -> currentList + newItem }
    }

    fun updatePassword(updatedItem: PasswordItem) {
        _passwordList.update { currentList ->
            // Prejdeme zoznam a nahradíme len ten prvok, ktorý sa zhoduje s id
            currentList.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }

    fun deletePassword(id: String) {
        _passwordList.update { currentList ->
            // Vytvoríme nový zoznam, ktorý neobsahuje prvok s daným id
            currentList.filterNot { it.id == id }
        }
    }

    fun getPasswordById(id: String): PasswordItem? {
        return _passwordList.value.find { it.id == id }
    }


    // =================================================================
    // SEKCA PRE IP ADRESY (IpItem)
    // =================================================================

    // OPRAVA: Používame MutableStateFlow s obyčajným List
    private val _ipList = MutableStateFlow(
        listOf(
            IpItem(id = "4", name = "Domáci Router", ipAddress = "192.168.1.1"),
            IpItem(id = "5", name = "Pracovný Server", ipAddress = "10.0.0.52")
        )
    )
    val ipList = _ipList.asStateFlow()

    // --- CRUD OPERÁCIE PRE IP ADRESY (upravené pre imutabilitu) ---
    fun addIpAddress(name: String, ipAddress: String) {
        val newItem = IpItem(
            id = getNextId(),
            name = name,
            ipAddress = ipAddress
        )
        _ipList.update { currentList -> currentList + newItem }
    }

    fun updateIpAddress(updatedItem: IpItem) {
        _ipList.update { currentList ->
            currentList.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }

    fun deleteIpAddress(id: String) {
        _ipList.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }

    fun getIpAddressById(id: String): IpItem? {
        return _ipList.value.find { it.id == id }
    }
}
