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
            .padding(16.dp) // Odsadenie karty od okrajov
    ) {
        // Všetky detaily sú teraz vo vnútri tejto karty
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Vnútorný stĺpec pre obsah karty
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp), // Vnútorný padding karty
                verticalArrangement = Arrangement.spacedBy(18.dp) // Medzery medzi položkami
            ) {
                // Položky detailu zostávajú rovnaké, len sú teraz vo vnútri karty
                DetailItem(label = "Názov služby", value = passwordItem.name)
                passwordItem.username?.let {
                    if (it.isNotBlank()) {
                        DetailItem(label = "Používateľské meno / E-mail", value = it)
                    }
                }
                PasswordDetailItem(label = "Heslo", password = passwordItem.password)
                passwordItem.url?.let { urlString ->
                    if (urlString.isNotBlank()) {
                        UrlDetailItem(urlString = urlString)
                    }
                }
                passwordItem.notes?.let {
                    if (it.isNotBlank()) {
                        DetailItem(label = "Poznámky", value = it)
                    }
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
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.SemiBold,
        )

    }
}

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

    }
}

@Composable
private fun UrlDetailItem(urlString: String) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "URL",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
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
        ClickableText(
            text = annotatedUrl,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            onClick = {
                try {
                    uriHandler.openUri(urlString)
                } catch (e: Exception) {
                    println("Chyba pri otváraní URL: ${e.message}")
                }
            }
        )

    }
}
