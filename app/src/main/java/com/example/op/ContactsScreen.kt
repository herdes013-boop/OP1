package com.example.op

// ... (všetky importy zostávajú) ...
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontStyle
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

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
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var selectedFunctionForDialog by remember { mutableStateOf<ChannelFunction?>(null) }
    // ✅ KONIEC: Nový stav pre dialóg

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
                    viewModel = viewModel,
                    channelName = selectedTabFilter,
                    functions = channelFunctions,
                    isEditMode = isEditMode,
                    modifier = Modifier.fillMaxSize(),
                    onAddFunction = { nazov -> viewModel.addChannelFunction(nazov) },
                    onDeleteFunction = { idFunkcie -> viewModel.removeChannelFunction(idFunkcie) },
                    onMoveFunction = { from, to -> viewModel.moveChannelFunction(from, to) },
                    // ✅ TENTO RIADOK SEM PRIDAJTE
                    onAddPersonClick = { function ->
                        selectedFunctionForDialog = function
                        showAddPersonDialog = true
                    },
                    // ✅ TENTO RIADOK SEM PRIDAJTE
                    onRemovePersonClick = { functionId, personId ->
                        viewModel.removePersonFromFunction(functionId, personId)
                    },
                    // ✅ TENTO RIADOK SEM PRIDAJTE
                    onUpdatePersonNote = { functionId, personId, newNote ->
                        viewModel.updatePersonNoteInFunction(functionId, personId, newNote)
                    }
                )
            }

            if (showAddPersonDialog && selectedFunctionForDialog != null) {
                val assignedIds = selectedFunctionForDialog!!.assignedPeople.map { it.contactId }
                AddPersonToFunctionDialog(
                    allContacts = viewModel.contacts,
                    assignedPeopleIds = assignedIds,
                    onDismiss = { showAddPersonDialog = false },
                    onPersonSelected = { contact ->
                        viewModel.assignPersonToFunction(selectedFunctionForDialog!!.id, contact)
                        showAddPersonDialog = false // Po výbere dialóg zatvoríme
                    }
                )
            }
            // ✅ KONIEC: Kód na zobrazenie dialógu

            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
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
            } // Koniec `if` bloku - správne

        } // Koniec `Box`
    } // Koniec `Column`
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelDetailView(
    viewModel: ContactsViewModel,
    channelName: String,
    functions: List<ChannelFunction>,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onAddFunction: (String) -> Unit,
    onDeleteFunction: (String) -> Unit,
    onMoveFunction: (Int, Int) -> Unit,
    onAddPersonClick: (ChannelFunction) -> Unit,
    // ✅ TENTO RIADOK SEM PRIDAJTE
    onRemovePersonClick: (functionId: String, personId: String) -> Unit,
    onUpdatePersonNote: (functionId: String, personId: String, newNote: String) -> Unit,
) {
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (from.index > 0 && to.index > 0) {
                onMoveFunction(from.index - 1, to.index - 1)
            }
        }
    )

    LazyColumn(
        state = reorderState.listState,
        modifier = modifier
            .fillMaxSize()
            .reorderable(reorderState),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. HLAVIČKA KANÁLA
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

        // 2. ZOZNAM FUNKCIÍ
        if (functions.isEmpty() && !isEditMode) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("Pre tento kanál zatiaľ nie sú definované žiadne funkcie.", textAlign = TextAlign.Center, color = Color.Gray)
                    }
                }
            }
        } else {
            items(items = functions, key = { it.id }) { function ->
                val onUpdateFunction = { newTitle: String, newNotes: String? ->
                    viewModel.updateChannelFunction(function.id, newTitle, newNotes)
                }
                val onDeleteFunctionLambda = { onDeleteFunction(function.id) }

                ReorderableItem(
                    reorderableState = reorderState,
                    key = function.id
                ) {
                    ChannelFunctionCard(
                        function = function,
                        isEditMode = isEditMode,
                        onDelete = onDeleteFunctionLambda,
                        onUpdate = onUpdateFunction,
                        onAddPersonClick = { onAddPersonClick(function) },
                        // ✅ TENTO RIADOK SEM PRIDAJTE
                        onRemovePerson = { personId ->
                            onRemovePersonClick(function.id, personId)
                        },
                        // ✅ TENTO RIADOK SEM PRIDAJTE
                        onUpdatePersonNote = { personId, newNote ->
                            onUpdatePersonNote(function.id, personId, newNote)
                        },
                        modifier = if (isEditMode) {
                            Modifier
                                .detectReorderAfterLongPress(reorderState)
                                .animateItemPlacement()
                        } else {
                            Modifier.animateItemPlacement()
                        }
                    )
                }
            }
        }

        // 3. TLAČIDLO "PRIDAŤ FUNKCIU"
        if (isEditMode) {
            item {
                TextButton(
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelFunctionCard(
    function: ChannelFunction,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onUpdate: (title: String, notes: String?) -> Unit,
    onAddPersonClick: () -> Unit,
    // ✅ TENTO RIADOK SEM PRIDAJTE
    onRemovePerson: (personId: String) -> Unit,
    // ✅ TENTO RIADOK SEM PRIDAJTE
    onUpdatePersonNote: (personId: String, newNote: String) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // --- HORNÁ ČASŤ S NÁZVOM A TLAČIDLOM ZMAZAŤ ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp)
            ) {
                if (isEditMode) {
                    BasicTextField(
                        value = function.title,
                        onValueChange = { newTitle -> onUpdate(newTitle, function.notes) },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    )
                } else {
                    Text(
                        text = function.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    )
                }

                if (isEditMode) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Clear, "Zmazať funkciu", tint = Color.Gray)
                    }
                }
            }

            // --- POZNÁMKY K FUNKCII ---
            if (isEditMode) {
                BasicTextField(
                    value = function.notes ?: "",
                    onValueChange = { newNotes -> onUpdate(function.title, newNotes) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    decorationBox = { innerTextField ->
                        if (function.notes.isNullOrBlank()) {
                            Text(
                                "Pridať poznámku...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                )
            } else {
                function.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
                    )
                }
            }

            // --- SEPARATOR (ČIARA) ---
            if (function.assignedPeople.isNotEmpty()) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // --- ZOZNAM PRIRADENÝCH OSÔB ---
            if (function.assignedPeople.isEmpty() && !isEditMode) {
                Text(
                    text = "Nikto nie je priradený",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp)
                )
            } else {
                function.assignedPeople.forEach { person ->
                    AssignedPersonRow(
                        person = person,
                        isEditMode = isEditMode,
                        // ✅ TENTO RIADOK SEM PRIDAJTE
                        onRemoveClick = { onRemovePerson(person.id) },
                        // ✅ TENTO RIADOK SEM PRIDAJTE
                        onNoteChange = { newNote ->
                            onUpdatePersonNote(person.id, newNote)
                        }
                    )
                }
            }

            // --- TLAČIDLO "PRIDAŤ OSOBU" V EDITAČNOM MÓDE ---
            if (isEditMode) {
                TextButton(
                    onClick = onAddPersonClick, // Použijeme parameter
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pridať osobu")
                }
            }
        }
    }
}

@Composable
private fun AssignedPersonRow(
    person: AssignedPerson,
    isEditMode: Boolean,
    onRemoveClick: () -> Unit, onNoteChange: (String) -> Unit, // Nový parameter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp), // Zmenšený vertikálny padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stĺpec s menom a telefónom (zostáva bez zmeny)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            person.phone?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // ✅ ZAČIATOK ÚPRAV: Logika pre poznámku alebo tlačidlo
        if (isEditMode) {
            // V editačnom móde je tu TextField a vedľa neho tlačidlo na mazanie
            BasicTextField(
                value = person.notes,
                onValueChange = onNoteChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (person.notes.isBlank()) {
                            Text(
                                "Poznámka...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.End
                            )
                        }
                        innerTextField()
                    }
                }
            )
            IconButton(onClick = onRemoveClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    contentDescription = "Zmazať osobu",
                    tint = Color.LightGray
                )
            }
        } else {
            // V normálnom móde je tu len text poznámky (ak existuje)
            person.notes.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        // ✅ KONIEC ÚPRAV
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Uistite sa, že máte tento OptIn
@Composable
private fun AddPersonToFunctionDialog(
    allContacts: List<ContactItem>,
    assignedPeopleIds: List<String>,
    onDismiss: () -> Unit,
    onPersonSelected: (ContactItem) -> Unit,
) {
    // ✅ KROK 1: Pridáme lokálny stav pre vyhľadávací text
    var searchQuery by remember { mutableStateOf("") }

    // Zobrazíme iba tie kontakty, ktoré ešte nie sú v danej funkcii priradené
    val availableContacts = allContacts.filter { contact ->
        contact.id.toString() !in assignedPeopleIds
    }

    // ✅ KROK 2: Filtrujeme dostupné kontakty na základe vyhľadávania
    val filteredContacts = if (searchQuery.isBlank()) {
        availableContacts
    } else {
        availableContacts.filter {
            it.getFullName().contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať osobu") },
        text = {
            // ✅ KROK 3: Vložíme Column, aby sme mohli mať TextField aj zoznam pod sebou
            Column {
                // Vyhľadávacie pole
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    placeholder = { Text("Vyhľadať kontakt...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazať")
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        // Použijeme štandardné farby
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    )
                )

                // Zoznam kontaktov
                if (filteredContacts.isEmpty()) {
                    Text(
                        if (searchQuery.isNotBlank()) "Nenašiel sa žiadny zodpovedajúci kontakt."
                        else "Všetky dostupné kontakty sú už priradené."
                    )
                } else {
                    LazyColumn {
                        // Použijeme už prefiltrovaný zoznam
                        items(filteredContacts, key = { it.id }) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPersonSelected(contact) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getChannelIcon(contact.channel),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(contact.getFullName(), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}