// Súbor: EditIpAddressScreen.kt
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
fun EditIpAddressScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    ipId: String,
    onBack: () -> Unit
) {
    var originalIpItem by remember(ipId) {
        mutableStateOf(viewModel.getIpAddressById(ipId)?.copy())
    }

    if (originalIpItem == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var localIpItem by remember { mutableStateOf(originalIpItem!!) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember {
        derivedStateOf { localIpItem != originalIpItem }
    }

    fun saveChanges() {
        viewModel.updateIpAddress(localIpItem)
        originalIpItem = localIpItem.copy()
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
                title = "Upraviť IP adresu",
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (hasUnsavedChanges && localIpItem.name.isNotBlank() && localIpItem.ipAddress.isNotBlank()) {
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
                value = localIpItem.name,
                onValueChange = { localIpItem = localIpItem.copy(name = it) },
                label = { Text("Názov zariadenia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = localIpItem.name.isBlank()
            )
            OutlinedTextField(
                value = localIpItem.ipAddress,
                onValueChange = { localIpItem = localIpItem.copy(ipAddress = it) },
                label = { Text("IP Adresa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = localIpItem.ipAddress.isBlank()
            )
            OutlinedTextField(
                value = localIpItem.notes ?: "",
                onValueChange = { localIpItem = localIpItem.copy(notes = it) },
                label = { Text("Poznámky (voliteľné)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

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
        DeleteIpConfirmDialog(
            itemName = originalIpItem?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteIpAddress(ipId)
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

@Composable
private fun DeleteIpConfirmDialog(itemName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmazať IP adresu?") },
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
