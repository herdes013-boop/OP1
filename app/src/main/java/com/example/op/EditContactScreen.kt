package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    contactId: Int,
    viewModel: ContactsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit,
) {
    // --- NOVÝ, ROBUSTNÝ BLOK NAČÍTANIA DÁT ---
    var localContactState by remember { mutableStateOf<ContactItem?>(null) }
    var originalContact by remember { mutableStateOf<ContactItem?>(null) }

    LaunchedEffect(contactId) {
        val fetchedItem = viewModel.getContactById(contactId)
        localContactState = fetchedItem
        originalContact = fetchedItem?.copy() // Kópia pre porovnanie
    }
    // --- KONIEC NOVÉHO BLOKU ---

    // Poistka, ak sa kontakt nenájde
    if (localContactState == null) {
        // Zobrazí sa len na chvíľu, kým sa dáta načítajú
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 'let' nám zaručí, že pracujeme s nenulovými dátami a zjednoduší kód
    localContactState?.let { localContact ->

        var showDeleteDialog by remember { mutableStateOf(false) }
        var showUnsavedChangesDialog by remember { mutableStateOf(false) }
        var showMenu by remember { mutableStateOf(false) }

        // Porovnávame aktuálny stav s originálom
        val hasUnsavedChanges by remember(localContact, originalContact) {
            derivedStateOf { localContact != originalContact }
        }

        fun saveContactAndGoBack() {
            localContactState?.let { currentContact ->
                viewModel.updateContact(currentContact)
            }
            onBack()
        }

        fun saveChanges() {
            localContactState?.let { currentContact -> // Získame aktuálny stav
                viewModel.updateContact(currentContact)
                originalContact = currentContact.copy()
            }
        }

        fun handleBackNavigation() {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog = true
            } else {
                onBack()
            }
        }

        LaunchedEffect(hasUnsavedChanges) {
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = "Upraviť kontakt",
                    navigationIcon = {
                        IconButton(onClick = ::handleBackNavigation) {
                            Icon(Icons.Default.ArrowBack, "Naspäť")
                        }
                    },
                    actions = {
                        if (hasUnsavedChanges) {
                            Button(
                                onClick = { saveChanges() }, // ✅ Nová funkcia a správna syntax
                                modifier = Modifier.padding(horizontal = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                )
                            ) {
                                Text("ULOŽIŤ")
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

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = localContact.firstName.orEmpty(),
                    onValueChange = { localContactState = localContact.copy(firstName = it) },
                    label = { Text("Meno") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.lastName.orEmpty(),
                    onValueChange = { localContactState = localContact.copy(lastName = it) },
                    label = { Text("Priezvisko") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.phone.orEmpty(),
                    onValueChange = { localContactState = localContact.copy(phone = it) },
                    label = { Text("Telefónne číslo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.email.orEmpty(),
                    onValueChange = { localContactState = localContact.copy(email = it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.function.orEmpty(),
                    onValueChange = { localContactState = localContact.copy(function = it) },
                    label = { Text("Funkcia/Pozícia") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.notes.orEmpty(),
                    onValueChange = { localContactState = localContact.copy(notes = it) },
                    label = { Text("Poznámky") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                val selectedChannelValue: String = localContact.channel ?: viewModel.channelOptions.first()
                ChannelDropdown(
                    selectedChannel = selectedChannelValue,
                    onChannelSelected = { localContactState = localContact.copy(channel = it) },
                    channelOptions = viewModel.channelOptions
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Zmazať", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, "Zmazať", tint = MaterialTheme.colorScheme.error) }
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Odstrániť kontakt?") },
                text = { Text("Naozaj chcete natrvalo odstrániť kontakt?") },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            originalContact?.let { viewModel.removeContact(it) }
                            showDeleteDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Odstrániť") }
                }
            )
        }

        if (showUnsavedChangesDialog) {
            UnsavedChangesDialog(
                onSave = {
                    saveContactAndGoBack()
                    showUnsavedChangesDialog = false
                },
                onDiscard = {
                    onBack()
                    showUnsavedChangesDialog = false
                },
                onCancel = {
                    showUnsavedChangesDialog = false
                }
            )
        }
    }
}


// ChannelDropdown zostáva bez zmeny
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDropdown(
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    channelOptions: List<String>,
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
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
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
