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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val passwords by viewModel.passwordList.collectAsState()
    val ipAddresses by viewModel.ipList.collectAsState()

    // --- TU JE ZMENA č.1 ---
    // Už nepoužívame `var selectedTabIndex by remember...`
    // Namiesto toho berieme stav priamo z ViewModelu.
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
                        // --- TU JE ZMENA č.2 ---
                        // Pri kliknutí voláme funkciu z ViewModelu.
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
                    // --- TU JE ZMENA č.3 ---
                    // Keď klikneme na +, povieme ViewModelu, aby si to zapamätal.
                    1 -> {
                        viewModel.onTabSelected(1) // Explicitne nastavíme, aby sme boli na IP tabe
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


// Composable funkcie PasswordListItem a IpListItem zostávajú bez zmeny
@Composable
fun PasswordListItem(item: PasswordItem, onClick: () -> Unit) {
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
                item.username?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(item.password)) }
            ) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Kopírovať heslo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

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
