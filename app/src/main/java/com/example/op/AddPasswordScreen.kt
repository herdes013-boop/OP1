// Súbor: AddPasswordScreen.kt
package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var localPasswordItem by remember { mutableStateOf(viewModel.createEmptyPasswordItem()) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Zistíme, či má formulár nejaký obsah (okrem predvolených hodnôt)
    val hasContent by remember(localPasswordItem) {
        derivedStateOf {
            localPasswordItem.name.isNotBlank() ||
                    localPasswordItem.username?.isNotBlank() == true ||
                    localPasswordItem.password.isNotBlank() ||
                    localPasswordItem.url?.isNotBlank() == true ||
                    localPasswordItem.notes?.isNotBlank() == true
        }
    }

    fun saveAndGoBack() {
        viewModel.addPassword(localPasswordItem)
        onBack()
    }

    fun handleBackNavigation() {
        if (hasContent) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "Nové heslo",
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    // Tlačidlo ULOŽIŤ sa zobrazí, len ak je vyplnený aspoň názov
                    if (localPasswordItem.name.isNotBlank()) {
                        Button(
                            onClick = ::saveAndGoBack,
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("ULOŽIŤ")
                        }
                    }
                }
            )
        )
    }

    BackHandler(onBack = ::handleBackNavigation)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = localPasswordItem.name,
            onValueChange = { localPasswordItem = localPasswordItem.copy(name = it) },
            label = { Text("Názov služby (napr. Google)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = localPasswordItem.name.isBlank() // Zobrazí chybu, ak je názov prázdny
        )
        OutlinedTextField(
            value = localPasswordItem.username ?: "",
            onValueChange = { localPasswordItem = localPasswordItem.copy(username = it) },
            label = { Text("Meno alebo email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = localPasswordItem.password,
            onValueChange = { localPasswordItem = localPasswordItem.copy(password = it) },
            label = { Text("Heslo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = localPasswordItem.url ?: "",
            onValueChange = { localPasswordItem = localPasswordItem.copy(url = it) },
            label = { Text("URL (voliteľné)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = localPasswordItem.notes ?: "",
            onValueChange = { localPasswordItem = localPasswordItem.copy(notes = it) },
            label = { Text("Poznámky (voliteľné)") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedChangesDialog = false
                saveAndGoBack()
            },
            onDiscard = {
                showUnsavedChangesDialog = false
                onBack()
            },
            onCancel = { showUnsavedChangesDialog = false }
        )
    }
}
