package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactDetailScreen(
    modifier: Modifier = Modifier,
    contactId: Int,
    viewModel: ContactsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (Int) -> Unit,
    onBack: () -> Unit
) {
    val contact by viewModel.selectedContact.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(contactId) {
        viewModel.loadContactDetail(contactId)
    }

    LaunchedEffect(contact) {
        contact?.let {
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = it.getFullName(),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Naspäť")
                        }
                    },
                    // ✅✅✅ ZAČIATOK OPRAVY ✅✅✅
                    actions = {
                        // Ikona aj menu sú teraz spolu v jednom bloku
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Viac")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Upraviť") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEdit(it.id)
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, "Upraviť") }
                            )
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
                    // ✅✅✅ KONIEC OPRAVY ✅✅✅
                )
            )
        }
    }

    // Obsah obrazovky je teraz jednoduchší
    // Vonkajší Box už nie je potrebný
    contact?.let { c ->
        Column(
            modifier = modifier // Použijeme modifier z parametra, ktorý obsahuje padding
                .fillMaxSize()
                .padding(16.dp) // Pridáme náš vlastný vnútorný padding
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Zobrazovacie polia
            DetailItem(label = "Meno a Priezvisko", value = c.getFullName())
            c.function?.let { DetailItem(label = "Funkcia", value = it) }
            c.phone?.let { DetailItem(label = "Telefón", value = it, isClickable = true) }
            c.email?.let { DetailItem(label = "Email", value = it, isClickable = true) }
            c.channel?.let { DetailItem(label = "Kanál", value = it) }
            c.notes?.let { DetailItem(label = "Poznámky", value = it) }
        }
    }

    // Dialóg pre potvrdenie zmazania zostáva
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Odstrániť kontakt?") },
            text = { Text("Naozaj chcete natrvalo odstrániť kontakt \"${contact?.getFullName()}\"?") },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        contact?.let { viewModel.removeContact(it) }
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Odstrániť") }
            }
        )
    }
}

// Pomocná komponenta zostáva bez zmeny
@Composable
private fun DetailItem(label: String, value: String, isClickable: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                color = if (isClickable) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                fontWeight = if (isClickable) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}
