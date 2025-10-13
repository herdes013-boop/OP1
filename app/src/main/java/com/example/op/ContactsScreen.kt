package com.example.op

// ... (všetky importy zostávajú) ...
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.runtime.getValue

// --- Tieto funkcie zostávajú bez zmeny ---
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
    // ✅ ÚPRAVA PRE ZJEDNOTENIE VZHĽADU
    Card(
        modifier = Modifier.fillMaxWidth(), // Odstránili sme vertikálny padding priamo tu
        onClick = onItemClick,
        // Použijeme rovnaké farby a tieň ako inde v aplikácii
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
// ✅ HLAVNÁ OBRAZOVKA KONTAKTOV - OPRAVENÁ
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier,
) {
    val selectedTabFilter by viewModel.selectedTabFilter.collectAsState()
    val categories = viewModel.channelOptions.toList()
    val ALL_CHANNELS_FILTER = "Všetky"
    val selectedTabIndex = categories.indexOf(selectedTabFilter)
    val isEditMode = viewModel.isEditMode

    // Tento blok sa stará o správne zobrazenie hornej lišty
    LaunchedEffect(selectedTabFilter, isEditMode) {
        if (selectedTabFilter == ALL_CHANNELS_FILTER) {
            // Pre záložku "Všetky"
            sharedViewModel.setTopBarState(TopBarState(title = "Kontakty", actions = {}))
        } else {
            // Pre ostatné záložky
            val channelDisplayName = when (selectedTabFilter) {
                "Jednotka" -> ":1"
                "Dvojka" -> ":2"
                "24" -> ":24"
                "Sport" -> ":Sport"
                else -> selectedTabFilter
            }
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = channelDisplayName,
                    actions = {
                        TextButton(onClick = { viewModel.toggleEditMode() }) {
                            Text(
                                text = if (isEditMode) "Hotovo" else "Upraviť",
                                color = Color.White // Týmto povieme, že text má byť biely
                            )
                        }
                    }
                )
            )
        }
    }



    Column(modifier = modifier.fillMaxSize()) {
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

        Box(modifier = Modifier.weight(1f)) {

            // Tento riadok musí byť TU, vo vnútri Box-u
            val channelFunctions by viewModel.channelFunctions.collectAsState()

            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                AllContactsView(
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ChannelDetailView(
                    channelName = selectedTabFilter,
                    functions = channelFunctions,
                    isEditMode = isEditMode,
                    modifier = Modifier.fillMaxSize(),
                    // ✅ FINÁLNE PREPOJENIE S VIEWMODELOM
                    onAddFunction = { nazov -> viewModel.addChannelFunction(nazov) },
                    onDeleteFunction = { idFunkcie -> viewModel.removeChannelFunction(idFunkcie) }
                )
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
                Icon(Icons.Default.Add, contentDescription = "Pridať")
            }
        }
    }
}


// =========================================================================
// ✅ POMOCNÉ FUNKCIE - KOMPLETNÉ A OPRAVENÉ
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllContactsView(
    viewModel: ContactsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val searchQuery = viewModel.searchQuery
    val contacts = viewModel.displayedContacts

    Column(modifier = modifier) {
        SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    // Aplikujeme padding a offset priamo na SearchBar
                    .padding(top = 4.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                    .offset(y = (-18).dp),
                placeholder = { Text("Vyhľadať v kontaktoch...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
            trailingIcon = {
                // ✅ ÚPRAVA: Pridaná logika pre zobrazenie ikony filtra
                if (searchQuery.isNotEmpty()) {
                    // Ak sa vyhľadáva, zobraz krížik na zmazanie
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Vymazať text", tint = Color.Black)
                    }
                } else {
                    // Ak je vyhľadávanie prázdne, zobraz ikonu filtra
                    IconButton(onClick = { /* TODO: Otvoriť dialóg s filtrami */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtrovať zoznam", tint = Color.Black)
                    }
                }
            },
                colors = SearchBarDefaults.colors(
                    containerColor = Color.White,
                    dividerColor = Color.Transparent
                )
            ) {}


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
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // ✅ DOPLŇTE TENTO RIADOK
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

@Composable
private fun ChannelDetailView(
    channelName: String, functions: List<ChannelFunction>,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    // Tieto parametre chýbali, teraz ich pridávame:
    onAddFunction: (String) -> Unit,
    onDeleteFunction: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp), // Padding pre celý zoznam
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp), // Priestor hore a dole
        verticalArrangement = Arrangement.spacedBy(8.dp) // Medzera medzi bublinami
    ) {
        // Farebný nadpis kanála
        item {
            val titleColor = when (channelName) {
                "Jednotka" -> com.example.op.ui.theme.TelekomMagenta
                "Sport" -> Color(0xFFE64A19)
                "24" -> Color(0xFF1976D2)
                else -> Color.Unspecified
            }
            Text(
                text = when (channelName) {
                    "Jednotka" -> ":1"
                    "Dvojka" -> ":2"
                    "24" -> ":24"
                    "Sport" -> ":Sport"
                    else -> channelName
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                color = titleColor
            )
        }

        // Zobrazenie bublín alebo prázdnej správy
        if (functions.isEmpty() && !isEditMode) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pre tento kanál zatiaľ nie sú definované žiadne funkcie.", textAlign = TextAlign.Center)
                }
            }
        } else {
            // Pre každú funkciu zobrazíme našu novú bublinu
            items(functions, key = { it.id }) { function ->
                ChannelFunctionCard(
                    function = function,
                    isEditMode = isEditMode,
                    // Teraz voláme onDelete, ktoré sme dostali ako parameter
                    onDelete = { onDeleteFunction(function.id) }
                )
            }
        }

        // Tlačidlo "Pridať novú funkciu" na konci
        if (isEditMode) {
            item {
                TextButton(
                    // Teraz voláme onAddFunction, ktoré sme dostali ako parameter
                    onClick = { onAddFunction("Nová funkcia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pridať novú funkciu")
                }
            }
        }
    }
}


@Composable
private fun AssignedPersonRow(
    person: AssignedPerson,
    isEditMode: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Zmenšíme padding, aby sa poznámka lepšie zmestila a nebola ďaleko
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stĺpec s menom a telefónom
        Column(
            // Dáme mu váhu, aby sa roztiahol, ale nechal miesto pre poznámku/tlačidlo
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            // Telefónne číslo zobrazíme, len ak existuje
            person.phone?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Zobrazenie buď poznámky alebo tlačidla na zmazanie
        if (isEditMode) {
            // V editačnom móde ukážeme tlačidlo na zmazanie
            IconButton(onClick = { /* TODO: Zmazať osobu */ }) {
                Icon(Icons.Default.Clear, contentDescription = "Zmazať osobu", tint = Color.LightGray)
            }
        } else if (person.notes.isNotBlank()) {
            // Mimo editačného módu, ak existuje poznámka, ukážeme ju
            Spacer(Modifier.width(16.dp)) // Medzera medzi menom a poznámkou
            Text(
                text = person.notes,
                style = MaterialTheme.typography.bodyMedium, // Trochu zväčšíme text pre lepšiu čitateľnosť
                color = MaterialTheme.colorScheme.primary, // Zvýrazníme farbu
                textAlign = TextAlign.End,
                maxLines = 2, // Povolíme maximálne 2 riadky
                overflow = TextOverflow.Ellipsis // Ak je text dlhší, zobrazia sa ...
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelFunctionCard(
    function: ChannelFunction,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    // Pridávame tento parameter:
    onDelete: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = function.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                if (isEditMode) {
                    // Upravujeme onClick, aby volalo parameter
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Clear, "Zmazať funkciu", tint = Color.Gray)
                    }
                }
            }

            if (function.assignedPeople.isEmpty() && !isEditMode) {
                Text(
                    text = "Nikto nie je priradený",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            } else {
                function.assignedPeople.forEach { person ->
                    AssignedPersonRow(
                        person = person,
                        isEditMode = isEditMode
                    )
                }
            }

            if (isEditMode) {
                TextButton(
                    onClick = { /* TODO: Pridať osobu */ },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pridať osobu")
                }
            }
        }
    }
}