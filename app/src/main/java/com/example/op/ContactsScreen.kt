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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log


// ... (getChannelIcon a ContactListItem ostávajú bez zmeny)
fun getChannelIcon(channel: String?): ImageVector {
    return when (channel) {
        "Jednotka" -> Icons.Filled.LooksOne
        "Dvojka" -> Icons.Filled.LooksTwo
        "24" -> Icons.Filled.Newspaper // Pridaná ikona pre "24"
        "Sport" -> Icons.Filled.SportsSoccer
        else -> Icons.Filled.Person // "Iné" bolo odstránené, "else" pokryje ostatné prípady
    }
}

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
            Icon(
                imageVector = getChannelIcon(contact.channel),
                contentDescription = contact.channel ?: "Kontakt",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
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
// Hlavná obrazovka pre kontakty - S OPRAVOU
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val searchQuery = viewModel.searchQuery
    val selectedTabFilter = viewModel.selectedTabFilter
    val ALL_CHANNELS_FILTER = "Všetky"
    val contacts = viewModel.displayedContacts
    val categories = viewModel.channelOptions.toList()
    val selectedTabIndex = categories.indexOf(selectedTabFilter)

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // =========== TOTO JE JEDINÁ ZMENA, KTORÚ POTREBUJETE ===========
            TabRow(
                selectedTabIndex = if (selectedTabIndex == -1) 0 else selectedTabIndex,
            ) {
                categories.forEach { originalTitle -> // Premenoval som premennú pre jasnosť

                    // "Preložíme" pôvodný názov na taký, ktorý chceme zobraziť
                    val displayTitle = when (originalTitle) {
                        "Jednotka" -> ":1"
                        "Dvojka" -> ":2"
                        "24" -> ":24"
                        "Sport" -> ":Sport"
                        else -> originalTitle // "Všetky" a iné zostanú nezmenené
                    }

                    Tab(
                        // Logika stále pracuje s pôvodným názvom (originalTitle)
                        selected = selectedTabFilter == originalTitle,
                        onClick = {
                            if (selectedTabFilter == ALL_CHANNELS_FILTER && originalTitle != ALL_CHANNELS_FILTER) {
                                viewModel.updateSearchQuery("")
                            }
                            viewModel.updateSelectedTabFilter(originalTitle)
                        },
                        text = {
                            Text(
                                text = displayTitle, // Tu zobrazíme náš nový, skrátený názov
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
            }
            // ======================= KONIEC ZMENY =======================

            // Zvyšok kódu (SearchBar, LazyColumn, atď.) zostáva úplne bez zmeny
            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { /* Hľadá sa priebežne */ },
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Vyhľadať v kontaktoch...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ikona vyhľadávania") },
                    trailingIcon = {
                        // Podmienené zobrazenie ikony "X"
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazať text")
                            }
                        }
                    }
                ) {
                    // Prázdny obsahový blok, ktorý je pre SearchBar povinný
                }
            }

            if (contacts.isEmpty()) {
                val message = if (selectedTabFilter == ALL_CHANNELS_FILTER && searchQuery.isNotBlank()) {
                    "Nenašli sa žiadne kontakty pre vyhľadávanie \"$searchQuery\"."
                } else if (selectedTabFilter != ALL_CHANNELS_FILTER) {
                    "V kanáli \"$selectedTabFilter\" zatiaľ nie sú žiadne kontakty."
                } else {
                    "Zatiaľ nemáte žiadne kontakty."
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 4.dp,
                        bottom = 80.dp // Padding pre FAB
                    )
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            onItemClick = {
                                // ✅✅✅ PRIDAJTE TIETO 2 RIADKY ✅✅✅
                                val route = "contact_detail/${contact.id}"
                                Log.d("NAV_TEST", "Pokus o navigáciu na:$route")

                                // Pôvodný kód
                                navController.navigate(route)
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.resetForm()
                navController.navigate(Routes.ADD_CONTACT)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Pridať kontakt")
        }
    }
}
