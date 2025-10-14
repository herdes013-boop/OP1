// súbor: PasswordsViewModel.kt

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


class PasswordsViewModel : ViewModel() {

    // --- ID Management ---
    private var lastId = 5
    private fun getNextId(): String {
        lastId++
        return lastId.toString()
    }

    fun createEmptyPasswordItem(): PasswordItem {
        return PasswordItem(
            id = getNextId(),
            name = "",
            username = null,
            password = "",
            url = null,
            notes = null
        )
    }

    // --- Ovládanie záložiek (Tabs) ---
    // --- Ovládanie záložiek (Tabs) ---
    private val _selectedTabIndex = mutableStateOf(0)
    val selectedTabIndex: State<Int> = _selectedTabIndex

    /**
     * Nastaví záložku na prvú pozíciu.
     * Volá sa, keď používateľ opustí obrazovku.
     */
    fun resetTabToDefault() {
        _selectedTabIndex.value = 0
    }

    fun onTabSelected(index: Int) {
        _selectedTabIndex.value = index
    }



    // --- Vyhľadávanie ---
    private val _passwordSearchText = MutableStateFlow("")
    val passwordSearchText = _passwordSearchText.asStateFlow()
    fun onPasswordSearchTextChange(text: String) {
        _passwordSearchText.value = text
    }

    private val _ipSearchText = MutableStateFlow("")
    val ipSearchText = _ipSearchText.asStateFlow()
    fun onIpSearchTextChange(text: String) {
        _ipSearchText.value = text
    }


    // =================================================================
    //         SEKCIA PRE ZOZNAM HESIEL (PasswordItem)
    // =================================================================

    private val _passwordList = MutableStateFlow(
        listOf(
            PasswordItem("1", "Google", "uzivatel@gmail.com", "silneHeslo123", "Hlavný účet", url = "https://google.com"),
            PasswordItem("2", "Facebook", "uzivatel@facebook.com", "ineHeslo", null, url = "https://facebook.com"),
            PasswordItem("3", "Bank", "IBAN:123456", "superTajne", "Účet na úspory", url = null)
        )
    )

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

    fun getPasswordById(id: String): PasswordItem? {
        return _passwordList.value.find { it.id == id }
    }

    fun addPassword(item: PasswordItem) {
        _passwordList.update { currentList -> currentList + item }
    }

    fun updatePassword(updatedItem: PasswordItem) {
        _passwordList.update { currentList ->
            currentList.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }

    fun deletePassword(id: String) {
        _passwordList.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }


    // =================================================================
    //         SEKCIA PRE IP ADRESY (IpItem)
    // =================================================================

    private val _ipList = MutableStateFlow(
        listOf(
            IpItem(id = "4", name = "Domáci Router", ipAddress = "192.168.1.1", notes = "Heslo: admin"),
            IpItem(id = "5", name = "Pracovný Server", ipAddress = "10.0.0.52", notes = null)
        )
    )

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

    fun addIpAddress(ipItem: IpItem) {
        _ipList.update { currentList -> currentList + ipItem }
    }

    fun updateIpAddress(updatedItem: IpItem) {
        _ipList.update { currentList ->
            currentList.map { if (it.id == updatedItem.id) updatedItem else it }
        }
    }

    fun getIpAddressById(id: String): IpItem? {
        return _ipList.value.find { it.id == id }
    }

    fun createEmptyIpItem(): IpItem {
        return IpItem(
            id = getNextId(),
            name = "",
            ipAddress = "",
            notes = null
        )
    }

    fun deleteIpAddress(ipId: String) {
        _ipList.update { currentList ->
            currentList.filterNot { it.id == ipId }
        }
    }
}
