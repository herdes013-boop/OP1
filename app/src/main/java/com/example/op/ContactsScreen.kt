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
import androidx.compose.material3.Checkbox
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.lazy.itemsIndexed



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
    Card(
        modifier = Modifier.fillMaxWidth(), // Toto je už správne
        onClick = onItemClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp) // ✅ TOTO JE KĽÚČOVÁ ZMENA
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,modifier: Modifier = Modifier,
) {
    val selectedTabFilter by viewModel.selectedTabFilter.collectAsState()
    val categories = viewModel.channelOptions.toList()
    val ALL_CHANNELS_FILTER = "Všetky"
    val selectedTabIndex = categories.indexOf(selectedTabFilter)
    val isEditMode = viewModel.isEditMode
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var selectedFunctionForDialog by remember { mutableStateOf<ChannelFunction?>(null) }
    var showEditFunctionDialog by remember { mutableStateOf(false) }
    var functionToEdit by remember { mutableStateOf<ChannelFunction?>(null) }

    LaunchedEffect(selectedTabFilter, isEditMode) {
        if (selectedTabFilter == ALL_CHANNELS_FILTER) {
            sharedViewModel.setTopBarState(TopBarState(title = "Kontakty", actions = {}))
        } else {
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
                            Text(text = if (isEditMode) "Hotovo" else "Upraviť", color = Color.White)
                        }
                    }
                )
            )
        }
    }

    // =========================================================================
    // ✅ KĽÚČOVÁ ZMENA: Aplikujeme rovnakú štruktúru ako v PasswordsScreen
    // =========================================================================
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            // Obsah pod záložkami
            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                // ✅ OBSAH AllContactsView VLOŽENÝ PRIAMO SEM
                val searchQuery = viewModel.searchQuery
                val contactsFromViewModel = viewModel.displayedContacts
                val isFilterDialogVisible = viewModel.isFilterDialogVisible
                val activeFilters by viewModel.activeChannelFilters.collectAsState()
                val allChannels = viewModel.channelOptions.filter { it != "Všetky" }

                if (isFilterDialogVisible) {
                    FilterContactsDialog(
                        allChannels = allChannels,
                        activeFilters = activeFilters,
                        onDismiss = { viewModel.onFilterDialogDismiss() },
                        onChannelSelected = { channel, isSelected ->
                            viewModel.onFilterChannelSelected(channel, isSelected)
                        },
                        onClearFilters = { viewModel.clearAllFilters() }
                    )
                }

                val contacts = if (activeFilters.isEmpty()) {
                    contactsFromViewModel
                } else {
                    contactsFromViewModel.filter { contact -> contact.channel in activeFilters }
                }

                // Tento Column už nie je v samostatnej funkcii
                Column {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onSearch = {},
                        active = false,
                        onActiveChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                            .offset(y = (-18).dp),
                        placeholder = { Text("Vyhľadať v kontaktoch...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, "Vymazať text", tint = Color.Black)
                                }
                            } else {
                                IconButton(onClick = { viewModel.onFilterDialogOpen() }) {
                                    Icon(
                                        Icons.Filled.FilterList,
                                        "Filtrovať zoznam",
                                        tint = if (activeFilters.isEmpty()) Color.Black else MaterialTheme.colorScheme.primary
                                    )
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
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val message = when {
                                searchQuery.isNotBlank() || activeFilters.isNotEmpty() -> "Pre zadané kritériá sa nenašli žiadne kontakty."
                                else -> "Zatiaľ žiadne kontakty."
                            }
                            Text(message, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
            } else {
                val channelFunctions by viewModel.channelFunctions.collectAsState()
                ChannelDetailView(
                    viewModel = viewModel,
                    functions = channelFunctions,
                    isEditMode = isEditMode,
                    onAddFunction = { nazov -> viewModel.addChannelFunction(nazov) },
                    onMoveFunction = { from, to -> viewModel.moveChannelFunction(from, to) },
                    onAddPersonClick = { function ->
                        selectedFunctionForDialog = function
                        showAddPersonDialog = true
                    },
                    onRemovePersonClick = { functionId, personId -> viewModel.removePersonFromFunction(functionId, personId) },
                    onUpdatePersonNote = { functionId, personId, newNote -> viewModel.updatePersonNoteInFunction(functionId, personId, newNote) },
                    onEditFunctionClick = { function ->
                        functionToEdit = function
                        showEditFunctionDialog = true
                    }
                )
            }
        }

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
                Icon(Icons.Default.Add, "Pridať kontakt")
            }
        }

        if (showAddPersonDialog && selectedFunctionForDialog != null) {
            val assignedIds = selectedFunctionForDialog!!.assignedPeople.map { it.contactId }
            AddPersonToFunctionDialog(
                allContacts = viewModel.contacts,
                assignedPeopleIds = assignedIds,
                onDismiss = { showAddPersonDialog = false },
                onPersonSelected = { contact ->
                    viewModel.assignPersonToFunction(selectedFunctionForDialog!!.id, contact)
                    showAddPersonDialog = false
                }
            )
        }

        if (showEditFunctionDialog && functionToEdit != null) {
            var newTitle by remember { mutableStateOf(functionToEdit!!.title) }
            AlertDialog(
                onDismissRequest = { showEditFunctionDialog = false },
                title = { Text("Upraviť názov funkcie") },
                text = {
                    TextField(value = newTitle, onValueChange = { newTitle = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateChannelFunction(functionToEdit!!.id, newTitle, functionToEdit!!.notes ?: "")
                            showEditFunctionDialog = false
                        },
                        enabled = newTitle.isNotBlank()
                    ) { Text("Uložiť") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditFunctionDialog = false }) { Text("Zrušiť") }
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelDetailView(
    viewModel: ContactsViewModel,
    functions: List<ChannelFunction>,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onAddFunction: (String) -> Unit,
    onMoveFunction: (Int, Int) -> Unit,
    // ✅ TIETO RIADKY PRIDAJTE SPÄŤ
    onAddPersonClick: (ChannelFunction) -> Unit,
    onRemovePersonClick: (functionId: String, personId: String) -> Unit,
    onUpdatePersonNote: (functionId: String, personId: String, newNote: String) -> Unit,
    onEditFunctionClick: (ChannelFunction) -> Unit


) {
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> onMoveFunction(from.index, to.index) }
    )

    // ✅ KROK 1: Vytvoríme jednu veľkú hlavnú kartu
    val cardModifier = if (isEditMode) {
        // V edit móde sa karta roztiahne na celú obrazovku a bude sa dať presúvať
        Modifier
            .fillMaxSize()
            .reorderable(reorderState)
    } else {
        // V normálnom režime sa výška prispôsobí obsahu a nedá sa presúvať
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // Pridáme trochu miesta okolo karty
    }

// 2. Použijeme ho na karte
    Card(
        modifier = cardModifier, // ✅ POUŽIJEME NÁŠ NOVÝ PODMIENENÝ MODIFIKÁTOR
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Svetlé pozadie
    ) {
        // ✅ KROK 2: Vnútri karty je LazyColumn, ktorý sa dá presúvať
        LazyColumn(
            state = reorderState.listState,
            // ✅ TOTO JE JEDINÉ SPRÁVNE MIESTO PRE TEN KÓD

            contentPadding = PaddingValues(12.dp)
        ) {
            if (functions.isEmpty() && !isEditMode) {
                item {
                    // Odstránili sme .fillParentMaxSize(), aby sa Box nerozťahoval
                    Box(
                        modifier = Modifier
                            .fillMaxWidth() // Iba na šírku
                            .padding(vertical = 48.dp), // Dáme mu trochu miesta zhora a zdola
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pre tento kanál zatiaľ nie sú definované žiadne funkcie.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(items = functions, key = { it.id }) { function ->
                ReorderableItem(
                    reorderableState = reorderState, // ✅ OPRAVA: Použijeme správny parameter
                    key = function.id
                ) {
                    FunctionSection(
                        function = function,
                        viewModel = viewModel,
                        isEditMode = isEditMode,
                        modifier = Modifier
                            // ✅ OPRAVA: Použijeme opäť celý reorderState
                            .then(if (isEditMode) Modifier.detectReorderAfterLongPress(reorderState) else Modifier),
                        onAddPersonClick = { onAddPersonClick(function) },
                        onRemovePersonClick = { personId -> onRemovePersonClick(function.id, personId) },
                        onUpdatePersonNote = { personId, newNote -> onUpdatePersonNote(function.id, personId, newNote) },
                        onEditFunctionClick = { onEditFunctionClick(function) }
                    )
                }

                // Deliaca čiara medzi položkami
                Divider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
            }

            // Tlačidlo na pridanie novej funkcie (len v edit mode)
            if (isEditMode) {
                item {
                    OutlinedButton(
                        onClick = { onAddFunction("Nová funkcia") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pridať novú funkciu")
                    }
                }
            }
        }
    }
}


/**
 * Komponent pre jednu funkciu (napr. "Kameramani").
 * V normálnom režime je bez okraja, v režime úprav má jemný okraj.
 */
@Composable
private fun FunctionSection(
    function: ChannelFunction,
    viewModel: ContactsViewModel,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onAddPersonClick: () -> Unit,
    onRemovePersonClick: (personId: String) -> Unit,
    onUpdatePersonNote: (personId: String, newNote: String) -> Unit,
    onEditFunctionClick: () -> Unit
) {
    // Modifikátor, ktorý pridá orámovanie a odsadenie IBA v režime úprav
    val containerModifier = if (isEditMode) {
        Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    } else {
        Modifier
    }

    // Hlavný stĺpec, ktorý dostáva modifikátor od ReorderableItem
    // a zároveň v sebe spája vizuálny modifikátor
    Column(modifier = modifier.then(containerModifier)) {
        // --- HLAVIČKA S NÁZVOM A AKCIAMI ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Presunúť",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = function.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary, // <-- PRIDAJTE TENTO RIADOK
                modifier = Modifier.weight(1f)
            )
            if (isEditMode) {
                IconButton(onClick = onEditFunctionClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, "Upraviť názov", tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { viewModel.removeChannelFunction(function.id) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Zmazať funkciu", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // --- POZNÁMKY K FUNKCII ---
        if (isEditMode) {
            BasicTextField(
                value = function.notes ?: "",
                onValueChange = { newNotes ->
                    viewModel.updateChannelFunction(function.id, function.title, newNotes)
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                decorationBox = { innerTextField ->
                    if (function.notes.isNullOrBlank()) {
                        Text(
                            "Pridať poznámku k funkcii...",
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }

        // --- ZOZNAM PRIRADENÝCH ĽUDÍ ---
        if (function.assignedPeople.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            function.assignedPeople.forEach { person ->
                AssignedPersonRow(
                    person = person,
                    isEditMode = isEditMode,
                    onRemoveClick = { onRemovePersonClick(person.id) },
                    onNoteChange = { newNote -> onUpdatePersonNote(person.id, newNote) }
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        // --- TLAČIDLO "PRIRADIŤ OSOBU" ---
        if (isEditMode) {
            TextButton(
                onClick = onAddPersonClick,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Priradiť osobu")
            }
        }
    }
}

/**
 * Riadok pre jednu priradenú osobu s vylepšeným UI.
 */
@Composable
private fun AssignedPersonRow(
    person: AssignedPerson,
    isEditMode: Boolean,
    onRemoveClick: () -> Unit,
    onNoteChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = if (isEditMode) 0.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (!person.phone.isNullOrBlank()) {
                Text(person.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(Modifier.width(12.dp))

        if (isEditMode) {
            // V editačnom móde je tu poznámka ako TextField
            BasicTextField(
                value = person.notes,
                onValueChange = onNoteChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.weight(0.8f), // Dáme poznámke trochu menej miesta
                maxLines = 2,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (person.notes.isBlank()) {
                            Text("Poznámka...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontStyle = FontStyle.Italic)
                        }
                        innerTextField()
                    }
                }
            )
            IconButton(onClick = onRemoveClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "Odstrániť osobu", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        } else {
            // V normálnom móde je tu len text poznámky
            if (!person.notes.isBlank()) {
                Text(
                    text = person.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
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

@Composable
private fun FilterContactsDialog(
    allChannels: List<String>,
    activeFilters: Set<String>,
    onDismiss: () -> Unit,
    onChannelSelected: (String, Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrovať podľa kanála") },
        text = {
            LazyColumn {
                items(allChannels) { channel ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onChannelSelected(channel, channel !in activeFilters)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = channel in activeFilters,
                            onCheckedChange = { isChecked ->
                                onChannelSelected(channel, isChecked)
                            }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(text = channel)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zavrieť")
            }
        },
        dismissButton = {
            // Tlačidlo na zrušenie všetkých filtrov
            TextButton(onClick = {
                onClearFilters()
                onDismiss() // a zatvoríme dialóg
            }) {
                Text("Zrušiť filtre")
            }
        }
    )
}