package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordDetailScreen(
    passwordId: String,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val passwordItem by viewModel.selectedPassword.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // ===== KĽÚČOVÁ ZMENA JE TU =====

    // Tento `LaunchedEffect` sa spustí iba raz, hneď pri vstupe na obrazovku.
    LaunchedEffect(Unit) {
        // 1. OKAMŽITE nastavíme správnu štruktúru hornej lišty.
        //    Tým "prebijeme" starú lištu z `PasswordsScreen`.
        //    Titulok bude dočasne prázdny.
        sharedViewModel.setTopBarState(
            TopBarState(
                title = " ", // Dôležitá medzera, aby sa titulok nezrútil
                isVisible = true,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Viac")
                    }
                }
            )
        )

        // 2. Až TERAZ povieme ViewModelu, aby začal načítavať dáta.
        viewModel.loadPasswordDetail(passwordId)
    }

    // Tento druhý `LaunchedEffect` už len čaká na dáta a doplní titulok.
    LaunchedEffect(passwordItem) {
        if (passwordItem != null) {
            // Použijeme našu novú funkciu, ktorá zmení IBA titulok.
            sharedViewModel.updateTopBarTitle(passwordItem!!.name)
        }
    }

    // Tento `DisposableEffect` "uprace" po odchode z obrazovky.
    DisposableEffect(Unit) {
        onDispose {
            // Vyčistíme ViewModel, aby si nepamätal posledné heslo.
            viewModel.clearSelectedPassword()
        }
    }
    // ===== KONIEC ZMIEN =====

    // Ak sa heslo nenašlo alebo sa ešte nenačítalo, zobrazíme prázdnu schránku.
    // Horná lišta je už ale nastavená správne vďaka prvému LaunchedEffect.
    if (passwordItem == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // --- Zvyšok UI zostáva bez akejkoľvek zmeny ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailItem(label = "Názov služby", value = passwordItem!!.name)
            passwordItem!!.username?.let {
                DetailItem(label = "Používateľské meno / E-mail", value = it, canBeCopied = true)
            }
            DetailItem(label = "Heslo", value = "••••••••", displayValue = passwordItem!!.password, canBeCopied = true)
            passwordItem!!.notes?.let {
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
            text = { Text("Naozaj chcete natrvalo zmazať toto heslo?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePassword(passwordId)
                        showDeleteDialog = false
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

// Funkcia DetailItem zostáva bez zmeny
@Composable
private fun DetailItem(
    label: String,
    value: String,
    displayValue: String = value,
    canBeCopied: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (canBeCopied) {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(displayValue)) }) {
                    Icon(Icons.Default.ContentCopy, "Kopírovať")
                }
            }
        }
        Divider()
    }
}

