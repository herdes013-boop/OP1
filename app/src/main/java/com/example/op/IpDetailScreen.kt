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

@Composable
fun IpDetailScreen(
    modifier: Modifier = Modifier,
    ipId: String, // Zmenené z passwordId
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    // Berieme si dáta z ipList
    val ips by viewModel.ipList.collectAsState()

    // Hľadáme správnu položku v zozname IP adries
    val ipItem = remember(ipId, ips) {
        ips.find { it.id == ipId }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(ipItem) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = ipItem?.name ?: "Detail IP", // Zmenený titulok
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

    // Ak sa položka nenájde, zobrazíme chybu
    if (ipItem == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chyba: IP adresa sa nenašla.")
        }
        return
    }

    // Aplikujeme odsadenie od Scaffold
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Zobrazujeme polia pre IpItem
            DetailItem(label = "Názov", value = ipItem.name)
            DetailItem(label = "IP Adresa", value = ipItem.ipAddress)
            ipItem.notes?.let {
                if (it.isNotBlank()) {
                    DetailItem(label = "Poznámky", value = it)
                }
            }
        }

        // Dropdown menu pre Úpravu/Zmazanie
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                        viewModel.deleteIpAddress(ipId) // Používame správnu funkciu
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

// Pomocná funkcia, ktorá zostáva rovnaká
@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.SemiBold,
        )
        Divider()
    }
}
