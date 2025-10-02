package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.* import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageChannelsScreen(
    viewModel: ContactsViewModel,
    onBack: () -> Unit
) {
    // 🎯 Channels je mutableStateListOf, takže sa automaticky aktualizuje
    val channels = viewModel.channelOptions
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Správa Kanálov") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Pridať kanál")
            }
        }
    ) { paddingValues ->

        if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Zatiaľ nemáte definované žiadne kanály.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Používame Channel ako kľúč, aby sa LazyColumn správne re-renderoval
                items(channels, key = { it }) { channel ->
                    ChannelListItem(
                        channel = channel,
                        // Volá funkciu definovanú vo ContactsViewModel
                        onDelete = { viewModel.removeChannel(channel) }
                    )
                    Divider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddChannelDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newChannel ->
                viewModel.addChannel(newChannel)
                showAddDialog = false
            }
        )
    }
}

// ----------------------------------------------------
// Komponenta pre jednu položku kanála
// ----------------------------------------------------

@Composable
fun ChannelListItem(channel: String, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(channel) },
        leadingContent = { Icon(Icons.Default.Label, contentDescription = "Kanál") },
        trailingContent = {
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Odstrániť", tint = MaterialTheme.colorScheme.error)
            }
        },
        modifier = Modifier.fillMaxWidth().clickable { /* Zatiaľ bez editácie, len mazanie */ }
    )

    // Dialóg na potvrdenie mazania
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Potvrdenie odstránenia") },
            text = { Text("Naozaj chcete odstrániť kanál '$channel'? Kontakty, ktoré ho používali, budú aktualizované na predvolený kanál (ak existuje).") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Odstrániť")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Zrušiť")
                }
            }
        )
    }
}

// ----------------------------------------------------
// Komponenta pre dialóg pridania kanála
// ----------------------------------------------------

@Composable
fun AddChannelDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newChannelName by remember { mutableStateOf("") }
    val isNameValid = newChannelName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať nový kanál") },
        text = {
            Column {
                Text("Zadajte názov nového kanála:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newChannelName,
                    onValueChange = { newChannelName = it },
                    label = { Text("Názov kanála") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newChannelName) },
                enabled = isNameValid
            ) {
                Text("Pridať")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}
