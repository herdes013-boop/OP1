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
import kotlinx.coroutines.launch // Nutné pre volanie showSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    navController: NavController,
    contactId: Int,
    viewModel: ContactsViewModel = viewModel(), // Používame Váš ContactsViewModel
    onBack: () -> Unit
) {
    // 🎯 Kľúčové: Načítame a inicializujeme formulár pri prvom vstupe.
    val contactData = remember(contactId) {
        viewModel.getContactById(contactId)?.copy()
    }

    // Ak sa kontakt nenájde, okamžite sa vrátime naspäť
    if (contactData == null) {
        onBack()
        return
    }

    // -----------------------------------------------------------------
    // STAVY PRE SNACKBAR
    // -----------------------------------------------------------------
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    // -----------------------------------------------------------------

    var originalContact by remember { mutableStateOf(contactData) }
    var localContact by remember { mutableStateOf(originalContact) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // Dôležité: Porovnáva sa aktuálny stav s pôvodným stavom.
    val hasUnsavedChanges = localContact != originalContact

    // -----------------------------------------------------------------
    // FUNKCIE PRE SPÄTNÚ VÄZBU
    // -----------------------------------------------------------------

    /**
     * Zobrazí úspešnú správu o uložení.
     */
    fun showSavedSnackbar() {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = "Kontakt bol úspešne uložený",
                duration = SnackbarDuration.Short
            )
        }
    }

    // -----------------------------------------------------------------
    // FUNKCIE PRE AKCIE
    // -----------------------------------------------------------------

    /**
     * Uloží zmeny, aktualizuje 'originalContact', zostane na obrazovke a zobrazí Snackbar.
     */
    fun saveContactAndStay() {
        // 1. Uložíme zmeny do View Modelu
        viewModel.updateContact(localContact)

        // 2. Aktualizujeme 'originalContact', aby sa zresetoval stav hasUnsavedChanges
        originalContact = localContact.copy()

        // 3. Zobrazíme Snackbar
        showSavedSnackbar()
    }

    /**
     * Zahodí (resetuje) lokálne zmeny, ale zostane na obrazovke.
     * Táto funkcia sa používa len v dialógoch.
     */
    fun discardChangesAndStay() {
        // Resetuje localContact na pôvodné hodnoty
        localContact = originalContact.copy()
    }

    /**
     * Uloží zmeny a naviguje späť.
     */
    fun saveContactAndNavigateBack() {
        viewModel.updateContact(localContact)
        showUnsavedChangesDialog = false
        // 🎯 Kľúčová zmena: Ak sa úspešne uloží cez dialóg, zobrazíme Snackbar,
        // ale musíme ho stihnúť zobraziť pred onBack().
        showSavedSnackbar()
        onBack()
    }

    fun discardChangesAndNavigateBack() {
        showUnsavedChangesDialog = false
        onBack()
    }

    fun deleteContactAndNavigateBack() {
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
        // Tlačidlo ULOŽIŤ - UKLADÁ PRIAMO BEZ DIALÓGU
        IconButton(
            onClick = ::saveContactAndStay,
            enabled = hasUnsavedChanges
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
        // Pridanie SnackbarHost
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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

        // Dialóg NEULOŽENÝCH ZMIEN
        if (showUnsavedChangesDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedChangesDialog = false },
                title = { Text("Neuložené zmeny") },
                text = { Text("Máte neuložené zmeny. Chcete ich uložiť pred odchodom?") },

                confirmButton = {
                    // Uložiť a späť - VOLÁ NOVÚ FUNKCIU, KTORÁ ZOBRAZÍ SNACKBAR
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
            // Vylúčime filter "Všetky", ak je náhodou v options
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
