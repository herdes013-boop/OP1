// Súbor: EditPasswordScreen.kt
package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    passwordId: String,
    onBack: () -> Unit
) {
    var originalPasswordItem by remember(passwordId) {
        mutableStateOf(viewModel.getPasswordById(passwordId)?.copy())
    }

    // Ak sa položka medzitým zmazala alebo nenájde, vrátime sa späť
    if (originalPasswordItem == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var localPasswordItem by remember { mutableStateOf(originalPasswordItem!!) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember {
        derivedStateOf { localPasswordItem != originalPasswordItem }
    }

    fun saveChanges() {
        viewModel.updatePassword(localPasswordItem)
        originalPasswordItem = localPasswordItem.copy() // Reset pre "hasUnsavedChanges"
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    LaunchedEffect(hasUnsavedChanges) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "Upraviť heslo",
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (hasUnsavedChanges && localPasswordItem.name.isNotBlank()) {
                        Button(
                            onClick = ::saveChanges,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("ULOŽIŤ")
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Viac")
                    }
                }
            )
        )
    }

    BackHandler(onBack = ::handleBackNavigation)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = localPasswordItem.name,
                onValueChange = { localPasswordItem = localPasswordItem.copy(name = it) },
                label = { Text("Názov služby") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = localPasswordItem.name.isBlank()
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

        // Dropdown menu pre Zmazanie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp)
                .wrapContentSize(Alignment.TopEnd)
        ) {
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Zmazať", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, "Zmazať", tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            itemName = originalPasswordItem?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deletePassword(passwordId)
                showDeleteDialog = false
                onBack()
            }
        )
    }

    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedChangesDialog = false
                saveChanges()
                onBack()
            },
            onDiscard = {
                showUnsavedChangesDialog = false
                onBack()
            },
            onCancel = { showUnsavedChangesDialog = false }
        )
    }
}

// Pomocný dialóg pre zmazanie, aby sme sa vyhli opakovaniu
@Composable
private fun DeleteConfirmDialog(itemName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmazať heslo?") },
        text = { Text("Naozaj chcete natrvalo zmazať položku '$itemName'?") },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušiť") } },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Zmazať") }
        }
    )
}
