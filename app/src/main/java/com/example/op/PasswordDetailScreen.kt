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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier) // Spojí fillMaxSize s odsadením zhora
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Rolovanie
                .padding(horizontal = 16.dp, vertical = 8.dp), // Vlastný padding pre obsah
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            // Názov služby
            DetailItem(label = "Názov služby", value = passwordItem.name)

            // Používateľské meno
            passwordItem.username?.let {
                if (it.isNotBlank()) {
                    DetailItem(label = "Používateľské meno / E-mail", value = it)
                }
            }

            // Heslo
            PasswordDetailItem(label = "Heslo", password = passwordItem.password)

            // URL
            passwordItem.url?.let { urlString ->
                if (urlString.isNotBlank()) {
                    UrlDetailItem(urlString = urlString)
                }
            }

            // Poznámky
            passwordItem.notes?.let {
                if (it.isNotBlank()) {
                    DetailItem(label = "Poznámky", value = it)
                }
            }
        }

        // Dropdown menu
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
                    text = { Text("Zmazať", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        showDeleteDialog = true
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, "Zmazať", tint = MaterialTheme.colorScheme.error) }
                )
            }
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
        Divider()
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
        Divider()
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
        Divider()
    }
}
