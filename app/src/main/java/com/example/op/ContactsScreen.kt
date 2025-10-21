package com.example.op

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.material3.InputChip
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListItem(
    contact: ContactItem,    viewModel: ContactsViewModel = viewModel(), // ✅ PRIDANÝ VIEWMODEL
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = when (contact.channel) {
                "Jednotka" -> painterResource(id = R.drawable.ic_logo_jednotka)
                "Dvojka" -> painterResource(id = R.drawable.ic_logo_dvojka)
                "24" -> painterResource(id = R.drawable.ic_logo_24)
                "Sport" -> painterResource(id = R.drawable.ic_logo_sport)
                else -> null
            }

            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = contact.channel,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Kontakt",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.getFullName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // ✅ KROK 1: ZÍSKANIE PRVEJ FUNKCIE
                // Zoberieme prvé ID funkcie z kontaktu, ak nejaké má.
                val firstFunctionId = contact.functionIds.firstOrNull()

                if (firstFunctionId != null) {
                    // Nájdeme názov funkcie podľa jej ID vo ViewModele.
                    val functionName = viewModel.allContactFunctions
                        .find { it.id == firstFunctionId }?.name

                    if (functionName != null) {
                        Text(
                            text = functionName, // Zobrazíme nájdený názov
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
    sharedViewModel: SharedViewModel, modifier: Modifier = Modifier,
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

            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                val searchQuery = viewModel.searchQuery
                val contactsFromViewModel = viewModel.displayedContacts
                val isFilterDialogVisible = viewModel.isFilterDialogVisible
                val activeChannelFilters by viewModel.activeChannelFilters.collectAsState()
                val activeFunctionFilters by viewModel.activeFunctionFilters.collectAsState()
                val activeFilters by viewModel.activeChannelFilters.collectAsState()
                val allChannels = viewModel.channelOptions.filter { it != "Všetky" }

                if (isFilterDialogVisible) {
                    val activeChannelFilters by viewModel.activeChannelFilters.collectAsState()
                    val activeFunctionFilters by viewModel.activeFunctionFilters.collectAsState() // Načítame stav
                    val allChannels = viewModel.channelOptions.filter { it != "Všetky" }

                    FilterContactsDialog(
                        // Kanály
                        allChannels = allChannels,
                        activeChannelFilters = activeChannelFilters,
                        onChannelSelected = viewModel::onFilterChannelSelected, // Skratka pre lambdu

                        // Funkcie
                        allFunctions = viewModel.allContactFunctions,
                        activeFunctionFilters = activeFunctionFilters,
                        onFunctionSelected = viewModel::onFilterFunctionSelected, // Skratka pre lambdu

                        // Spoločné
                        onDismiss = viewModel::onFilterDialogDismiss,
                        onClearFilters = viewModel::clearAllFilters
                    )
                }

                val contacts = if (activeFilters.isEmpty()) {
                    contactsFromViewModel
                } else {
                    contactsFromViewModel.filter { contact -> contact.channel in activeFilters }
                }

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
                                        tint = if (activeChannelFilters.isEmpty() && activeFunctionFilters.isEmpty()) Color.Black else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = Color.White,
                            dividerColor = Color.Transparent
                        )
                    ) {}


                    ActiveContactFiltersRow(
                        activeChannelFilters = activeChannelFilters,
                        activeFunctionFilters = activeFunctionFilters,
                        allFunctions = viewModel.allContactFunctions,
                        onRemoveChannel = viewModel::removeChannelFilter,
                        onRemoveFunction = viewModel::removeFunctionFilter,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
                            .offset(y = (-18).dp)
                    )


                    if (contactsFromViewModel.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val message = when {
                                searchQuery.isNotBlank() || activeChannelFilters.isNotEmpty() || activeFunctionFilters.isNotEmpty() -> "Pre zadané kritériá sa nenašli žiadne kontakty."
                                else -> "Zatiaľ žiadne kontakty."
                            }
                            Text(message, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(contactsFromViewModel, key = { it.id }) { contact -> // ZMENA: použijeme priamo contactsFromViewModel
                                ContactListItem(
                                    contact = contact,
                                    viewModel = viewModel,
                                    onItemClick = { navController.navigate("contact_detail/${contact.id}") }
                                )
                            }
                        }
                    }
                }
            } else {
                val channelFunctions by viewModel.channelFunctions.collectAsState()

                val titleColor = when (selectedTabFilter) {
                    "Jednotka" -> Color(0xFFEC008C) // Ružová
                    "Dvojka" -> Color.Black
                    "Sport" -> Color(0xFFF24E1E) // Červeno-oranžová
                    "24" -> Color(0xFF2F2F8B)   // Modrá
                    else -> MaterialTheme.colorScheme.onSurface
                }

                val titleText = when (selectedTabFilter) {
                    "Jednotka" -> ":1"
                    "Dvojka" -> ":2"
                    "24" -> ":24"
                    "Sport" -> ":Šport"
                    else -> selectedTabFilter
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

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
    onAddPersonClick: (ChannelFunction) -> Unit,
    onRemovePersonClick: (functionId: String, personId: String) -> Unit,
    onUpdatePersonNote: (functionId: String, personId: String, newNote: String) -> Unit,
    onEditFunctionClick: (ChannelFunction) -> Unit,
) {
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> onMoveFunction(from.index, to.index) }
    )

    val cardModifier = if (isEditMode) {
        Modifier
            .fillMaxSize()
            .reorderable(reorderState)
    } else {
        Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = reorderState.listState,
            contentPadding = PaddingValues(12.dp)
        ) {
            if (functions.isEmpty() && !isEditMode) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
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
                    reorderableState = reorderState,
                    key = function.id
                ) {
                    FunctionSection(
                        function = function,
                        viewModel = viewModel,
                        isEditMode = isEditMode,
                        modifier = Modifier
                            .then(if (isEditMode) Modifier.detectReorderAfterLongPress(reorderState) else Modifier),
                        onAddPersonClick = { onAddPersonClick(function) },
                        onRemovePersonClick = { personId -> onRemovePersonClick(function.id, personId) },
                        onUpdatePersonNote = { personId, newNote -> onUpdatePersonNote(function.id, personId, newNote) },
                        onEditFunctionClick = { onEditFunctionClick(function) }
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp))
            }

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

@Composable
private fun FunctionSection(
    function: ChannelFunction,
    viewModel: ContactsViewModel,
    isEditMode: Boolean,
    modifier: Modifier = Modifier,
    onAddPersonClick: () -> Unit,
    onRemovePersonClick: (personId: String) -> Unit,
    onUpdatePersonNote: (personId: String, newNote: String) -> Unit,
    onEditFunctionClick: () -> Unit,
) {
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

    Column(modifier = modifier.then(containerModifier)) {
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
                color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun AssignedPersonRow(
    person: AssignedPerson,
    isEditMode: Boolean,
    onRemoveClick: () -> Unit,
    onNoteChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isEditMode) 0.dp else 8.dp),
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
            BasicTextField(
                value = person.notes,
                onValueChange = onNoteChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.weight(0.8f),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPersonToFunctionDialog(
    allContacts: List<ContactItem>,
    assignedPeopleIds: List<String>,
    onDismiss: () -> Unit,
    onPersonSelected: (ContactItem) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val availableContacts = allContacts.filter { contact -> contact.id.toString() !in assignedPeopleIds }
    val filteredContacts = if (searchQuery.isBlank()) {
        availableContacts
    } else {
        availableContacts.filter { it.getFullName().contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať osobu") },
        text = {
            Column {
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
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    )
                )

                if (filteredContacts.isEmpty()) {
                    Text(
                        if (searchQuery.isNotBlank()) "Nenašiel sa žiadny zodpovedajúci kontakt."
                        else "Všetky dostupné kontakty sú už priradené."
                    )
                } else {
                    LazyColumn {
                        items(filteredContacts, key = { it.id }) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPersonSelected(contact) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val painter = when (contact.channel) {
                                    "Jednotka" -> painterResource(id = R.drawable.ic_logo_jednotka)
                                    "Dvojka" -> painterResource(id = R.drawable.ic_logo_dvojka)
                                    "24" -> painterResource(id = R.drawable.ic_logo_24)
                                    "Sport" -> painterResource(id = R.drawable.ic_logo_sport)
                                    else -> null
                                }

                                if (painter != null) {
                                    Image(
                                        painter = painter,
                                        contentDescription = contact.channel,
                                        modifier = Modifier.size(28.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Kontakt",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(Modifier.width(16.dp))
                                Text(contact.getFullName(), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zrušiť") }
        }
    )
}

@Composable
private fun FilterContactsDialog(
    // Parametre pre kanály
    allChannels: List<String>,
    activeChannelFilters: Set<String>,
    onChannelSelected: (String, Boolean) -> Unit,

    // ✅ NOVÉ PARAMETRE PRE FUNKCIE
    allFunctions: List<ContactFunction>,
    activeFunctionFilters: Set<String>,
    onFunctionSelected: (String, Boolean) -> Unit,

    // Spoločné parametre
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrovať kontakty") },
        text = {
            // Použijeme LazyColumn, aby sa zmestilo veľa možností
            LazyColumn {
                // --- Sekcia pre kanály ---
                item {
                    Text(
                        "PODĽA KANÁLA",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(allChannels) { channel ->
                    FilterDialogRow(
                        text = channel,
                        isChecked = channel in activeChannelFilters,
                        onCheckedChange = { isChecked -> onChannelSelected(channel, isChecked) }
                    )
                }

                // --- Oddeľovač ---
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }

                // --- ✅ NOVÁ SEKCIA PRE FUNKCIE ---
                item {
                    Text(
                        "PODĽA FUNKCIE",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(allFunctions, key = { it.id }) { function ->
                    FilterDialogRow(
                        text = function.name,
                        isChecked = function.id in activeFunctionFilters,
                        onCheckedChange = { isChecked -> onFunctionSelected(function.id, isChecked) }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Zobraziť")
            }
        },
        dismissButton = {
            TextButton(onClick = onClearFilters) {
                Text("Zrušiť filtre")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ActiveContactFiltersRow(
    activeChannelFilters: Set<String>,
    activeFunctionFilters: Set<String>,
    allFunctions: List<ContactFunction>,
    onRemoveChannel: (String) -> Unit,
    onRemoveFunction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zobrazíme, len ak je aspoň jeden filter aktívny
    if (activeChannelFilters.isNotEmpty() || activeFunctionFilters.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                "Aktívne filtre:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 4.dp
            ) {
                // Zobrazenie filtrov pre kanály
                activeChannelFilters.forEach { channel ->
                    InputChip(
                        selected = true,
                        onClick = { onRemoveChannel(channel) },
                        label = { Text(channel) },
                        trailingIcon = {
                            Icon(Icons.Default.Clear, "Odstrániť filter", Modifier.size(18.dp))
                        }
                    )
                }

                // Zobrazenie filtrov pre funkcie
                activeFunctionFilters.forEach { functionId ->
                    val function = allFunctions.find { it.id == functionId }
                    if (function != null) {
                        InputChip(
                            selected = true,
                            onClick = { onRemoveFunction(functionId) },
                            label = { Text(function.name) },
                            trailingIcon = {
                                Icon(Icons.Default.Clear, "Odstrániť filter", Modifier.size(18.dp))
                            }
                        )
                    }
                }
            }
        }
    }
}


// Pomocný komponent, aby sme neopakovali kód
@Composable
private fun FilterDialogRow(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Spacer(Modifier.width(16.dp))
        Text(text = text)
    }
}
