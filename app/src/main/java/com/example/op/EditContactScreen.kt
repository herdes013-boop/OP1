// Súbor: EditContactScreen.kt
package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // <-- SPRÁVNY IMPORT
import androidx.compose.foundation.lazy.items      // <-- SPRÁVNY IMPORT
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.google.accompanist.flowlayout.FlowRow // <-- SPRÁVNY IMPORT

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
    var localContactState by remember { mutableStateOf<ContactItem?>(null) }
    var originalContact by remember { mutableStateOf<ContactItem?>(null) }

    LaunchedEffect(contactId) {
        val fetchedItem = viewModel.getContactById(contactId)
        localContactState = fetchedItem
        originalContact = fetchedItem?.copy()
    }

    if (localContactState == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    localContactState?.let { localContact ->
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showUnsavedChangesDialog by remember { mutableStateOf(false) }
        var showMenu by remember { mutableStateOf(false) }
        var showFunctionSelectionDialog by remember { mutableStateOf(false) }

        val hasUnsavedChanges by remember(localContact, originalContact) {
            derivedStateOf { localContact != originalContact }
        }

        fun saveContactAndGoBack() {
            viewModel.updateContact(localContact)
            onBack()
        }

        fun saveChanges() {
            viewModel.updateContact(localContact)
            originalContact = localContact.copy()
        }

        fun handleBackNavigation() {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog = true
            } else {
                onBack()
            }
        }

        LaunchedEffect(hasUnsavedChanges, localContact.firstName) {
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = "Upraviť kontakt",
                    navigationIcon = {
                        IconButton(onClick = ::handleBackNavigation) {
                            Icon(Icons.Default.ArrowBack, "Naspäť")
                        }
                    },
                    actions = {
                        if (hasUnsavedChanges && localContact.firstName.isNotBlank()) {
                            Button(
                                onClick = ::saveChanges,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
                    value = localContact.firstName,
                    onValueChange = { localContactState = localContact.copy(firstName = it) },
                    label = { Text("Meno *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = localContact.firstName.isBlank()
                )
                OutlinedTextField(
                    value = localContact.lastName,
                    onValueChange = { localContactState = localContact.copy(lastName = it) },
                    label = { Text("Priezvisko") },
                    modifier = Modifier.fillMaxWidth()
                )

                FunctionSelector(
                    allFunctions = viewModel.allContactFunctions,
                    selectedFunctionIds = localContact.functionIds,
                    onOpenDialog = { showFunctionSelectionDialog = true }
                )

                OutlinedTextField(
                    value = localContact.phone ?: "",
                    onValueChange = { localContactState = localContact.copy(phone = it) },
                    label = { Text("Telefónne číslo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.email ?: "",
                    onValueChange = { localContactState = localContact.copy(email = it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = localContact.notes ?: "",
                    onValueChange = { localContactState = localContact.copy(notes = it) },
                    label = { Text("Poznámky") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                ChannelDropdown(
                    selectedChannel = localContact.channel,
                    onChannelSelected = { localContactState = localContact.copy(channel = it) },
                    channelOptions = viewModel.channelOptions
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp)
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
                text = { Text("Naozaj chcete natrvalo odstrániť kontakt \"${originalContact?.getFullName()}\"?") },
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

        if (showFunctionSelectionDialog) {
            FunctionSelectionDialog(
                allFunctions = viewModel.allContactFunctions,
                selectedIds = localContact.functionIds,
                onDismiss = { showFunctionSelectionDialog = false },
                onConfirm = { newSelectedIds ->
                    localContactState = localContact.copy(functionIds = newSelectedIds)
                    showFunctionSelectionDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FunctionSelector(
    allFunctions: List<ContactFunction>,
    selectedFunctionIds: List<String>,
    onOpenDialog: () -> Unit,
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
                    FlowRow( // <-- BEZ DLHEJ CESTY
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
                imageVector = Icons.Default.ArrowDropDown, // <-- BEZ DLHEJ CESTY
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
    onConfirm: (List<String>) -> Unit,
) {
    var tempSelectedIds by remember { mutableStateOf(selectedIds.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vyberte funkcie") },
        text = {
            LazyColumn { // <-- OPRAVENÉ
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
