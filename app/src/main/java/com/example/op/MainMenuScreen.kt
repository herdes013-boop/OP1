package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        // --- 1. Top Bar / Hero sekcia ---
        TopAppBar(
            title = {
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Dobrý deň,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Ján Novák",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            actions = {
                IconButton(onClick = { /* Navigácia na profil */ }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        )

        // --- 2. Kontajner s hlavným statusom (Elevated Card) ---
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Trezor",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Váš trezor je bezpečný.",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Posledná kontrola: pred 5 minútami",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // --- 3. Sekcia pre rýchle akcie ---
        Text(
            text = "Rýchle akcie",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ✅ OPRAVA: weight(1f) aplikujeme priamo v tomto Row
            QuickActionButton(
                label = "Nové heslo",
                icon = Icons.Default.Key,
                onClick = { navController.navigate("add_password") },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )

            // ✅ OPRAVA: weight(1f) aplikujeme priamo v tomto Row
            QuickActionButton(
                label = "Nový kontakt",
                icon = Icons.Default.Phone,
                onClick = { navController.navigate("add_contact") },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

// ----------------------------------------------------
// Pomocná komponenta pre rýchle tlačidlá - teraz prijíma modifikátor
// ----------------------------------------------------

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // ✅ Pridaný nový parameter modifier
) {
    ElevatedButton(
        onClick = onClick,
        // ✅ Modifier je teraz prenesený z volania Row
        modifier = modifier,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}