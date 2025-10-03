package com.example.op

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VpnKey // ✅ Import pre ikonu kľúča (generátor)
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
fun AddPasswordScreen(navController: NavController, viewModel: PasswordsViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pridať nové heslo", style = MaterialTheme.typography.titleLarge) },
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

            // 1. Názov služby
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Názov služby (napr. Google)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // 2. Používateľské meno
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Používateľské meno / E-mail") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // 3. Heslo (viditeľné + Generátor)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Heslo") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    // Tlačidlo, ktoré zavolá generátor hesiel z ViewModelu
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

            // 4. Poznámky
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
                    if (title.isNotBlank() && password.isNotBlank()) {
                        viewModel.addPassword(title, username.ifBlank { null }, password, notes.ifBlank { null })
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && password.isNotBlank()
            ) {
                Text("Uložiť heslo")
            }
        }
    }
}
