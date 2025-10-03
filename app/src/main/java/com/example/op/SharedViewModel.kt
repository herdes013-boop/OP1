package com.example.op

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// ========= ZMENA TU =========
data class TopBarState(
    val title: String = "Návody",
    val navigationIcon: (@Composable () -> Unit)? = null,
    val actions: (@Composable () -> Unit)? = null,
    val isVisible: Boolean = true // PRIDANÝ PARAMETER
)

class SharedViewModel : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState = _topBarState.asStateFlow()

    private val _showBottomBar = MutableStateFlow(true)
    val showBottomBar = _showBottomBar.asStateFlow()

    fun setTopBarState(newState: TopBarState) {
        _topBarState.value = newState
    }

    fun resetTopBarState() {
        _topBarState.value = TopBarState() // Vráti na predvolený stav
    }

    fun setShowBottomBar(isVisible: Boolean) {
        _showBottomBar.value = isVisible
    }
}
