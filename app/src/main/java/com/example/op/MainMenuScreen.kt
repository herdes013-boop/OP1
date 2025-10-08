package com.example.op

import com.example.op.ui.theme.TelekomMagenta
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.example.op.ui.theme.TelekomMagenta // Pre prístup k farbe
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // --- 1. Top Bar / Hero sekcia ---
        TopAppBar(
            title = {
                // ... titulok zostáva rovnaký ...
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Domov", // ZMENENÉ PODĽA VAŠEJ POŽIADAVKY
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            },

            // 2. KROK: PRIDÁME BLOK PRE NAVIGAČNÚ IKONU
            navigationIcon = {
                IconButton(onClick = {
                    // Tento príkaz nás neskôr presmeruje na obrazovku nastavení
                    navController.navigate("settings")
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            },

            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TelekomMagenta,
                titleContentColor = Color.White,

                // 3. KROK: PRIDÁME FARBU PRE NOVÚ IKONU
                navigationIconContentColor = Color.White,

                actionIconContentColor = Color.White
            ),
            actions = {
                // ... ikona profilu zostáva bez zmeny ...
                IconButton(onClick = { /* Navigácia na profil */ }) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profil",
                    )
                }
            }
        )

        // --- Zvyšok kódu zostáva bez zmeny ---

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
            // Zmeníme usporiadanie, aby sa tlačidlá roztiahli
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Pridá medzery medzi tlačidlá
        ) {
            QuickActionButton(
                label = "Nové heslo",
                icon = Icons.Default.Key,
                onClick = { navController.navigate("add_password") },
                modifier = Modifier.weight(1f) // Každé tlačidlo zaberie 1/3 šírky
            )
            QuickActionButton(
                label = "Nový kontakt",
                icon = Icons.Default.Phone,
                onClick = { navController.navigate("add_contact") },
                modifier = Modifier.weight(1f)
            )
            // ✅ KROK 1: PRIDÁME TOTO TLAČIDLO SPÄŤ
            QuickActionButton(
                label = "Návody", // Text tlačidla
                icon = Icons.Default.Menu, // Použijeme ikonu Menu
                onClick = {
                    navController.navigate(Screen.Tutorials.route) // Prejde na obrazovku s návodmi
                },
                modifier = Modifier.weight(1f) // Zaberie poslednú tretinu
            )
        }
    }
}

// Pomocná komponenta zostáva bez zmeny
@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedButton(
        onClick = onClick,
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
