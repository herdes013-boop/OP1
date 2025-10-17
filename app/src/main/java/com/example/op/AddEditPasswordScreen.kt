// Súbor: AddEditPasswordScreen.kt
package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    passwordId: String? = null,
    // ✅ KROK 1: Pridanie nového parametra
    onBack: () -> Unit
) {
    val isNewItem = passwordId == null

    val originalPasswordItem: PasswordItem = remember(passwordId) {
        if (isNewItem) {
            viewModel.createEmptyPasswordItem()} else {
            // Pri úprave nájdeme existujúcu položku A HNEĎ VYTVORÍME KÓPIU
            (viewModel.getPasswordById(passwordId!!) ?: viewModel.createEmptyPasswordItem()).copy()
        }
    }

    var localPasswordItem by remember { mutableStateOf(originalPasswordItem) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember {
        derivedStateOf { localPasswordItem != originalPasswordItem }
    }

    if (!isNewItem && viewModel.getPasswordById(passwordId!!) == null) {
        // ✅ KROK 2: Použitie onBack() namiesto priameho volania
        LaunchedEffect(Unit) { onBack() }
        return
    }

    fun saveItemAndGoBack() {
        if (isNewItem) {
            viewModel.addPassword(localPasswordItem)
        } else {
            viewModel.updatePassword(localPasswordItem)
        }
        // ✅ KROK 2: Použitie onBack() namiesto priameho volania
        onBack()
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            // ✅ KROK 2: Použitie onBack() namiesto priameho volania
            onBack()
        }
    }

    LaunchedEffect(isNewItem, hasUnsavedChanges) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = if (isNewItem) "Nové heslo" else "Upraviť heslo",
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (hasUnsavedChanges) {
                        Button(
                            onClick = ::saveItemAndGoBack,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50) // Zelená
                            )
                        ) {
                            Text("ULOŽIŤ")
                        }
                    }
                    if (!isNewItem) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Viac")
                        }
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
                label = { Text("Názov služby (napr. Google)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            OutlinedTextField(
                value = localPasswordItem.notes ?: "",
                onValueChange = { localPasswordItem = localPasswordItem.copy(notes = it) },
                label = { Text("Poznámky (voliteľné)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp)
        ) {
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Odstrániť heslo?") },
            text = { Text("Naozaj chcete natrvalo odstrániť položku '${originalPasswordItem.name}'?") },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePassword(originalPasswordItem.id)
                        showDeleteDialog = false
                        // ✅ KROK 2: Použitie onBack() namiesto priameho volania
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Odstrániť") }
            }
        )
    }

    // ✅ KROK 3: Nahradenie starého dialógu novým, univerzálnym dialógom
    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedChangesDialog = false
                saveItemAndGoBack()
            },
            onDiscard = {
                showUnsavedChangesDialog = false
                onBack()
            },
            onCancel = {
                showUnsavedChangesDialog = false
            }
        )
    }
}
