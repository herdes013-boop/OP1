package com.example.op

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Dátová trieda, ktorá definuje, ako vyzerá horná lišta
data class TopBarState(
    val title: String = "OP Správca",
    val navigationIcon: @Composable (() -> Unit)? = null,
    val actions: @Composable (() -> Unit)? = null
)

class SharedViewModel : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState = _topBarState.asStateFlow()

    private val _showBottomBar = MutableStateFlow(true)
    val showBottomBar = _showBottomBar.asStateFlow()

    fun setTopBarState(state: TopBarState) {
        _topBarState.value = state
    }

    fun resetTopBarState() {
        _topBarState.value = TopBarState() // Vráti predvolenú lištu
    }

    fun setShowBottomBar(visible: Boolean) {
        _showBottomBar.value = visible
    }
}
