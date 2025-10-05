package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ZMENA: Obrazovka už nepoužíva @OptIn, keďže nemá vlastný TopAppBar
@Composable
fun AddEditIpAddressScreen(
    // ========== ZMENA: PRIDANÝ NOVÝ PARAMETER `modifier` ==========
    modifier: Modifier = Modifier,
    // ==============================================================
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel, // Potrebujeme pre nastavenie TopBar
    ipId: String? = null
) {
    val isEditing = ipId != null
    val initialIpItem by remember(ipId) {
        derivedStateOf {
            if (isEditing) viewModel.getIpAddressById(ipId!!) else null
        }
    }

    var name by remember(initialIpItem) { mutableStateOf(initialIpItem?.name ?: "") }
    var ipAddress by remember(initialIpItem) { mutableStateOf(initialIpItem?.ipAddress ?: "") }

    val isFormValid = name.isNotBlank() && ipAddress.isNotBlank()

    // DYNAMICKÉ NASTAVENIE HORNEJ LIŠTY
    LaunchedEffect(isEditing) {
        val title = if (isEditing) "Upraviť IP adresu" else "Nová IP adresa"
        sharedViewModel.setTopBarState(
            TopBarState(
                title = title,
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                }
            )
        )
    }

    if (isEditing && initialIpItem == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // ========== ZMENA: OBRAZOVKA UŽ NEPOUŽÍVA SCAFFOLD ==========
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp), // Jednoduchý padding, zvyšok rieši `modifier`
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
