package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    navController: NavController,
    contactId: Int,
    viewModel: ContactsViewModel = viewModel(), // Používame Váš ContactsViewModel
    onBack: () -> Unit
) {
    // 🎯 Kľúčové: Načítame a inicializujeme formulár pri prvom vstupe.
    // Robíme to len raz, vďaka remember.
    val contactData = remember(contactId) {
        viewModel.getContactById(contactId)?.copy()
    }

    // Ak sa kontakt nenájde, okamžite sa vrátime naspäť
    if (contactData == null) {
        onBack()
        return
    }

    // ⭐ OPRAVA REFERENCIE: Zabezpečíme, že sa originalContact berie ako meniteľný stav
    // Táto hodnota sa mení LEN pri úspešnom uložení.
    var originalContact by remember { mutableStateOf(contactData) }

    // 🎯 Kľúčové: lokálny, meniteľný stav, inicializovaný pôvodnými dátami.
    var localContact by remember { mutableStateOf(originalContact) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSaveConfirmationDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Dôležité: Porovnáva sa aktuálny stav s pôvodným stavom.
    // Používame porovnanie dvoch data class (copy/original)
    val hasUnsavedChanges = localContact != originalContact

    // -----------------------------------------------------------------
    // FUNKCIE PRE AKCIE
    // -----------------------------------------------------------------

    // Uloží zmeny a zostane na obrazovke, a ZARESETUJE STAV ZMIEN.
    fun saveContactAndStay() {
        // Voláme novú signatúru: updateContact teraz berie hotový ContactItem
        viewModel.updateContact(localContact)
        showSaveConfirmationDialog = false

        // ⭐ OPRAVA PARADOXU: Po uložení aktualizujeme 'originalContact'
        originalContact = localContact.copy()
    }

    // Zahodí (resetuje) lokálne zmeny, ale zostane na obrazovke.
    fun discardChangesAndStay() {
        // Resetuje localContact na pôvodné hodnoty
        localContact = originalContact.copy()
        showSaveConfirmationDialog = false
    }

    // Pôvodná funkcia pre spätnú navigáciu
    fun saveContactAndNavigateBack() {
        // Voláme novú signatúru
        viewModel.updateContact(localContact)
        showSaveConfirmationDialog = false
        showUnsavedChangesDialog = false
        onBack()
    }

    fun discardChangesAndNavigateBack() {
        showUnsavedChangesDialog = false
        onBack()
    }

    fun deleteContactAndNavigateBack() {
        // Voláme novú signatúru: removeContact berie hotový ContactItem
        viewModel.removeContact(originalContact)
        showDeleteDialog = false
        onBack()
    }

    // Pomocná funkcia na aktualizáciu stavu
    fun updateLocalContact(newContact: ContactItem) {
        localContact = newContact
    }

    // -----------------------------------------------------------------
    // LOGIKA SPÄŤ
    // -----------------------------------------------------------------

    // Volanie systémového tlačidla späť (Android/Emulator)
    BackHandler(enabled = true) {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    val actions: @Composable RowScope.() -> Unit = {
        // Tlačidlo ULOŽIŤ - OTVÁRA POTVRDZOVACÍ DIALÓG
        IconButton(
            onClick = { showSaveConfirmationDialog = true },
            enabled = hasUnsavedChanges // Používa novú podmienku
        ) {
            Icon(Icons.Default.Done, contentDescription = "Uložiť")
        }

        // Tlačidlo MAZAŤ - OTVÁRA POTVRDZOVACÍ DIALÓG
        IconButton(
            onClick = { showDeleteDialog = true }
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Vymazať", tint = MaterialTheme.colorScheme.error)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upraviť Kontakt") },
                navigationIcon = {
                    // LOGIKA PRE ŠÍPKU SPÄŤ V TOP APP BAR
                    IconButton(onClick = {
                        if (hasUnsavedChanges) {
                            showUnsavedChangesDialog = true
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                },
                actions = actions
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Polia pre kontaktné dáta. Používame localContact a ContactItem.copy()
            OutlinedTextField(
                // Používame .orEmpty() pre String?
                value = localContact.firstName.orEmpty(),
                onValueChange = { updateLocalContact(localContact.copy(firstName = it)) },
                label = { Text("Meno") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // Používame .orEmpty() pre String?
                value = localContact.lastName.orEmpty(),
                onValueChange = { updateLocalContact(localContact.copy(lastName = it)) },
                label = { Text("Priezvisko") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // OPRAVA: Premenované z phoneNumber na phone (podľa ContactItem)
                value = localContact.phone.orEmpty(),
                onValueChange = { updateLocalContact(localContact.copy(phone = it)) },
                label = { Text("Telefónne číslo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // Používame .orEmpty() pre String?
                value = localContact.email.orEmpty(),
                onValueChange = { updateLocalContact(localContact.copy(email = it)) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // Používame .orEmpty() pre String?
                value = localContact.function.orEmpty(),
                onValueChange = { updateLocalContact(localContact.copy(function = it)) },
                label = { Text("Funkcia/Pozícia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                // Používame .orEmpty() pre String?
                value = localContact.notes.orEmpty(),
                onValueChange = { updateLocalContact(localContact.copy(notes = it)) },
                label = { Text("Poznámky") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // 🌟 OPRAVA CHYBY: Explicitne vytvoríme ne-nullable String pre selectedChannel
            val selectedChannelValue: String = localContact.channel ?: viewModel.channelOptions.first()

            ChannelDropdown(
                selectedChannel = selectedChannelValue,
                onChannelSelected = { updateLocalContact(localContact.copy(channel = it)) },
                channelOptions = viewModel.channelOptions
            )
        }

        // -----------------------------------------------------------------
        // POTVRDZOVACIE DIALÓGY
        // -----------------------------------------------------------------

        // Dialóg MAZANIA
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Odstrániť kontakt?") },
                text = { Text("Naozaj chcete natrvalo odstrániť kontakt?") },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteDialog = false }) {
                        Text("Zrušiť")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = ::deleteContactAndNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Odstrániť")
                    }
                }
            )
        }

        // Dialóg PRE POTVRDENIE ULOŽENIA
        if (showSaveConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showSaveConfirmationDialog = false },
                title = { Text("Uložiť zmeny?") },
                text = { Text("Chcete uložiť zmeny a zostať na obrazovke?") },
                // Tlačidlo Áno (uložiť a zostať)
                confirmButton = {
                    Button(
                        onClick = ::saveContactAndStay
                    ) {
                        Text("Uložiť")
                    }
                },
                // Tlačidlo "Nie" (zahodiť zmeny a zostať)
                dismissButton = {
                    OutlinedButton(
                        onClick = ::discardChangesAndStay
                    ) {
                        Text("Zahodiť zmeny")
                    }
                }
            )
        }

        // Dialóg NEULOŽENÝCH ZMIEN - ÚPRAVA ROZLOŽENIA
        if (showUnsavedChangesDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedChangesDialog = false },
                title = { Text("Neuložené zmeny") },
                text = { Text("Máte neuložené zmeny. Chcete ich uložiť pred odchodom?") },

                confirmButton = {
                    // Uložiť a späť
                    Button(onClick = ::saveContactAndNavigateBack) {
                        Text("Uložiť")
                    }
                },
                dismissButton = {
                    // Storno (zostať na obrazovke)
                    OutlinedButton(onClick = { showUnsavedChangesDialog = false }) {
                        Text("Zrušiť")
                    }
                    Spacer(Modifier.width(8.dp))
                    // Zahodiť a späť
                    TextButton(onClick = ::discardChangesAndNavigateBack) {
                        Text("Zahodiť")
                    }
                }
            )
        }
    }
}

// ----------------------------------------------------
// Kanálové Dropdown Menu
// ----------------------------------------------------

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
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            channelOptions.forEach { selectionOption ->
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
