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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color // Pridaný import pre pomocnú funkciu
import androidx.compose.foundation.text.selection.SelectionContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpDetailScreen(
    modifier: Modifier = Modifier,
    ipId: String,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val ips by viewModel.ipList.collectAsState()

    val ipItem = remember(ipId, ips) {
        ips.find { it.id == ipId }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(ipItem) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = ipItem?.name ?: "Detail IP",
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (ipItem != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Viac")
                        }
                    }
                }
            )
        )
    }

    if (ipItem == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chyba: IP adresa sa nenašla.")
        }
        return
    }

    // =================================================================
    // ✅ ÚPRAVA: Celý obsah vložíme do jednej hlavnej karty
    // =================================================================
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Použijeme našu centrálnu DetailCard
        DetailCard {
            // Zobrazujeme polia pre IpItem s medzerami a kopírovaním
            DetailItem(label = "Názov", value = ipItem.name, isValueSelectable = true)

            Spacer(Modifier.height(16.dp))
            DetailItem(label = "IP Adresa", value = ipItem.ipAddress, isValueSelectable = true)

            ipItem.notes?.let {
                if (it.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    DetailItem(label = "Poznámky", value = it, isValueSelectable = true)
                }
            }
        }
    }

    // Dropdown menu zostáva vonku, aby sa správne zobrazilo nad všetkým
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, end = 4.dp)
            .wrapContentSize(Alignment.TopEnd)
    ) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text("Upraviť") },
                onClick = {
                    showMenu = false
                    onNavigateToEdit(ipId)
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

    // Dialóg pre potvrdenie zmazania
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Zmazať IP adresu") },
            text = { Text("Naozaj chcete natrvalo zmazať položku '${ipItem.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteIpAddress(ipId)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Zmazať") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            }
        )
    }
}

// Pomocná funkcia, ktorá zostáva rovnaká, ale bez deliacej čiary
@Composable
private fun DetailItem(
    label: String,
    value: String,
    isValueSelectable: Boolean = false // Parameter na povolenie kopírovania
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(), // Zjednotený vzhľad s veľkými písmenami
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))

        // Ak je hodnota kopírovateľná, obalíme ju
        if (isValueSelectable) {
            SelectionContainer {
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
