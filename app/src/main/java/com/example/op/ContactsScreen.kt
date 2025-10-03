package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel

// ----------------------------------------------------------------------
// POZNÁMKA: DEFINÍCIE triedy ContactItem a objektu Routes boli odstránené,
// pretože spôsobujú redeklaráciu s existujúcimi súbormi (ContactItem.kt).
// Aplikácia sa teraz spolieha na ich existenciu v rámci balíčka 'com.example.op'.
// ----------------------------------------------------------------------

// --------------------------------------------------
// Pomocné komponenty a funkcie
// --------------------------------------------------

/**
 * Pomocná funkcia na získanie ikony na základe kanálu.
 */
fun getChannelIcon(channel: String?): ImageVector {
    // Mapujeme kanály z ViewModelu na konkrétne Material ikony
    return when (channel) {
        "Jednotka" -> Icons.Filled.LooksOne
        "Dvojka" -> Icons.Filled.LooksTwo
        "24" -> Icons.Filled.Newspaper
        "Sport" -> Icons.Filled.SportsSoccer
        "Iné" -> Icons.Filled.OtherHouses
        else -> Icons.Filled.Person // Predvolená ikona
    }
}

/**
 * Komponent na zobrazenie kontaktu v zozname.
 */
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
            // Ikona kanálu
            Icon(
                imageVector = getChannelIcon(contact.channel),
                contentDescription = contact.channel ?: "Kontakt",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(16.dp))

            // Detaily kontaktu
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    // Používame pomocnú funkciu getFullName() - predpokladáme, že je v ContactItem
                    text = contact.getFullName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                contact.function?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
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
    // Použijeme predvolenú inštanciu ViewModel
    viewModel: ContactsViewModel = viewModel()
) {
    // Čítanie stavov z ViewModelu.
    val searchQuery = viewModel.searchQuery
    val selectedTabFilter = viewModel.selectedTabFilter
    val ALL_CHANNELS_FILTER = "Všetky" // Konštanta pre jasné porovnanie

    // Zobrazené kontakty
    val contacts = viewModel.displayedContacts

    // Definovanie záložiek (Získavame z ViewModelu)
    val categories = viewModel.channelOptions.toList()
    // Zistenie aktuálneho indexu pre správne zobrazenie záložky
    val selectedTabIndex = categories.indexOf(selectedTabFilter)

    Scaffold(
        topBar = {
            Column {
                // 1. TopAppBar (Hlavička)
                TopAppBar(
                    title = { Text("Kontakty") },
                    actions = {
                        // Navigácia na správu kanálov
                        IconButton(onClick = { navController.navigate(Routes.MANAGE_CHANNELS) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Správa Kanálov")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                // 2. ScrollableTabRow (Záložky)
                ScrollableTabRow(
                    selectedTabIndex = if (selectedTabIndex == -1) 0 else selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    categories.forEach { title ->
                        Tab(
                            selected = selectedTabFilter == title,
                            onClick = {
                                // Ak odchádzame zo záložky "Všetky", vymažeme vyhľadávanie
                                if (selectedTabFilter == ALL_CHANNELS_FILTER && title != ALL_CHANNELS_FILTER) {
                                    viewModel.updateSearchQuery("")
                                }
                                // Aktualizácia filtra vo ViewModel
                                viewModel.updateSelectedTabFilter(title)
                            },
                            text = { Text(title) },
                            unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Pred navigáciou resetujeme formulár
                    viewModel.resetForm()
                    navController.navigate(Routes.ADD_CONTACT)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pridať kontakt")
            }
        }
    ) { paddingValues ->
        // Obsah obrazovky pod TopBarom a TabRow
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 3. VYHĽADÁVACIE OKNO - Zobrazuje sa IBA, ak je vybrané "Všetky"
            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Vyhľadať v kontaktoch...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Vyhľadať")
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Vymazať vyhľadávanie")
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            // ------------------------------------------------------------------


            // 4. Zoznam kontaktov
            if (contacts.isEmpty()) {
                val message = if (selectedTabFilter == ALL_CHANNELS_FILTER && searchQuery.isNotBlank()) {
                    "Nenašli sa žiadne kontakty pre vyhľadávanie \"$searchQuery\"."
                } else if (selectedTabFilter != ALL_CHANNELS_FILTER) {
                    "V kanáli \"$selectedTabFilter\" zatiaľ nie sú žiadne kontakty."
                } else {
                    "Zatiaľ nemáte žiadne kontakty."
                }
                Text(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            onItemClick = {
                                navController.navigate(Routes.editContact(contact.id))
                            }
                        )
                    }
                }
            }
        }
    }
}
