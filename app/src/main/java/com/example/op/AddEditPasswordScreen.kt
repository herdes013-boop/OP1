package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    navController: NavController,
    viewModel: PasswordsViewModel,
    passwordId: String? = null
) {
    // Načítame dáta pre úpravu, ak je poskytnuté ID.
    // LaunchedEffect sa spustí len raz, keď sa obrazovka prvýkrát načíta.
    LaunchedEffect(passwordId) {
        if (passwordId != null) {
            viewModel.loadPasswordForEditing(passwordId)
        } else {
            viewModel.resetPasswordForm()
        }
    }

    // Pri odchode z obrazovky VŽDY resetujeme formulár, aby bol čistý pre ďalšie použitie.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetPasswordForm()
        }
    }

    // Získavame stavy priamo z ViewModelu
    val isEditing by viewModel.isEditing
    val passwordName by viewModel.passwordName
    val passwordUsername by viewModel.passwordUsername
    val passwordValue by viewModel.passwordValue
    val passwordNotes by viewModel.passwordNotes
    val isFormValid = viewModel.isPasswordFormValid

    // --- Problém č.2: Dialóg pre neuložené zmeny ---
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    if (showUnsavedChangesDialog) {
        // Môžeme použiť existujúci UnsavedChangesDialog, ak ho máme, alebo vytvoriť nový
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Neuložené zmeny") },
            text = { Text("Naozaj chcete odísť bez uloženia zmien?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedChangesDialog = false
                    navController.popBackStack()
                }) { Text("Odísť") }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) { Text("Zostať") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť heslo" else "Nové heslo") },
                navigationIcon = {
                    IconButton(onClick = {
                        // --- Problém č.2: Kontrola pred odchodom ---
                        if (viewModel.hasUnsavedChanges) {
                            showUnsavedChangesDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Naspäť")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = passwordName,
                onValueChange = { viewModel.onPasswordNameChange(it) },
                label = { Text("Názov služby") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = passwordUsername,
                onValueChange = { viewModel.onPasswordUsernameChange(it) },
                label = { Text("Používateľské meno / E-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = passwordValue,
                onValueChange = { viewModel.onPasswordValueChange(it) },
                label = { Text("Heslo") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.generateAndSetRandomPassword() }) {
                        Icon(Icons.Filled.VpnKey, "Generovať heslo")
                    }
                }
            )

            OutlinedTextField(
                value = passwordNotes,
                onValueChange = { viewModel.onPasswordNotesChange(it) },
                label = { Text("Poznámky (voliteľné)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.savePassword()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                // --- Problém č.1: Povolenie tlačidla ---
                enabled = isFormValid
            ) {
                Text("Uložiť")
            }
        }
    }
}
