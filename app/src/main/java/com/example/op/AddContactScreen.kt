// Súbor: AddContactScreen.kt

package com.example.op

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    // ✅ Prijímame modifier a onBack, presne ako pri editácii
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ContactsViewModel = viewModel(),
    sharedViewModel: SharedViewModel,
    onBack: () -> Unit
) {
    // ✅ Používame lokálny stav, nie ViewModel
    var localContact by remember {
        mutableStateOf(ContactItem(id = 0, firstName = "", lastName = ""))
    }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // ✅ Zisťujeme zmeny porovnaním s prázdnym objektom
    val hasUnsavedChanges by remember(localContact) {
        derivedStateOf {
            localContact.firstName.isNotBlank() ||
                    localContact.lastName.isNotBlank() ||
                    localContact.function?.isNotBlank() == true ||
                    localContact.phone?.isNotBlank() == true ||
                    localContact.email?.isNotBlank() == true ||
                    localContact.notes?.isNotBlank() == true
        }
    }

    // ✅ Funkcie na uloženie a návrat
    fun saveContactAndGoBack() {
        viewModel.addContact(localContact) // Uložíme celý lokálny objekt naraz
        onBack()
    }

    fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onBack()
        }
    }

    // ✅ Správa horného panela cez SharedViewModel
    LaunchedEffect(hasUnsavedChanges) {
        sharedViewModel.setTopBarState(TopBarState(
            title = "Nový kontakt",
            navigationIcon = {
                IconButton(onClick = ::handleBackNavigation) {
                    Icon(Icons.Default.ArrowBack, "Naspäť")
                }
            },
            actions = {
                if (hasUnsavedChanges) {
                    Button(
                        onClick = ::saveContactAndGoBack,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("ULOŽIŤ")
                    }
                }
            }
        ))
    }

    // ✅ Spracovanie systémového tlačidla "späť"
    BackHandler(onBack = ::handleBackNavigation)

    // Formulár už nie je v Scaffolde, ale v Boxe, ktorý dostane padding
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ Formulár je prepojený na `localContact`, nie na ViewModel
            OutlinedTextField(
                value = localContact.firstName,
                onValueChange = { localContact = localContact.copy(firstName = it) },
                label = { Text("Meno") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.lastName,
                onValueChange = { localContact = localContact.copy(lastName = it) },
                label = { Text("Priezvisko") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.phone ?: "",
                onValueChange = { localContact = localContact.copy(phone = it) },
                label = { Text("Telefónne číslo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.email ?: "",
                onValueChange = { localContact = localContact.copy(email = it) },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.function ?: "",
                onValueChange = { localContact = localContact.copy(function = it) },
                label = { Text("Funkcia/Pozícia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localContact.notes ?: "",
                onValueChange = { localContact = localContact.copy(notes = it) },
                label = { Text("Poznámky") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            val selectedChannelValue: String = localContact.channel ?: viewModel.channelOptions.first()
            ChannelDropdown(
                selectedChannel = selectedChannelValue,
                onChannelSelected = { localContact = localContact.copy(channel = it) },
                channelOptions = viewModel.channelOptions
            )
        }
    }

    // ✅ Používame náš univerzálny dialóg
    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = {
                saveContactAndGoBack()
                showUnsavedChangesDialog = false
            },
            onDiscard = {
                onBack()
                showUnsavedChangesDialog = false
            },
            onCancel = {
                showUnsavedChangesDialog = false
            }
        )
    }
}
