// Súbor: AddEditIpAddressScreen.kt
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIpAddressScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    ipId: String? = null,
    // ✅ KROK 2.1: Pridanie nového parametra
    onBack: () -> Unit
) {
    val isNewItem = ipId == null

    val originalIpItem: IpItem = remember(ipId) {
        if (isNewItem) {
            viewModel.createEmptyIpItem()
        } else {
            (viewModel.getIpAddressById(ipId!!) ?: viewModel.createEmptyIpItem()).copy()
        }
    }

    var localIpItem by remember { mutableStateOf(originalIpItem) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember {
        derivedStateOf { localIpItem != originalIpItem }
    }

    if (!isNewItem && viewModel.getIpAddressById(ipId!!) == null) {
        LaunchedEffect(Unit) { onBack() } // Použijeme onBack
        return
    }

    fun saveItemAndGoBack() {
        if (isNewItem) {
            viewModel.addIpAddress(localIpItem)
        } else {
            viewModel.updateIpAddress(localIpItem)
        }
        onBack() // Použijeme onBack
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack() // Použijeme onBack
        }
    }

    LaunchedEffect(isNewItem, hasUnsavedChanges) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = if (isNewItem) "Nová IP adresa" else "Upraviť IP adresu",
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

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = localIpItem.name,
                onValueChange = { localIpItem = localIpItem.copy(name = it) },
                label = { Text("Názov zariadenia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = localIpItem.ipAddress,
                onValueChange = { localIpItem = localIpItem.copy(ipAddress = it) },
                label = { Text("IP Adresa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = localIpItem.notes ?: "",
                onValueChange = { localIpItem = localIpItem.copy(notes = it) },
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
            title = { Text("Odstrániť položku?") },
            text = { Text("Naozaj chcete natrvalo odstrániť položku '${originalIpItem.name}'?") },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteIpAddress(originalIpItem.id)
                        showDeleteDialog = false
                        onBack() // Použijeme onBack
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Odstrániť") }
            }
        )
    }

    // ✅ KROK 2.2: Nahradenie starého dialógu novým
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
