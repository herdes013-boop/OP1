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
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navController: NavController,
    viewModel: ContactsViewModel
) {
    // 1. STAV PRE DIALÓGOVÉ OKNO
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Formulár je "špinavý" (dirty) ak má vyplnené aspoň Meno alebo Priezvisko.
    // Používame rovnakú podmienku pre validitu Uložiť aj pre kontrolu pri Späť.
    val isFormDirty = viewModel.formFirstName.isNotBlank() ||
            viewModel.formLastName.isNotBlank()

    val saveContactAndGoBack: () -> Unit = {
        viewModel.saveNewContact()
        navController.popBackStack()
    }

    val performDiscardAndGoBack: () -> Unit = {
        // Resetujeme stav formulára a ideme späť
        viewModel.resetForm()
        navController.popBackStack()
    }

    val onBackClicked: () -> Unit = {
        if (isFormDirty) {
            // Ak je formulár "špinavý", zobrazíme dialóg.
            showDiscardDialog = true
        } else {
            // Ak nie je špinavý, ideme rovno späť (žiadne zmeny na stratenie).
            performDiscardAndGoBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pridať nový kontakt") },
                navigationIcon = {
                    // 2. SPÄŤ TLAČIDLO TERAZ VOLÁ NOVÚ LOGIKU
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                }
            )
        },
        floatingActionButton = {
            // Formulár je platný, ak má vyplnené aspoň Meno alebo Priezvisko
            val isFormValid = isFormDirty

            FloatingActionButton(
                // Ak je formulár neplatný, kliknutie nerobí nič ({})
                onClick = if (isFormValid) saveContactAndGoBack else ({}),
                // Zakázanie tlačidla (zmena farby a elevácie), ak formulár nie je platný
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                elevation = if (isFormValid) FloatingActionButtonDefaults.elevation() else FloatingActionButtonDefaults.loweredElevation()
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

    // 3. DIALÓGOVÉ OKNO PRE POTVRDENIE ZAHODENIA ZMIEN
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                // Zrušíme dialóg, zostaneme na obrazovke
                showDiscardDialog = false
            },
            title = {
                Text("Zahodiť zmeny?")
            },
            text = {
                Text("Máte neuložené zmeny. Naozaj ich chcete zahodiť a vrátiť sa späť?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        performDiscardAndGoBack() // Zahodíme a ideme späť
                    }
                ) {
                    Text("Zahodiť")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false // Zrušíme, zostaneme
                    }
                ) {
                    Text("Zrušiť")
                }
            }
        )
    }
}

@Composable
fun ContactForm(viewModel: ContactsViewModel) {

    Spacer(modifier = Modifier.height(16.dp))

    // Meno
    OutlinedTextField(
        // OPRAVENÉ
        value = viewModel.formFirstName,
        onValueChange = viewModel::updateFirstName,
        label = { Text("Meno") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Priezvisko
    OutlinedTextField(
        // OPRAVENÉ
        value = viewModel.formLastName,
        onValueChange = viewModel::updateLastName,
        label = { Text("Priezvisko") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Funkcia/Pozícia
    OutlinedTextField(
        // OPRAVENÉ
        value = viewModel.formFunction,
        onValueChange = viewModel::updateFunction,
        label = { Text("Funkcia") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Telefónne číslo
    OutlinedTextField(
        // OPRAVENÉ
        value = viewModel.formPhone,
        onValueChange = viewModel::updatePhone,
        label = { Text("Telefónne číslo") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // E-mail
    OutlinedTextField(
        // OPRAVENÉ
        value = viewModel.formEmail,
        onValueChange = viewModel::updateEmail,
        label = { Text("E-mail") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Kanál (Rozbaľovacia ponuka)
    ChannelDropdownField(viewModel = viewModel)

    Spacer(modifier = Modifier.height(8.dp))

    // Poznámky
    OutlinedTextField(
        // OPRAVENÉ
        value = viewModel.formNotes,
        onValueChange = viewModel::updateNotes,
        label = { Text("Poznámky") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDropdownField(viewModel: ContactsViewModel) {
    var isExpanded by remember { mutableStateOf(false) }

    // Získame možnosti pre formulár (vylúčime "Všetky", ktoré je len pre filtrovanie)
    val dropdownOptions = remember {
        viewModel.channelOptions.drop(1)
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            // OPRAVENÉ
            value = viewModel.formChannel,
            onValueChange = {}, // Hodnota sa mení len výberom z menu
            readOnly = true,
            label = { Text("Kanál") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            dropdownOptions.forEach { channel ->
                DropdownMenuItem(
                    text = { Text(channel) },
                    onClick = {
                        viewModel.updateChannel(channel)
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
