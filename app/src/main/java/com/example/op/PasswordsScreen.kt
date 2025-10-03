package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

/**
 * Hlavná obrazovka pre zobrazenie a správu hesiel a IP adries.
 * Používa TabRow pre prepínanie medzi dvomi typmi dát.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(
    navController: NavController,
    // Správna inicializácia View Modelu
    viewModel: PasswordsViewModel = viewModel()
) {
    // Získanie dát z View Modelu ako stav
    val passwords by viewModel.passwordList.collectAsState()
    val ipAddresses by viewModel.ipList.collectAsState()

    // Lokálny stav pre riadenie vybranej záložky
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Heslá", "IP Adresy")

    // !!!!! TU JE KĽÚČOVÁ ZMENA - PRIDANIE TYPU List<Any> !!!!!
    val currentData: List<Any> = when (selectedTabIndex) {
        0 -> passwords
        1 -> ipAddresses
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Môj Trezor") },
                actions = {
                    IconButton(onClick = { /* TODO: Implementovať vyhľadávanie */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Hľadať")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Navigácia na pridanie nového záznamu */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Pridať")
            }
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

                // TAB BAR (Záložky)
                TabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Zobrazenie zoznamu alebo správy o prázdnom stave
                if (currentData.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Zatiaľ žiadne záznamy.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                } else {
                    // ZOZNAM DÁT
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (selectedTabIndex) {
                            0 -> {
                                items(passwords, key = { it.id }) { item ->
                                    PasswordListItem(item)
                                }
                            }
                            1 -> {
                                items(ipAddresses, key = { it.id }) { item ->
                                    IpListItem(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// =================================================================================================
// KOMPONENTY ZÁZNAMOV
// =================================================================================================

/**
 * Položka v zozname pre zobrazenie Hesla.
 */
@Composable
fun PasswordListItem(item: PasswordItem) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigácia na detail */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Názov
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Prihlasovacie meno (ak existuje)
                item.username?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tlačidlo na kopírovanie hesla
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(item.password))
                    // TODO: Zobraziť Snackbar s oznámením o skopírovaní
                }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Kopírovať heslo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Položka v zozname pre zobrazenie IP Adresy.
 */
@Composable
fun IpListItem(item: IpItem) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigácia na detail */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Názov siete/zariadenia
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // IP Adresa
                Text(
                    text = item.ipAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tlačidlo na kopírovanie IP adresy
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(item.ipAddress))
                    // TODO: Zobraziť Snackbar s oznámením o skopírovaní
                }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Kopírovať IP adresu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
