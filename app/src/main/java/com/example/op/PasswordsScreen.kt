package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


// Hlavná obrazovka zostáva takmer bez zmeny
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier,
) {
    val passwords by viewModel.passwordList.collectAsState()
    val ipAddresses by viewModel.ipList.collectAsState()

    val selectedTabIndex by viewModel.selectedTabIndex
    val tabs = listOf("Heslá", "IP Adresy")

    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(
            TopBarState(
                title = "Heslá",
                isVisible = true,
                actions = null,
                navigationIcon = null
            )
        )
        sharedViewModel.setShowBottomBar(true)
    }

    val currentData: List<Any> = when (selectedTabIndex) {
        0 -> passwords
        1 -> ipAddresses
        else -> emptyList()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currentData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Zatiaľ žiadne záznamy.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> items(passwords, key = { it.id }) { item ->
                            // Používa sa už upravený PasswordListItem
                            PasswordListItem(
                                item = item,
                                onClick = { navController.navigate(Routes.passwordDetail(item.id)) }
                            )
                        }
                        1 -> items(ipAddresses, key = { it.id }) { item ->
                            IpListItem(
                                item = item,
                                onClick = { navController.navigate(Routes.editIpAddress(item.id)) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                when (selectedTabIndex) {
                    0 -> navController.navigate(Routes.ADD_PASSWORD)
                    1 -> {
                        viewModel.onTabSelected(1)
                        navController.navigate(Routes.ADD_IP_ADDRESS)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Pridať")
        }
    }
}

// ========================================================================
// ===                   ZAČIATOK ZMIEN V `PasswordListItem`              ===
// ========================================================================

/**
 * Pomocná funkcia, ktorá zobrazí text hesla a zafarbí číslice.
 */
@Composable
private fun ColoredPasswordText(
    password: String,
    modifier: Modifier = Modifier,
    defaultColor: Color = MaterialTheme.colorScheme.onSurface,
    numberColor: Color = MaterialTheme.colorScheme.primary, // Farba pre číslice
) {
    val annotatedString = buildAnnotatedString {
        password.forEach { char ->
            if (char.isDigit()) {
                // Ak je znak číslica, pridá ho s farbou pre čísla a tučným štýlom
                withStyle(style = SpanStyle(color = numberColor, fontWeight = FontWeight.Bold)) {
                    append(char)
                }
            } else {
                // Inak ho pridá s predvolenou farbou
                withStyle(style = SpanStyle(color = defaultColor)) {
                    append(char)
                }
            }
        }
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Kompletne prepracovaný Composable pre položku v zozname hesiel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListItem(item: PasswordItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp) // Medzera medzi riadkami
        ) {
            // --- HORNÝ RIADOK: Názov služby ---
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // --- SPODNÝ RIADOK: Používateľské meno a Heslo ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Používateľské meno (ak existuje)
                item.username?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            // weight(1f) spôsobí, že meno zaberie voľné miesto
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // Heslo s farebnými číslami
                ColoredPasswordText(
                    password = item.password,
                    // Ak meno neexistuje, heslo zaberie celý spodný riadok
                    modifier = if (item.username.isNullOrBlank()) Modifier.weight(1f) else Modifier
                )
            }
        }
    }
}

// ========================================================================
// ===                    KONIEC ZMIEN V `PasswordListItem`               ===
// ========================================================================


// IpListItem zostáva bez zmeny
@Composable
fun IpListItem(item: IpItem, onClick: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.ipAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(item.ipAddress)) }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Kopírovať IP adresu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

