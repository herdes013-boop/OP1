package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.op.ui.theme.TelekomMagenta // ✅ PRIDANÝ IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navController: NavController,
    viewModel: ContactsViewModel
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Formulár je "špinavý" (dirty) a zároveň platný, ak má vyplnené aspoň Meno alebo Priezvisko.
    val isFormValidAndDirty = viewModel.formFirstName.isNotBlank() || viewModel.formLastName.isNotBlank()

    val saveContactAndGoBack: () -> Unit = {
        viewModel.saveNewContact()
        navController.popBackStack()
    }

    val performDiscardAndGoBack: () -> Unit = {
        viewModel.resetForm()
        navController.popBackStack()
    }

    val onBackClicked: () -> Unit = {
        if (isFormValidAndDirty) {
            showDiscardDialog = true
        } else {
            performDiscardAndGoBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pridať nový kontakt") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Späť")
                    }
                },
                // ✅ KROK 1: PRIDANIE TLAČIDLA "ULOŽIŤ" DO HORNEJ LIŠTY
                actions = {
                    // Tlačidlo "Uložiť" sa zobrazí, len ak je formulár platný
                    if (isFormValidAndDirty) {
                        Button(
                            onClick = saveContactAndGoBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50), // Zelená farba
                                contentColor = Color.White
                            )
                        ) {
                            Text("Uložiť")
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Malý odstup od okraja
                },
                // ✅ KROK 2: NASTAVENIE SPRÁVNYCH FARIEB
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelekomMagenta,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        // ✅ KROK 3: ODSTRÁNENIE floatingActionButton
        // floatingActionButton = { ... } // Táto celá sekcia je preč
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
            // Formulár zostáva bez zmeny
            ContactForm(viewModel = viewModel)
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Zahodiť zmeny?") },
            text = { Text("Máte neuložené zmeny. Naozaj ich chcete zahodiť a vrátiť sa späť?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        performDiscardAndGoBack()
                    }
                ) {
                    Text("Zahodiť")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Zrušiť")
                }
            }
        )
    }
}


// =========================================================================
// Zvyšok súboru (ContactForm, ChannelDropdownField) je úplne bez zmeny.
// Môžete si ho nechať tak, ako je.
// =========================================================================

@Composable
fun ContactForm(viewModel: ContactsViewModel) {

    Spacer(modifier = Modifier.height(16.dp))

    // Meno
    OutlinedTextField(
        value = viewModel.formFirstName,
        onValueChange = viewModel::updateFirstName,
        label = { Text("Meno") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Priezvisko
    OutlinedTextField(
        value = viewModel.formLastName,
        onValueChange = viewModel::updateLastName,
        label = { Text("Priezvisko") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Funkcia/Pozícia
    OutlinedTextField(
        value = viewModel.formFunction,
        onValueChange = viewModel::updateFunction,
        label = { Text("Funkcia") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Telefónne číslo
    OutlinedTextField(
        value = viewModel.formPhone,
        onValueChange = viewModel::updatePhone,
        label = { Text("Telefónne číslo") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // E-mail
    OutlinedTextField(
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

    val dropdownOptions = remember {
        viewModel.channelOptions.drop(1)
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = viewModel.formChannel,
            onValueChange = {},
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
