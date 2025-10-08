package com.example.op

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TopBarState(
    val title: String = "Návody",
    val navigationIcon: (@Composable () -> Unit)? = null,
    // Zmeníme typ, aby bol kompatibilný s `actions` v TopAppBar
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val isVisible: Boolean = true,
)

class SharedViewModel : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState = _topBarState.asStateFlow()

    private val _showBottomBar = MutableStateFlow(true)
    val showBottomBar = _showBottomBar.asStateFlow()

    fun setTopBarState(newState: TopBarState) {
        _topBarState.value = newState
    }

    // ===== PRIDANÁ FUNKCIA =====
    // Táto funkcia aktualizuje IBA titulok a ostatné nastavenia lišty
    // (ikony, akcie, viditeľnosť) nechá nedotknuté.
    fun updateTopBarTitle(newTitle: String) {
        _topBarState.value = _topBarState.value.copy(title = newTitle)
    }
    // ===== KONIEC PRIDANEJ FUNKCIE =====

    fun resetTopBarState() {
        _topBarState.value = TopBarState() // Vráti na predvolený stav
    }

    fun setShowBottomBar(isVisible: Boolean) {
        _showBottomBar.value = isVisible
    }
    fun updateTopBarActions(newActions: @Composable RowScope.() -> Unit) {
        // Namiesto .update { ... } použijeme priame priradenie hodnoty
        _topBarState.value = _topBarState.value.copy(actions = newActions)
    }
}

