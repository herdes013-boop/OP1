package com.example.op

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.op.ui.theme.TelekomMagenta // Tento import si prípadne upravte podľa vášho projektu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesManagementScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
) {
    // Používame spravovateľné kategórie z TutorialsViewModel
    val categories = tutorialsViewModel.managedCategories

    // Stavy pre dialógy
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<String?>(null) }

    // Nastavenie Top Baru
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(TopBarState(isVisible = false))
    }

    // Zobrazenie dialógu na pridanie
    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { categoryName ->
                tutorialsViewModel.addCategory(categoryName)
                showAddDialog = false
            }
        )
    }

    // Zobrazenie dialógu na úpravu
    if (showEditDialog && categoryToEdit != null) {
        EditCategoryDialog(
            categoryName = categoryToEdit!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName ->
                tutorialsViewModel.updateCategory(categoryToEdit!!, newName)
                showEditDialog = false
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spravovať kategórie návodov") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Naspäť")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelekomMagenta, // Farba z vášho druhého súboru
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Pridať kategóriu")
            }
        }
    ) { paddingValues ->
        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Zatiaľ neboli pridané žiadne kategórie.")
            }
        } else {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories, key = { it }) { category ->
                    ListItem(
                        headlineContent = { Text(category) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    categoryToEdit = category
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, "Upraviť kategóriu", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { tutorialsViewModel.removeCategory(category) }) {
                                    Icon(Icons.Default.Delete, "Zmazať kategóriu", tint = MaterialTheme.colorScheme.error)
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
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pridať novú kategóriu") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Názov kategórie") },
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

@Composable
private fun EditCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(categoryName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upraviť názov kategórie") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Názov kategórie") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim()) },
                enabled = text.isNotBlank() && text != categoryName
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
