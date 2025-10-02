package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navController: NavController,
    viewModel: ContactsViewModel
) {
    // Definícia navigačných funkcií ako lambdy s explicitným typom () -> Unit
    val saveContactAndGoBack: () -> Unit = {
        viewModel.saveNewContact() // ✅ Volanie správnej funkcie
        navController.popBackStack()
    }

    val discardChangesAndGoBack: () -> Unit = {
        // Zatiaľ stačí len resetovať stav a ísť späť
        viewModel.resetForm()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pridať nový kontakt") },
                navigationIcon = {
                    IconButton(onClick = discardChangesAndGoBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                }
            )
        },
        floatingActionButton = {
            // ✅ FIX: Použitie ne-nullable Value vlastností pre bezpečné volanie isNotBlank()
            val isFormValid = viewModel.formFirstNameValue.isNotBlank() ||
                    viewModel.formLastNameValue.isNotBlank()

            FloatingActionButton(
                // Ak je formulár neplatný, kliknutie nerobí nič ({})
                onClick = if (isFormValid) saveContactAndGoBack else ({}),
                // Zakázanie tlačidla, ak formulár nie je platný
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Uložiť kontakt")
            }
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Formulár pre pridanie/editáciu kontaktu
            ContactForm(viewModel = viewModel)
        }
    }
}

@Composable
fun ContactForm(viewModel: ContactsViewModel) {

    Spacer(modifier = Modifier.height(16.dp))

    // Meno
    OutlinedTextField(
        // ✅ FIX: Použitie formFirstNameValue (ne-nullable)
        value = viewModel.formFirstNameValue,
        onValueChange = viewModel::updateFirstName,
        label = { Text("Meno") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Priezvisko
    OutlinedTextField(
        // ✅ FIX: Použitie formLastNameValue (ne-nullable)
        value = viewModel.formLastNameValue,
        onValueChange = viewModel::updateLastName,
        label = { Text("Priezvisko") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Funkcia/Pozícia
    OutlinedTextField(
        // ✅ FIX: Použitie formFunctionValue (ne-nullable)
        value = viewModel.formFunctionValue,
        onValueChange = viewModel::updateFunction,
        label = { Text("Funkcia") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Telefónne číslo
    OutlinedTextField(
        // ✅ FIX: Použitie formPhoneValue (ne-nullable)
        value = viewModel.formPhoneValue,
        onValueChange = viewModel::updatePhone,
        label = { Text("Telefónne číslo") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // E-mail
    OutlinedTextField(
        // ✅ FIX: Použitie formEmailValue (ne-nullable)
        value = viewModel.formEmailValue,
        onValueChange = viewModel::updateEmail,
        label = { Text("E-mail") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Kanál (pre jednoduchosť zatiaľ ako textové pole, malo by byť dropdown)
    OutlinedTextField(
        // ✅ FIX: Použitie formChannelValue (ne-nullable)
        value = viewModel.formChannelValue,
        onValueChange = viewModel::updateChannel,
        label = { Text("Kanál (napr. Jednotka, Dvojka)") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Poznámky
    OutlinedTextField(
        // ✅ FIX: Použitie formNotesValue (ne-nullable)
        value = viewModel.formNotesValue,
        onValueChange = viewModel::updateNotes,
        label = { Text("Poznámky") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))
}
