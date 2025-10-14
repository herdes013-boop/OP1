package com.example.op

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    modifier: Modifier = Modifier,
    passwordId: String,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    onNavigateToEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val passwords by viewModel.passwordList.collectAsState()

    val passwordItem = remember(passwordId, passwords) {
        passwords.find { it.id == passwordId }
    }
    val uriHandler = LocalUriHandler.current

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

    // Vonkajší Box preberá odsadenie od Scaffold
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Použijeme našu novú, centrálnu DetailCard
        DetailCard {
            // Medzery medzi položkami definujeme priamo tu pomocou Spacer
            DetailItem(label = "Názov služby", value = passwordItem.name, isValueSelectable = true)
            passwordItem.username?.let {
                if (it.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    DetailItem(label = "Používateľské meno / E-mail", value = it, isValueSelectable = true)
                }
            }

            Spacer(Modifier.height(16.dp))
            PasswordDetailItem(label = "Heslo", password = passwordItem.password)

            passwordItem.url?.let { urlString ->
                if (urlString.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    UrlDetailItem(urlString = urlString)
                }
            }
            passwordItem.notes?.let {
                if (it.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    DetailItem(label = "Poznámky", value = it, isValueSelectable = true)
                }
            }
        }
    }

// Dropdown menu zostáva vonku, aby sa správne zobrazilo nad všetkým
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, end = 4.dp) // Odsadenie pre "MoreVert" ikonu
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

    // Dialóg na potvrdenie zmazania
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

// --- POMOCNÉ FUNKCIE ---

@Composable
private fun DetailItem(
    label: String,
    value: String,
    isValueSelectable: Boolean = false // Parameter na povolenie kopírovania
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(), // Zjednotený vzhľad
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))

        if (isValueSelectable) {
            SelectionContainer {
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun PasswordDetailItem(label: String, password: String) {
    val annotatedString = buildAnnotatedString {
        password.forEach { char ->
            if (char.isDigit()) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                    append(char)
                }
            } else {
                withStyle(style = SpanStyle(color = LocalContentColor.current)) {
                    append(char)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))

        // Heslo je vždy kopírovateľné
        SelectionContainer {
            Text(text = annotatedString, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun UrlDetailItem(urlString: String) {
    val uriHandler = LocalUriHandler.current
    val annotatedUrl = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(urlString)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "URL",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(2.dp))
        ClickableText(
            text = annotatedUrl,
            style = MaterialTheme.typography.bodyLarge,
            onClick = {
                try {
                    uriHandler.openUri(urlString)
                } catch (_: Exception) {
                    // Chybu môžeme ignorovať alebo zalogovať
                }
            }
        )
    }
}
