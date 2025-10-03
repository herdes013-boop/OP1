package com.example.op

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.op.IpItem // Tento riadok musí byť prítomný


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIpScreen(
    navController: NavController,
    ipId: String? // ID je teraz nepovinné (null pre novú položku)
) {
    // Stav pre názov a IP adresu
    var name by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("") }

    // TODO: Načítať existujúcu položku, ak ipId nie je null

    ScaffoldTemplate(
        header = {
            TopAppBar(
                title = {
                    Text(if (ipId == null) "Pridať IP" else "Upraviť IP")
                },
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Názov") }
            )
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Adresa") }
            )
            Button(onClick = {
                // TODO: Logika pre uloženie (pridanie alebo aktualizácia)

                navController.popBackStack()
            }) {
                Text("Uložiť")
            }

            if (ipId != null) {
                Button(
                    onClick = {
                        // TODO: Logika pre zmazanie položky
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Zmazať")
                }
            }
        }
    }
}
