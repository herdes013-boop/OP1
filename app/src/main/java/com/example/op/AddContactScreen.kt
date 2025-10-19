// Súbor: AddContactScreen.kt

package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    // ✅ KROK 1: ÚPRAVA LOKÁLNEHO STAVU
    // Dátová trieda ContactItem má teraz iné parametre
    var localContact by remember {
        mutableStateOf(
            ContactItem(
                id = 0,
                firstName = "",
                lastName = "",
                functionIds = emptyList(), // Používame nový zoznam IDčok
                phone = null,
                email = null,
                channel = viewModel.channelOptions.firstOrNull() ?: "",
                notes = null
            )
        )
    }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showFunctionSelectionDialog by remember { mutableStateOf(false) } // Stav pre dialóg s funkciami

    // ✅ KROK 2: ÚPRAVA DETEKCIE ZMIEN
    // Kontrolujeme nové pole `functionIds` namiesto starého `function`
    val hasUnsavedChanges by remember(localContact) {
        derivedStateOf {
            localContact.firstName.isNotBlank() ||
                    localContact.lastName.isNotBlank() ||
                    localContact.functionIds.isNotEmpty() || // Zmenené
                    localContact.phone?.isNotBlank() == true ||
                    localContact.email?.isNotBlank() == true ||
                    localContact.notes?.isNotBlank() == true
        }
    }

    // Funkcie na uloženie a návrat zostávajú rovnaké
    fun saveContactAndGoBack() {
        viewModel.addContact(localContact)
        onBack()
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    // Správa horného panela zostáva rovnaká
    LaunchedEffect(hasUnsavedChanges) {
        sharedViewModel.setTopBarState(TopBarState(
            title = "Nový kontakt",
            navigationIcon = {
                IconButton(onClick = ::handleBackNavigation) {
                    Icon(Icons.Default.ArrowBack, "Naspäť")
                }
            },
            actions = {
                // Tlačidlo ULOŽIŤ sa zobrazí, len ak je meno vyplnené
                if (hasUnsavedChanges && localContact.firstName.isNotBlank()) {
                    Button(
                        onClick = ::saveContactAndGoBack,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("ULOŽIŤ")
                    }
                }
            }
        ))
    }

    BackHandler(onBack = ::handleBackNavigation)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Povinné polia
            OutlinedTextField(
                value = localContact.firstName,
                onValueChange = { localContact = localContact.copy(firstName = it) },
                label = { Text("Meno *") }, // Pridaná hviezdička
                modifier = Modifier.fillMaxWidth(),
                isError = localContact.firstName.isBlank() && hasUnsavedChanges
            )
            OutlinedTextField(
                value = localContact.lastName,
                onValueChange = { localContact = localContact.copy(lastName = it) },
                label = { Text("Priezvisko") },
                modifier = Modifier.fillMaxWidth()
            )

            // ✅ KROK 3: NOVÝ KOMPONENT NA VÝBER FUNKCIÍ
            FunctionSelector(
                allFunctions = viewModel.allContactFunctions,
                selectedFunctionIds = localContact.functionIds,
                onOpenDialog = { showFunctionSelectionDialog = true }
            )

            // Ostatné polia
            OutlinedTextField(
                value = localContact.phone ?: "",
                onValueChange = { localContact = localContact.copy(phone = it) },
                label = { Text("Telefónne číslo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.email ?: "",
                onValueChange = { localContact = localContact.copy(email = it) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.notes ?: "",
                onValueChange = { localContact = localContact.copy(notes = it) },
                label = { Text("Poznámky") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            ChannelDropdown(
                selectedChannel = localContact.channel,
                onChannelSelected = { localContact = localContact.copy(channel = it) },
                channelOptions = viewModel.channelOptions
            )
        }
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

    // ✅ KROK 4: ZOBRAZENIE DIALÓGU NA VÝBER FUNKCIÍ
    if (showFunctionSelectionDialog) {
        FunctionSelectionDialog(
            allFunctions = viewModel.allContactFunctions,
            selectedIds = localContact.functionIds,
            onDismiss = { showFunctionSelectionDialog = false },
            onConfirm = { newSelectedIds ->
                localContact = localContact.copy(functionIds = newSelectedIds)
                showFunctionSelectionDialog = false
            }
        )
    }
}

// ✅ KROK 5: NOVÝ KOMPONENT PRE ZOBRAZENIE A VÝBER FUNKCIÍ


// ✅ KROK 6: NOVÝ DIALÓG NA VÝBER FUNKCIÍ

// Vložte TOTO na koniec súboru AddContactScreen.kt

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FunctionSelector(
    allFunctions: List<ContactFunction>,
    selectedFunctionIds: List<String>,
    onOpenDialog: () -> Unit
) {
    val selectedFunctions = allFunctions.filter { it.id in selectedFunctionIds }

    OutlinedBox(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDialog),
        label = "Funkcie"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (selectedFunctions.isEmpty()) {
                    Text("Vybrať funkcie...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        selectedFunctions.forEach { function ->
                            SuggestionChip(
                                onClick = { /* Štítok nie je klikateľný */ },
                                label = { Text(function.name) }
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Vybrať funkcie",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FunctionSelectionDialog(
    allFunctions: List<ContactFunction>,
    selectedIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tempSelectedIds by remember { mutableStateOf(selectedIds.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vyberte funkcie") },
        text = {
            LazyColumn {
                items(allFunctions, key = { it.id }) { function ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelectedIds = if (function.id in tempSelectedIds) {
                                    tempSelectedIds - function.id
                                } else {
                                    tempSelectedIds + function.id
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = function.id in tempSelectedIds,
                            onCheckedChange = null
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(function.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tempSelectedIds.toList()) }) {
                Text("Potvrdiť")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}



