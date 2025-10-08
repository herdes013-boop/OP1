package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Zovšeobecnený dátový model pre detail
sealed class DetailItem {
    data class Password(val item: PasswordItem) : DetailItem()
    data class IpAddress(val item: IpItem) : DetailItem()
}

@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    itemId: String?,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit,
) {
    val passwords by viewModel.passwordList.collectAsState()
    val ips by viewModel.ipList.collectAsState()

// 2. Na základe aktuálnych zoznamov nájdeme našu položku.
// `remember` tu zabezpečí, že hľadanie sa nespustí pri každej rekompozícii,
// ale iba vtedy, ak sa zmení `itemId` alebo obsah jedného zo zoznamov.
    val detailItem = remember(itemId, passwords, ips) {
        passwords.find { it.id == itemId }?.let { DetailItem.Password(it) }
            ?: ips.find { it.id == itemId }?.let { DetailItem.IpAddress(it) }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // ================== NOVÉ ==================
    // Pridávame SnackbarHostState pre zobrazenie notifikácie o kopírovaní
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // ==========================================

    // Nastavenie horného panelu
    LaunchedEffect(detailItem) {
        val currentItem = detailItem
        val title = when (currentItem) {
            is DetailItem.Password -> currentItem.item.name
            is DetailItem.IpAddress -> currentItem.item.name
            null -> "Načítava sa..."
        }

        sharedViewModel.setTopBarState(
            TopBarState(
                title = title,
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Späť")
                    }
                },
                actions = {
                    if (detailItem != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Možnosti")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Upraviť") },
                                onClick = {
                                    showMenu = false
                                    itemId?.let { onNavigateToEdit(it) }
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Zmazať") },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null) }
                            )
                        }
                    }
                }
            )
        )
    }

    // ================== UPRAVENÉ ==================
    // Pridávame `snackbarHost` do Scaffold-u
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // ==============================================
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ================== UPRAVENÉ ==================
            // Posielame `scope` a `snackbarHostState` ďalej do obsahu
            val onCopy: (String, String) -> Unit = { value, label ->
                scope.launch {
                    snackbarHostState.showSnackbar("'${label}' skopírované do schránky.")
                }
            }
            // ==============================================

            when (val currentItemValue = detailItem) {
                is DetailItem.Password -> {
                    key(currentItemValue.item) {
                        PasswordDetailContent(item = currentItemValue.item, onCopy = onCopy)
                    }
                }
                is DetailItem.IpAddress -> {
                    key(currentItemValue.item) {
                        IpDetailContent(item = currentItemValue.item, onCopy = onCopy)
                    }
                }
                null -> {
                    if (itemId != null) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        Text("Položka neexistuje.", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        val itemToDelete = detailItem
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Potvrdiť zmazanie") },
            text = { Text("Naozaj si prajete natrvalo zmazať túto položku?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            when (itemToDelete) {
                                is DetailItem.Password -> viewModel.deletePassword(itemToDelete.item.id)
                                is DetailItem.IpAddress -> viewModel.deleteIpAddress(itemToDelete.item.id)
                                null -> {}
                            }
                            showDeleteDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Zmazať") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            }
        )
    }
}

// ================== UPRAVENÉ ==================
// Funkcia teraz prijíma `onCopy` lambda funkciu
@Composable
private fun PasswordDetailContent(item: PasswordItem, onCopy: (String, String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item.username?.takeIf { it.isNotBlank() }?.let {
            DetailRow(label = "Používateľské meno", value = it, canCopy = true, onCopy = onCopy)
        }
        DetailRow(label = "Heslo", value = item.password, canCopy = true, onCopy = onCopy)
        item.notes?.takeIf { it.isNotBlank() }?.let {
            DetailRow(label = "Poznámky", value = it, canCopy = false, onCopy = onCopy)
        }
    }
}

// ================== UPRAVENÉ ==================
// Funkcia teraz prijíma `onCopy` lambda funkciu
@Composable
private fun IpDetailContent(item: IpItem, onCopy: (String, String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailRow(label = "IP Adresa", value = item.ipAddress, canCopy = true, onCopy = onCopy)
        item.notes?.takeIf { it.isNotBlank() }?.let {
            DetailRow(label = "Poznámky", value = it, canCopy = false, onCopy = onCopy)
        }
    }
}

// ================== UPRAVENÉ ==================
// Celá funkcia je prepracovaná
@Composable
private fun DetailRow(
    label: String,
    value: String,
    canCopy: Boolean,    onCopy: (String, String) -> Unit
) {
    // ✅ OPRAVENÝ RIADOK
    val clipboardManager = LocalClipboardManager.current

    val rowModifier = if (canCopy) {
        Modifier.clickable {
            clipboardManager.setText(AnnotatedString(value))
            onCopy(value, label)
        }
    } else {
        Modifier
    }

    Column(modifier = rowModifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            // Blok s ikonou bol odtiaľto zmazaný.
        }
    }
}
