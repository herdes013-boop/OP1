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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordScreen(
    navController: NavController,
    viewModel: PasswordsViewModel = viewModel(),
    passwordId: String? = null
) {
    val isEditing = passwordId != null
    val initialPassword = if (isEditing) viewModel.getPasswordById(passwordId!!) else null

    // Ak upravujeme neexistujúce heslo, vrátime sa späť
    if (isEditing && initialPassword == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var title by remember { mutableStateOf(initialPassword?.name ?: "") }
    var username by remember { mutableStateOf(initialPassword?.username ?: "") }
    var password by remember { mutableStateOf(initialPassword?.password ?: "") }
    var notes by remember { mutableStateOf(initialPassword?.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť heslo" else "Nové heslo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                value = title,
                onValueChange = { title = it },
                label = { Text("Názov služby") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Používateľské meno / E-mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Heslo") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { password = viewModel.generateRandomPassword() }) {
                        Icon(Icons.Filled.VpnKey, "Generovať heslo")
                    }
                }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Poznámky (voliteľné)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        val updatedItem = initialPassword!!.copy(
                            name = title,
                            username = username.ifBlank { null },
                            password = password,
                            notes = notes.ifBlank { null }
                        )
                        viewModel.updatePassword(updatedItem)
                    } else {
                        viewModel.addPassword(title, username.ifBlank { null }, password, notes.ifBlank { null })
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && password.isNotBlank()
            ) {
                Text("Uložiť")
            }
        }
    }
}
