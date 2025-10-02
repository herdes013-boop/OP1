package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon // ✅ OPRAVENÝ IMPORT pre Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(
    navController: NavController,
    viewModel: PasswordsViewModel
) {
    // Sledujeme zoznam hesiel zo StateFlow vo ViewModele
    val passwords by viewModel.passwordList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Môj Správca Hesiel", style = MaterialTheme.typography.headlineMedium) }
            )
        },
        // Tlačidlo na pridanie nového hesla
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_PASSWORD) }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Pridať heslo"
                )
            }
        }
    ) { paddingValues ->
        if (passwords.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("Zatiaľ nemáte uložené žiadne heslá.")
                Text("Stlačte '+' pre pridanie.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                items(passwords) { item ->
                    PasswordCard(item = item, onClick = {
                        // Navigácia na obrazovku úpravy s ID hesla
                        navController.navigate(Routes.editPassword(item.id))
                    })
                }
            }
        }
    }
}

@Composable
fun PasswordCard(item: PasswordItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Názov služby
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // Používateľské meno
            item.username?.let {
                Text(
                    text = "Meno: $it",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            // Heslo (zobrazené podľa požiadavky)
            Text(
                text = "Heslo: ${item.passwordEncrypted}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // Poznámky (voliteľné)
            item.notes?.let {
                Text(
                    text = "Poznámky: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}