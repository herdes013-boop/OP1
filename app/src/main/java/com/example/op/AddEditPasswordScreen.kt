package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    passwordId: String? = null
) {
    // 1. Príprava stavov a načítanie dát
    val isNewItem = passwordId == null

    // Načítame položku alebo vytvoríme novú
    val originalPasswordItem: PasswordItem = remember(passwordId) {
        if (isNewItem) {
            viewModel.createEmptyPasswordItem() // Zmenené
        } else {
            viewModel.getPasswordById(passwordId!!) ?: viewModel.createEmptyPasswordItem() // Zmenené
        }
    }

    // Lokálny stav, ktorý používateľ upravuje vo formulári
    var localPasswordItem by remember { mutableStateOf(originalPasswordItem) } // Zmenené

    // Stavy pre dialógy a menu
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Vypočítaný stav, ktorý zisťuje, či sú zmeny
    val hasUnsavedChanges by remember {
        derivedStateOf { localPasswordItem != originalPasswordItem } // Zmenené
    }

    // Ak upravujeme neexistujúcu položku, vrátime sa späť
    if (!isNewItem && viewModel.getPasswordById(passwordId!!) == null) { // Zmenené
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // 2. Funkcie pre ukladanie, mazanie a navigáciu
    fun saveItemAndGoBack() {
        if (isNewItem) {
            viewModel.addPassword(localPasswordItem) // Zmenené
        } else {
            viewModel.updatePassword(localPasswordItem) // Zmenené
        }
        navController.popBackStack()
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            navController.popBackStack()
        }
    }

    // 3. Nastavenie horného panela (TopAppBar) cez SharedViewModel
    LaunchedEffect(isNewItem, hasUnsavedChanges) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = if (isNewItem) "Nové heslo" else "Upraviť heslo", // Zmenené
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
                                containerColor = Color(0xFF4CAF50)
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

    // 4. Hlavný obsah obrazovky (formulár)
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Zmenené polia pre heslo
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
                singleLine = true,
                visualTransformation = PasswordVisualTransformation() // Skryje heslo
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

    // 5. Dialógy pre potvrdenie
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Odstrániť heslo?") }, // Zmenené
            text = { Text("Naozaj chcete natrvalo odstrániť položku '${originalPasswordItem.name}'?") }, // Zmenené
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePassword(originalPasswordItem.id) // Zmenené
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Odstrániť") }
            }
        )
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Neuložené zmeny") },
            text = { Text("Máte neuložené zmeny. Chcete ich zahodiť a odísť?") },
            confirmButton = {
                Button(onClick = {
                    showUnsavedChangesDialog = false
                    navController.popBackStack()
                }) { Text("Zahodiť a odísť") }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) { Text("Zostať") }
            }
        )
    }
}

