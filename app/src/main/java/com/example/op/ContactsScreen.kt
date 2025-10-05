package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// ... (getChannelIcon a ContactListItem ostávajú bez zmeny)
fun getChannelIcon(channel: String?): ImageVector {
    return when (channel) {
        "Jednotka" -> Icons.Filled.LooksOne
        "Dvojka" -> Icons.Filled.LooksTwo
        "24" -> Icons.Filled.Newspaper
        "Sport" -> Icons.Filled.SportsSoccer
        "Iné" -> Icons.Filled.OtherHouses
        else -> Icons.Filled.Person
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListItem(contact: ContactItem, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getChannelIcon(contact.channel),
                contentDescription = contact.channel ?: "Kontakt",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.getFullName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                contact.function?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}


// --------------------------------------------------
// Hlavná obrazovka pre kontakty - S OPRAVOU
// --------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val searchQuery = viewModel.searchQuery
    val selectedTabFilter = viewModel.selectedTabFilter
    val ALL_CHANNELS_FILTER = "Všetky"
    val contacts = viewModel.displayedContacts
    val categories = viewModel.channelOptions.toList()
    val selectedTabIndex = categories.indexOf(selectedTabFilter)

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // =========== KĽÚČOVÁ ZMENA JE TU ===========
            // Použijeme TabRow namiesto ScrollableTabRow
            TabRow(
                selectedTabIndex = if (selectedTabIndex == -1) 0 else selectedTabIndex,
            ) {
                categories.forEach { title ->
                    Tab(
                        selected = selectedTabFilter == title,
                        onClick = {
                            if (selectedTabFilter == ALL_CHANNELS_FILTER && title != ALL_CHANNELS_FILTER) {
                                viewModel.updateSearchQuery("")
                            }
                            viewModel.updateSelectedTabFilter(title)
                        },
                        // Pridané maxLines a overflow pre prípad veľmi dlhého textu
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                    )
                }
            }
            // ============================================

            if (selectedTabFilter == ALL_CHANNELS_FILTER) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Vyhľadať v kontaktoch...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Vyhľadať")
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Vymazať vyhľadávanie")
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (contacts.isEmpty()) {
                val message = if (selectedTabFilter == ALL_CHANNELS_FILTER && searchQuery.isNotBlank()) {
                    "Nenašli sa žiadne kontakty pre vyhľadávanie \"$searchQuery\"."
                } else if (selectedTabFilter != ALL_CHANNELS_FILTER) {
                    "V kanáli \"$selectedTabFilter\" zatiaľ nie sú žiadne kontakty."
                } else {
                    "Zatiaľ nemáte žiadne kontakty."
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 4.dp,
                        bottom = 80.dp // Padding pre FAB
                    )
                ) {
                    items(contacts, key = { it.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            onItemClick = {
                                navController.navigate(Routes.editContact(contact.id))
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                viewModel.resetForm()
                navController.navigate(Routes.ADD_CONTACT)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Pridať kontakt")
        }
    }
}
