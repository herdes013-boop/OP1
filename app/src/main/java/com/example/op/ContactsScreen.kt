package com.example.op

// Správne importy pre layout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Správne importy pre Material 3 komponenty
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text

// Správne importy pre stav a kompozíciu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Správne importy pre ViewModel a Navigáciu
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.TextButton

// (Zvyšok vášho kódu zostáva nezmenený)


// Funkcie getChannelIcon a ContactListItem zostávajú úplne bez zmeny
fun getChannelIcon(channel: String?): ImageVector {
    return when (channel) {
        "Jednotka" -> Icons.Filled.LooksOne
        "Dvojka" -> Icons.Filled.LooksTwo
        "24" -> Icons.Filled.Newspaper
        "Sport" -> Icons.Filled.SportsSoccer
        else -> Icons.Filled.Person
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
// Hlavná obrazovka pre kontakty - S POUŽITÍM SCAFFOLD
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val searchQuery = viewModel.searchQuery
    val selectedTabFilter = viewModel.selectedTabFilter
    val contacts = viewModel.displayedContacts
    val categories = viewModel.channelOptions.toList()

    val ALL_CHANNELS_FILTER = "Všetky"
    val selectedTabIndex = categories.indexOf(selectedTabFilter)

    // ✅ ZMENA: Celý obsah obrazovky dáme do jedného Columnu.
    Column(modifier = modifier.fillMaxSize()) {

        // 1. ZÁLOŽKY - sú hneď na vrchu, bez paddingu
        TabRow(selectedTabIndex = if (selectedTabIndex == -1) 0 else selectedTabIndex) {
            categories.forEach { originalTitle ->
                val displayTitle = when (originalTitle) {
                    "Jednotka" -> ":1"
                    "Dvojka" -> ":2"
                    "24" -> ":24"
                    "Sport" -> ":Sport"
                    else -> originalTitle
                }
                Tab(
                    selected = selectedTabFilter == originalTitle,
                    onClick = {
                        if (selectedTabFilter == ALL_CHANNELS_FILTER && originalTitle != ALL_CHANNELS_FILTER) {
                            viewModel.updateSearchQuery("")
                        }
                        viewModel.updateSelectedTabFilter(originalTitle)
                    },
                    text = { Text(displayTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        // 2. ZVYŠOK OBSAHU - tento je zabalený do Scaffold-u
        Scaffold(
            // Odstránime horný padding, lebo ten sme už vyriešili umiestnením záložiek
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    viewModel.resetForm()
                    navController.navigate(Routes.ADD_CONTACT)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Pridať kontakt")
                }
            }
        ) { innerPadding ->
            // Column pre zvyšný obsah, ktorý už POUŽÍVA innerPadding
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {

                // SearchBar
                if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                    // Riadok, ktorý bude držať SearchBar aj tlačidlo Filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // SearchBar zaberie väčšinu miesta
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onSearch = { /* Hľadá sa priebežne */ },
                            active = false,
                            onActiveChange = {},
                            modifier = Modifier.weight(1f), // <-- Dôležité: zaberie všetok dostupný priestor
                            placeholder = { Text("Vyhľadať...") }, // Skrátený text pre viac miesta
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Vymazať text")
                                    }
                                }
                            }
                        ) {}

                        // Medzera medzi SearchBar a tlačidlom
                        Spacer(Modifier.width(8.dp))

                        // Naše nové tlačidlo "Filter"
                        TextButton(onClick = { /* Zatiaľ nerobí nič */ }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null, // Popis bude v texte
                                modifier = Modifier.size(18.dp) // Menšia ikona, aby sa zmestila
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Filter")
                        }
                    }
                }

                // Zoznam alebo správa o prázdnom stave
                if (contacts.isEmpty()) {
                    val message = if (selectedTabFilter == ALL_CHANNELS_FILTER && searchQuery.isNotBlank()) {
                        "Nenašli sa žiadne kontakty pre vyhľadávanie \"$searchQuery\"."
                    } else if (selectedTabFilter != ALL_CHANNELS_FILTER) {
                        "V kanáli \"$selectedTabFilter\" zatiaľ nie sú žiadne kontakty."
                    } else {
                        "Zatiaľ nemáte žiadne kontakty."
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp)
                    ) {
                        items(contacts, key = { it.id }) { contact ->
                            ContactListItem(
                                contact = contact,
                                onItemClick = { navController.navigate("contact_detail/${contact.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}
