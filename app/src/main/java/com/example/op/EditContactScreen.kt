package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    navController: NavController,
    contactId: Int,
    viewModel: ContactsViewModel = viewModel(),
    // pridame SharedViewModel pre komunikaciu s TopAppBar
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    val contactData = remember(contactId) {
        viewModel.getContactById(contactId)?.copy()
    }

    if (contactData == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var originalContact by remember { mutableStateOf(contactData) }
    var localContact by remember { mutableStateOf(originalContact) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val hasUnsavedChanges = localContact != originalContact

    // --- Funkcie ---
    fun showSavedSnackbar() {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Kontakt bol úspešne uložený")
        }
    }

    fun saveContactAndStay() {
        viewModel.updateContact(localContact)
        originalContact = localContact.copy()
        showSavedSnackbar()
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    // --- Logika pre nastavenie hornej lišty ---
    LaunchedEffect(localContact, hasUnsavedChanges) {
        val contactName = listOfNotNull(localContact.firstName, localContact.lastName)
            .joinToString(" ")
            .ifBlank { "Upraviť Kontakt" }

        sharedViewModel.setTopBarState(
            TopBarState(
                title = contactName,
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (hasUnsavedChanges) {
                        IconButton(onClick = ::saveContactAndStay) {
                            Icon(Icons.Default.Done, "Uložiť")
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Viac")
                    }
                }
            )
        )
    }

    BackHandler(onBack = ::handleBackNavigation)

    // --- Zvyšok UI ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        // TopAppBar je odtialto odstranena
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... vsetky OutlinedTextFieldy zostavaju tu bez zmeny ...
                OutlinedTextField(
                    value = localContact.firstName.orEmpty(),
                    onValueChange = { localContact = localContact.copy(firstName = it) },
                    label = { Text("Meno") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.lastName.orEmpty(),
                    onValueChange = { localContact = localContact.copy(lastName = it) },
                    label = { Text("Priezvisko") },
                    modifier = Modifier.fillMaxWidth()
                )
                // ... atd ...
                OutlinedTextField(
                    value = localContact.phone.orEmpty(),
                    onValueChange = { localContact = localContact.copy(phone = it) },
                    label = { Text("Telefónne číslo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = localContact.email.orEmpty(),
                    onValueChange = { localContact = localContact.copy(email = it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = localContact.function.orEmpty(),
                    onValueChange = { localContact = localContact.copy(function = it) },
                    label = { Text("Funkcia/Pozícia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = localContact.notes.orEmpty(),
                    onValueChange = { localContact = localContact.copy(notes = it) },
                    label = { Text("Poznámky") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                val selectedChannelValue: String = localContact.channel ?: viewModel.channelOptions.first()

                ChannelDropdown(
                    selectedChannel = selectedChannelValue,
                    onChannelSelected = { localContact = localContact.copy(channel = it) },
                    channelOptions = viewModel.channelOptions
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                DropdownMenuItem(
                    text = { Text("Zmazať") },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, "Zmazať", tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }

    // ... Vsetky dialogy zostavaju bez zmeny ...
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Odstrániť kontakt?") },
            text = { Text("Naozaj chcete natrvalo odstrániť kontakt?") },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeContact(originalContact)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Odstrániť") }
            }
        )
    }

    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Neuložené zmeny") },
            text = { Text("Máte neuložené zmeny. Chcete ich uložiť pred odchodom?") },
            confirmButton = {
                Button(onClick = {
                    saveContactAndStay()
                    onBack()
                }) { Text("Uložiť a odísť") }
            },
            dismissButton = {
                TextButton(onClick = onBack) { Text("Zahodiť") }
            }
        )
    }
}

// ChannelDropdown zostava bez zmeny...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDropdown(
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    channelOptions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedChannel,
            onValueChange = {},
            label = { Text("Kanál") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val dropdownOptions = remember(channelOptions) {
                channelOptions.filter { it != "Všetky" }
            }

            dropdownOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onChannelSelected(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

