package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight // 📌 OPRAVENÝ IMPORT
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nastavenia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 📌 NASTAVENIE: Správa Kanálov
            SettingItem(
                title = "Správa Kanálov",
                subtitle = "Pridať, odstrániť alebo upraviť kategórie kontaktov",
                icon = Icons.Default.Label,
                onClick = { navController.navigate("manage_channels") }
            )
            Divider(Modifier.padding(horizontal = 16.dp))

            // --- Budúce nastavenia sa pridajú SEM ---
        }
    }
}

// Opakovateľný komponent pre položku nastavenia
@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = title) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = "Viac") }
    )
}