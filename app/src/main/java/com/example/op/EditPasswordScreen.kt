package com.example.op

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPasswordScreen(
    navController: NavController,
    passwordId: String, // <-- OPRAVA 1: ID je teraz String, nie Int
    viewModel: PasswordsViewModel = viewModel()
) {
    // Načítanie aktuálneho hesla pomocou String ID
    val initialPassword = viewModel.getPasswordById(passwordId)

    // Ak heslo neexistuje, vrátime sa späť
    if (initialPassword == null) {
        // Zabezpečenie, aby sa kód nespustil, ak je heslo null.
        // `LaunchedEffect` by bol ešte bezpečnejší, ale pre teraz stačí toto.
        navController.popBackStack()
        return
    }

    // Stavové premenné pre editáciu
    // OPRAVA 2: Používame správne názvy parametrov z PasswordItem (name, password)
    var title by remember { mutableStateOf(initialPassword.name) }
    var username by remember { mutableStateOf(initialPassword.username ?: "") }
    var password by remember { mutableStateOf(initialPassword.password) }
    var notes by remember { mutableStateOf(initialPassword.notes ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upraviť heslo", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Späť"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Polia pre zadávanie textu (bez zmien)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Názov služby") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Používateľské meno / E-mail") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Heslo") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        password = viewModel.generateRandomPassword()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.VpnKey,
                            contentDescription = "Generovať heslo"
                        )
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Poznámky (voliteľné)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // Tlačidlo Uložiť
            Button(
                onClick = {
                    // OPRAVA 3: Používame správne názvy parametrov v .copy()
                    val updatedItem = initialPassword.copy(
                        name = title,
                        username = username.ifBlank { null },
                        password = password,
                        notes = notes.ifBlank { null }
                    )
                    viewModel.updatePassword(updatedItem)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && password.isNotBlank()
            ) {
                Text("Uložiť zmeny")
            }
            Spacer(Modifier.height(8.dp))

            // Tlačidlo Vymazať
            Button(
                onClick = {
                    // OPRAVA 4: Voláme deletePassword so String ID
                    viewModel.deletePassword(passwordId)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vymazať heslo")
            }
        }
    }
}
