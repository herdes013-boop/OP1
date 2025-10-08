package com.example.op

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
    // ✅ OPRAVENÉ: Správne použitie derivedStateOf s viacerými kľúčmi
    val detailItem by remember(itemId, viewModel.passwordList, viewModel.ipList) {
        derivedStateOf {
            // Skús nájsť heslo, ak nie, skús nájsť IP adresu
            viewModel.passwordList.value.find { it.id == itemId }?.let {
                DetailItem.Password(it)
            } ?: viewModel.ipList.value.find { it.id == itemId }?.let {
                DetailItem.IpAddress(it)
            }
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                        // ✅ OPRAVENÉ: Použitá štandardná ikona bez 'AutoMirrored'
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val currentItem = detailItem) {
                is DetailItem.Password -> PasswordDetailContent(item = currentItem.item)
                is DetailItem.IpAddress -> IpDetailContent(item = currentItem.item)
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
                                // ✅ OPRAVENÉ: Posiela sa iba 'id'
                                is DetailItem.Password -> viewModel.deletePassword(itemToDelete.item.id)
                                // ✅ OPRAVENÉ: Volá sa správna metóda a posiela sa iba 'id'
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

@Composable
private fun PasswordDetailContent(item: PasswordItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item.username?.takeIf { it.isNotBlank() }?.let {
            DetailRow(label = "Používateľské meno", value = it, canCopy = true)
        }
        DetailRow(label = "Heslo", value = item.password, canCopy = true)
        item.notes?.takeIf { it.isNotBlank() }?.let {
            DetailRow(label = "Poznámky", value = it, canCopy = false)
        }
    }
}

@Composable
private fun IpDetailContent(item: IpItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailRow(label = "IP Adresa", value = item.ipAddress, canCopy = true)
        item.notes?.takeIf { it.isNotBlank() }?.let {
            DetailRow(label = "Poznámky", value = it, canCopy = false)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, canCopy: Boolean) {
    val clipboardManager = LocalClipboardManager.current

    Column {
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
            if (canCopy) {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(value)) }) {
                    Icon(Icons.Default.ContentCopy, "Skopírovať")
                }
            }
        }
    }
}
