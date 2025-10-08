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

@Composable
fun AddEditIpAddressScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
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
    // NOVÉ: Premenná pre uloženie stavu poľa pre poznámky
    var notes by remember(initialIpItem) { mutableStateOf(initialIpItem?.notes ?: "") }

    val isFormValid = name.isNotBlank() && ipAddress.isNotBlank()

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        // NOVÉ: Textové pole pre poznámky
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Poznámky (voliteľné)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (isEditing) {
                    // UPRAVENÉ: Pri úprave posielame aj poznámky
                    val updatedItem = initialIpItem!!.copy(
                        name = name,
                        ipAddress = ipAddress,
                        notes = notes.ifBlank { null } // Ak je pole prázdne, uloží sa null
                    )
                    viewModel.updateIpAddress(updatedItem)
                } else {
                    // UPRAVENÉ: Pri pridaní posielame aj poznámky
                    viewModel.addIpAddress(
                        name = name,
                        ipAddress = ipAddress,
                        notes = notes.ifBlank { null } // Ak je pole prázdne, uloží sa null
                    )
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
