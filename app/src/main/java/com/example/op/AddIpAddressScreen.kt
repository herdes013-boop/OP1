// Súbor: AddIpAddressScreen.kt
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIpAddressScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    var localIpItem by remember { mutableStateOf(viewModel.createEmptyIpItem()) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val hasContent by remember(localIpItem) {
        derivedStateOf {
            localIpItem.name.isNotBlank() ||
                    localIpItem.ipAddress.isNotBlank() ||
                    localIpItem.notes?.isNotBlank() == true
        }
    }

    fun saveAndGoBack() {
        viewModel.addIpAddress(localIpItem)
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
                title = "Nová IP adresa",
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (localIpItem.name.isNotBlank() && localIpItem.ipAddress.isNotBlank()) {
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
