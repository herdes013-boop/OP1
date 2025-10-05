package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun AddEditPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    // ==============================================================
    // ========== ZMENA: PRIDANÝ CHÝBAJÚCI PARAMETER ==========
    sharedViewModel: SharedViewModel,
    // ==============================================================
    passwordId: String? = null
) {
    LaunchedEffect(passwordId) {
        if (passwordId != null) {
            viewModel.loadPasswordForEditing(passwordId)
        } else {
            viewModel.resetPasswordForm()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetPasswordForm()
        }
    }

    val isEditing by viewModel.isEditing
    val passwordName by viewModel.passwordName
    val passwordUsername by viewModel.passwordUsername
    val passwordValue by viewModel.passwordValue
    val passwordNotes by viewModel.passwordNotes
    val isFormValid = viewModel.isPasswordFormValid

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // DYNAMICKÉ NASTAVENIE HORNEJ LIŠTY (teraz už bude fungovať)
    LaunchedEffect(isEditing, viewModel.hasUnsavedChanges) {
        val title = if (isEditing) "Upraviť heslo" else "Nové heslo"
        sharedViewModel.setTopBarState(
            TopBarState(
                title = title,
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges) {
                            showUnsavedChangesDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                }
            )
        )
    }

    if (showUnsavedChangesDialog) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            enabled = isFormValid
        ) {
            Text("Uložiť")
        }
    }
}
