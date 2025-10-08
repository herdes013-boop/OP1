package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
    val passwordSearchText by viewModel.passwordSearchText.collectAsState()
    val ipSearchText by viewModel.ipSearchText.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex
    val tabs = listOf("Heslá", "IP Adresy")

    LaunchedEffect(Unit) {
        viewModel.onScreenAppeared()
        sharedViewModel.setTopBarState(TopBarState(title = "Heslá", isVisible = true))
        sharedViewModel.setShowBottomBar(true)
    }

    // ✅ ZMENA č. 1: Použijeme Scaffold namiesto Box
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTabIndex) {
                        0 -> navController.navigate(Routes.ADD_PASSWORD)
                        1 -> navController.navigate(Routes.ADD_IP_ADDRESS)
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Pridať")
            }
        }
    ) { innerPadding -> // ✅ ZMENA č. 2: Získame automatický padding

        Column(
            // Aplikujeme padding od Scaffold-u
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Zvyšok kódu je identický, len je vložený sem
            when (selectedTabIndex) {
                0 -> SearchBar(
                    query = passwordSearchText,
                    onQueryChange = viewModel::onPasswordSearchTextChange,
                    placeholder = "Vyhľadať v heslách..."
                )
                1 -> SearchBar(
                    query = ipSearchText,
                    onQueryChange = viewModel::onIpSearchTextChange,
                    placeholder = "Vyhľadať v IP adresách..."
                )
            }

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

            when (selectedTabIndex) {
                0 -> {
                    TabContent(
                        searchText = passwordSearchText,
                        data = passwords,
                        emptyListText = "Zatiaľ žiadne heslá.",
                        noResultsText = "Žiadne výsledky pre '${passwordSearchText}'"
                    ) { item ->
                        val password = item as PasswordItem
                        PasswordListItem(
                            item = password,
                            onClick = { navController.navigate("item_detail/${password.id}") }
                        )
                    }
                }
                1 -> {
                    TabContent(
                        searchText = ipSearchText,
                        data = ipAddresses,
                        emptyListText = "Zatiaľ žiadne IP adresy.",
                        noResultsText = "Žiadne výsledky pre '${ipSearchText}'"
                    ) { item ->
                        val ip = item as IpItem
                        IpListItem(
                            item = ip,
                            onClick = { navController.navigate("item_detail/${ip.id}") }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { /* Hľadá sa priebežne */ },
        active = false,
        onActiveChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ikona vyhľadávania") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Vymazať text")
                }
            }
        }
    ) {
        // Prázdny, ale povinný blok
    }
}


@Composable
private fun <T> TabContent(
    searchText: String,
    data: List<T>,
    emptyListText: String,
    noResultsText: String,
    itemContent: @Composable (T) -> Unit,
) {
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(), // Padding už nie je potrebný, rieši ho Scaffold
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (searchText.isBlank()) emptyListText else noResultsText,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            // ✅ ZMENA č. 3: Odstránili sme manuálny `bottom` padding
            contentPadding = PaddingValues(bottom = 0.dp) // Priestor už rieši `innerPadding` od Scaffold-u
        ) {
            items(data) { item ->
                itemContent(item)
            }
        }
    }
}

// Zvyšok kódu (ColoredPasswordText, PasswordListItem, IpListItem) zostáva úplne bez zmeny...
@Composable
private fun ColoredPasswordText(
    password: String,
    modifier: Modifier = Modifier,
    defaultColor: Color = MaterialTheme.colorScheme.onSurface,
    numberColor: Color = MaterialTheme.colorScheme.primary,
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
    Text(
        text = annotatedString,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!item.username.isNullOrBlank()) {
                    Text(
                        text = item.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                ColoredPasswordText(
                    password = item.password
                )
            }
        }
    }
}

@Composable
fun IpListItem(item: IpItem, onClick: () -> Unit) {
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
        }
    }
}
