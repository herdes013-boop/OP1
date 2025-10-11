package com.example.op

// ... (všetky existujúce importy zostávajú) ...
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


// ... (Funkcie getChannelIcon a ContactListItem zostávajú úplne nezmenené) ...
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
        onClick = onItemClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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


// =========================================================================
// ✅ HLAVNÁ OBRAZOVKA KONTAKTOV - PREPRACOVANÁ A ZJEDNODUŠENÁ
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val selectedTabFilter = viewModel.selectedTabFilter
    val categories = viewModel.channelOptions.toList()

    val ALL_CHANNELS_FILTER = "Všetky"
    val selectedTabIndex = categories.indexOf(selectedTabFilter)

    // Hlavný stĺpec, ktorý drží záložky a obsah pod nimi
    Column(modifier = modifier.fillMaxSize()) {

        // 1. ZÁLOŽKY (TabRow)
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
                    onClick = { viewModel.updateSelectedTabFilter(originalTitle) },
                    text = { Text(displayTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        // Box, ktorý obsahuje zvyšok obrazovky a FloatingActionButton
        Box(modifier = Modifier.weight(1f)) {

            // 2. OBSAH POD ZÁLOŽKAMI - tu sa prepína zobrazenie
            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                // Zobrazenie pre záložku "Všetky"
                AllContactsView(
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Zobrazenie pre konkrétny kanál (napr. ":1")
                ChannelDetailView(
                    channelName = selectedTabFilter,
                    functions = viewModel.channelFunctions,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. FloatingActionButton
            FloatingActionButton(
                onClick = {
                    // TODO: Upraviť logiku - ak sme na kanáli, FAB by mal robiť niečo iné
                    viewModel.resetForm()
                    navController.navigate(Routes.ADD_CONTACT)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pridať")
            }
        }
    }
}


// =========================================================================
// ✅ POMOCNÉ FUNKCIE PRE JEDNOTLIVÉ ZOBRAZENIA
// =========================================================================

/**
 * Zobrazí obsah pre záložku "Všetky" (vyhľadávanie a zoznam kontaktov).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllContactsView(
    viewModel: ContactsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val searchQuery = viewModel.searchQuery
    val contacts = viewModel.displayedContacts

    Column(modifier = modifier) {
        // Riadok s vyhľadávaním
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Vyhľadať...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vymazať text")
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = Color.White,
                    dividerColor = Color.Transparent
                )
            ) {}
        }

        // Zoznam kontaktov alebo správa o prázdnom stave
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Nenašli sa žiadne kontakty pre \"$searchQuery\"." else "Zatiaľ žiadne kontakty.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 80.dp)
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

/**
 * Zobrazí obsah pre záložku konkrétneho kanála (napr. ":1").
 */
@Composable
private fun ChannelDetailView(
    channelName: String,
    functions: List<ChannelFunction>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(bottom = 80.dp)) { // Padding pre FAB
        // 1. Hlavička s názvom kanála
        item {
            Text(
                text = when (channelName) {
                    "Jednotka" -> ":1"
                    "Dvojka" -> ":2"
                    "24" -> ":24"
                    "Sport" -> ":Sport"
                    else -> channelName
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        }

        // 2. Zoznam funkcií a priradených osôb
        if (functions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Pre tento kanál zatiaľ nie sú definované žiadne funkcie.",
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(functions, key = { it.id }) { function ->
                ChannelFunctionSection(function = function)
            }
        }
    }
}

/**
 * Zobrazí jednu sekciu funkcie (napr. "Kameramani") a zoznam jej ľudí.
 */
@Composable
private fun ChannelFunctionSection(function: ChannelFunction) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Názov funkcie
        Text(
            text = function.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            fontWeight = FontWeight.Bold
        )
        // Zoznam priradených ľudí
        if (function.assignedPeople.isEmpty()) {
            Text(
                text = "Nikto nie je priradený",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, end = 16.dp)
            )
        } else {
            function.assignedPeople.forEach { person ->
                AssignedPersonRow(person = person)
            }
        }
    }
}

/**
 * Zobrazí jednu osobu priradenú k funkcii.
 */
@Composable
private fun AssignedPersonRow(person: AssignedPerson) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stĺpec pre Meno a Telefón
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            person.phone?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        // Poznámky napravo
        if (person.notes.isNotBlank()) {
            Spacer(Modifier.width(16.dp))
            Text(
                text = person.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.End
            )
        }
    }
}
