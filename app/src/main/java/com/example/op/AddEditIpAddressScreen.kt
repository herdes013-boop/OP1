package com.example.op

import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIpAddressScreen(
    navController: NavController,
    viewModel: PasswordsViewModel,
    ipId: String? = null
) {
    val isEditing = ipId != null
    // Načítame pôvodné dáta, ak upravujeme
    val initialIpItem by remember(ipId) {
        derivedStateOf {
            if (isEditing) viewModel.getIpAddressById(ipId!!) else null
        }
    }

    var name by remember(initialIpItem) { mutableStateOf(initialIpItem?.name ?: "") }
    var ipAddress by remember(initialIpItem) { mutableStateOf(initialIpItem?.ipAddress ?: "") }

    val isFormValid = name.isNotBlank() && ipAddress.isNotBlank()

    // Ak upravujeme neexistujúcu IP, vrátime sa späť
    if (isEditing && initialIpItem == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť IP adresu" else "Nová IP adresa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Názov zariadenia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Adresa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        val updatedItem = initialIpItem!!.copy(name = name, ipAddress = ipAddress)
                        viewModel.updateIpAddress(updatedItem)
                    } else {
                        viewModel.addIpAddress(name, ipAddress)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Uložiť")
            }
        }
    }
}
