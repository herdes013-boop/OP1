package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // Používame základnú šípku
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIpScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var ipAddress by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pridať novú IP Adresu") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Zmena na Icons.Default.ArrowBack
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CustomGreen,
                    titleContentColor = CustomOnGreen,
                    navigationIconContentColor = CustomOnGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pole pre Názov
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Názov (napr. Web Server, Domáca VPN)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Pole pre IP Adresu
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Adresa (napr. 192.168.1.1)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Pole pre Popis
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Popis/Poznámky") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            // Tlačidlo Uložiť
            Button(
                onClick = {
                    // TODO: Implementovať logiku uloženia novej IpItem do databázy
                    // a navigovať späť
                    println("Uložiť IP: $title, $ipAddress")
                    navController.popBackStack()
                },
                enabled = title.isNotBlank() && ipAddress.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Uložiť IP Adresu")
            }
        }
    }
}
