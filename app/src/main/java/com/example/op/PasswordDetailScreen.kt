package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordDetailScreen(
    modifier: Modifier = Modifier,
    passwordId: String,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val passwordItem = remember(passwordId) {
        viewModel.getPasswordById(passwordId)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(passwordItem) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = passwordItem?.name ?: "Detail",
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (passwordItem != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Viac")
                        }
                    }
                }
            )
        )
    }

    if (passwordItem == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chyba: Heslo sa nenašlo.")
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp), // Zmazané fixné odsadenie zhora
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === ZMENA č. 1: Odstránenie `canBeCopied` a zmena zobrazenia hesla ===
            DetailItem(label = "Názov služby", value = passwordItem.name)
            passwordItem.username?.let {
                // Ikona kopírovania odstránená
                DetailItem(label = "Používateľské meno / E-mail", value = it)
            }
            // Zobrazenie hesla ako farebného textu, ikona kopírovania odstránená
            PasswordDetailItem(label = "Heslo", password = passwordItem.password)
            passwordItem.notes?.let {
                if (it.isNotBlank()) {
                    DetailItem(label = "Poznámky", value = it)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
        ) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Upraviť") },
                    onClick = {
                        showMenu = false
                        onNavigateToEdit(passwordId)
                    },
                    leadingIcon = { Icon(Icons.Default.Edit, "Upraviť") }
                )
                DropdownMenuItem(
                    text = { Text("Zmazať") },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, "Zmazať", tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Zmazať heslo") },
            text = { Text("Naozaj chcete natrvalo zmazať heslo '${passwordItem.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePassword(passwordId)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Zmazať") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Zrušiť") }
            }
        )
    }
}

/**
 * ZMENA č. 2: Upravená funkcia `DetailItem`, ktorá už nepotrebuje parametre
 * `displayValue` a `canBeCopied`. Je teraz oveľa jednoduchšia.
 */
@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        // Riadok bol zjednodušený, už neobsahuje logiku pre ikonu.
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.SemiBold,
        )
        Divider()
    }
}

/**
 * ZMENA č. 3: NOVÁ pomocná funkcia špeciálne pre zobrazenie hesla s farebnými číslami.
 * Využíva rovnaký princíp ako `ColoredPasswordText` zo zoznamu.
 */
@Composable
private fun PasswordDetailItem(
    label: String,
    password: String,
    defaultColor: Color = LocalContentColor.current,
    numberColor: Color = MaterialTheme.colorScheme.primary
) {
    val annotatedString = buildAnnotatedString {
        password.forEach { char ->
            if (char.isDigit()) {
                withStyle(style = SpanStyle(color = numberColor, fontWeight = FontWeight.Bold)) {
                    append(char)
                }
            } else {
                withStyle(style = SpanStyle(color = defaultColor)) {
                    append(char)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.SemiBold
        )
        Divider()
    }
}
