package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // ✅ Nový import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.op.ui.theme.TelekomMagenta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionsScreen(
    navController: NavController,
    contactsViewModel: ContactsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
) {
    val functions = contactsViewModel.allContactFunctions

    // --- Nové stavy pre dialógy ---
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) } // ✅ Stav pre editovací dialóg
    var functionToEdit by remember { mutableStateOf<ContactFunction?>(null) } // ✅ Stav pre funkciu, ktorú upravujeme

    // Nastavenie Top Baru
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(TopBarState(isVisible = false))
    }

    // --- Zobrazenie dialógov ---
    if (showAddDialog) {
        AddFunctionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { functionName ->
                contactsViewModel.addContactFunction(functionName)
                showAddDialog = false
            }
        )
    }

    // ✅ Zobrazenie nového editovacieho dialógu
    if (showEditDialog && functionToEdit != null) {
        EditFunctionDialog(
            function = functionToEdit!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                contactsViewModel.updateContactFunction(functionToEdit!!.id, newName)
                showEditDialog = false
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spravovať funkcie") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Naspäť")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelekomMagenta,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Pridať funkciu")
            }
        }
    ) { paddingValues ->
        if (functions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Zatiaľ neboli pridané žiadne funkcie.")
            }
        } else {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            ) {
                items(functions, key = { it.id }) { function ->
                    ListItem(
                        headlineContent = { Text(function.name) },
                        trailingContent = {
                            // ✅ Pridali sme Row pre viac ikoniek
                            Row {
                                IconButton(onClick = {
                                    functionToEdit = function
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, "Upraviť funkciu", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { contactsViewModel.removeContactFunction(function.id) }) {
                                    Icon(Icons.Default.Delete, "Zmazať funkciu", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun AddFunctionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať novú funkciu") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Názov funkcie") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text("Pridať")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}

// ✅ NOVÝ KOMPONENT PRE EDITOVACÍ DIALÓG
@Composable
private fun EditFunctionDialog(
    function: ContactFunction,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    // Predvyplníme text aktuálnym názvom funkcie
    var text by remember { mutableStateOf(function.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upraviť názov funkcie") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Názov funkcie") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank() && text != function.name // Uložiť len ak je text iný a nie je prázdny
            ) {
                Text("Uložiť")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}
