package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign

// --------------------------------------------------
// Komponenty na Zobrazenie Kontaktu (presunuté z DataModels.kt)
// --------------------------------------------------

// Pomocná funkcia na získanie ikony
fun getChannelIcon(channel: String?): ImageVector {
    return when (channel) {
        "Telefón" -> Icons.Filled.Phone
        "Email" -> Icons.Filled.Email
        "Sociálne siete" -> Icons.Filled.Chat
        "Osobný kontakt" -> Icons.Filled.Person
        else -> Icons.Filled.Person
    }
}

// Komponent na zobrazenie kontaktu v zozname
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListItem(contact: ContactItem, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikona
            Icon(
                imageVector = getChannelIcon(contact.channel),
                contentDescription = contact.channel ?: "Kontakt",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(16.dp))

            // Detaily kontaktu
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${contact.firstName.orEmpty()} ${contact.lastName.orEmpty()}",
                    style = MaterialTheme.typography.titleMedium
                )
                contact.function?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// Hlavná obrazovka pre kontakty
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel
) {
    // ✅ OPRAVA: Odstránené .collectAsState(), pretože displayedContacts je Compose State
    val contacts = viewModel.displayedContacts

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kontakty") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Pred navigáciou resetujeme formulár, aby bol pripravený na nový kontakt
                    viewModel.resetForm()
                    navController.navigate(Routes.ADD_CONTACT)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pridať kontakt")
            }
        }
    ) { paddingValues ->
        // Obsah obrazovky pod TopBarom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dropdown pre filtrovanie kanálov
            ContactChannelFilter(viewModel = viewModel)

            // 2. Kontrola, či je zoznam prázdny (použitím 'contacts' typu List)
            if (contacts.isEmpty()) {
                Text(
                    text = if (viewModel.selectedChannel == "Všetky") "Zatiaľ nemáte uložené žiadne kontakty."
                    else "Žiadne kontakty pre kanál '${viewModel.selectedChannel}'.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    // Centrujeme text, ak je prázdny
                    textAlign = TextAlign.Center // Opravený import
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Používame 'contacts' (List)
                    items(contacts, key = { it.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            onItemClick = {
                                // ID je Int, voláme funkciu
                                navController.navigate(Routes.editContact(contact.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// Komponent pre Filter (Dropdown)
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactChannelFilter(viewModel: ContactsViewModel) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            readOnly = true,
            value = viewModel.selectedChannel,
            onValueChange = { },
            label = { Text("Filter Kanálov") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Používame channelOptions z ViewModelu
            viewModel.channelOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        viewModel.updateSelectedChannel(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}
