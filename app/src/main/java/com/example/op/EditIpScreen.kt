// ✅✅✅ TOTO JE SPRÁVNA VERZIA IMPORTOV ✅✅✅
package com.example.op

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Vaša funkcia @Composable fun EditIpScreen(...) pokračuje tu


@Composable
fun EditIpScreen(
    modifier: Modifier = Modifier, // Prijíma modifier pre padding zo Scaffold-u
    ipId: String?,
    viewModel: PasswordsViewModel, // Potrebujeme ViewModel na prácu s dátami
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit,
) {
    // 1. Načítanie dát a príprava stavov
    val ipItemToEdit: IpItem? = remember(ipId) {
        if (ipId == null) {
            viewModel.createEmptyIpItem() // Budeme volať novú funkciu z ViewModelu
        } else {
            viewModel.getIpAddressById(ipId) // ✅ TOTO JE SPRÁVNY NÁZOV
        }
    }

    // Ak upravujeme neexistujúcu položku, vrátime sa späť
    if (ipItemToEdit == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var originalIpItem by remember { mutableStateOf(ipItemToEdit) }
    var localIpItem by remember { mutableStateOf(originalIpItem) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val hasUnsavedChanges = localIpItem != originalIpItem
    val isNewItem = ipId == null

    // 2. Funkcie pre ukladanie a navigáciu
    fun saveItemAndGoBack() {
        if (isNewItem) {
            viewModel.addIpAddress(localIpItem)
        } else {
            viewModel.updateIpAddress(localIpItem)
        }
        onBack()
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    // 3. Nastavenie TopAppBar-u (rovnaký princíp ako pri kontaktoch)
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = if (isNewItem) "Pridať IP" else "Upraviť IP",
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                }
            )
        )
    }

    LaunchedEffect(hasUnsavedChanges, isNewItem) {
        sharedViewModel.updateTopBarActions {
            // Zobrazí tlačidlo ULOŽIŤ, ak ide o novú položku alebo ak sú zmeny
            if (isNewItem || hasUnsavedChanges) {
                Button(
                    onClick = ::saveItemAndGoBack,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Zelená farba
                    )
                ) {
                    Text("ULOŽIŤ")
                }
            }
            // Menu pre zmazanie sa zobrazí iba pri úprave existujúcej položky
            if (!isNewItem) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Viac")
                }
            }
        }
    }

    // Zabezpečí správne fungovanie tlačidla "späť" v systéme
    BackHandler(onBack = ::handleBackNavigation)

    // 4. Hlavný obsah obrazovky
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = localIpItem.name,
                onValueChange = { localIpItem = localIpItem.copy(name = it) },
                label = { Text("Názov") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localIpItem.ipAddress,
                onValueChange = { localIpItem = localIpItem.copy(ipAddress = it) },
                label = { Text("IP Adresa") },
                modifier = Modifier.fillMaxWidth()
            )
            // Tlačidlá "Uložiť" a "Zmazať" sú odtiaľto odstránené!
        }

        // Dropdown menu je zarovnané v Boxe
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

    // 5. Dialógy pre potvrdenie
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Odstrániť položku?") },
            text = { Text("Naozaj chcete natrvalo odstrániť položku '${originalIpItem.name}'?") },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteIpAddress(originalIpItem.id)
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
            text = { Text("Máte neuložené zmeny. Chcete ich zahodiť?") },
            confirmButton = {
                Button(onClick = onBack) { Text("Zahodiť a odísť") }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedChangesDialog = false }) { Text("Zostať") }
            }
        )
    }
}
