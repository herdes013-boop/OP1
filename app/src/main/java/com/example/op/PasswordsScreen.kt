package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordsScreen(
    navController: NavController,
    viewModel: PasswordsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier,
) {
    // Stavy z ViewModelu zostávajú rovnaké
    val passwords by viewModel.passwordList.collectAsState()
    val ipAddresses by viewModel.ipList.collectAsState()
    val passwordSearchText by viewModel.passwordSearchText.collectAsState()
    val ipSearchText by viewModel.ipSearchText.collectAsState()
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

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ✅ ZMENA 1: Zobrazíme správne vyhľadávacie pole podľa vybranej záložky
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

            // Záložky sú teraz AŽ POD vyhľadávacím poľom
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            // Obsah záložiek sa už stará len o zobrazenie zoznamu
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
                            onClick = { navController.navigate(Routes.passwordDetail(password.id)) }
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
                            onClick = { navController.navigate(Routes.editIpAddress(ip.id)) }
                        )
                    }
                }
            }
        }

        // Floating Action Button zostáva na svojom mieste
        FloatingActionButton(
            onClick = {
                when (selectedTabIndex) {
                    0 -> navController.navigate(Routes.ADD_PASSWORD)
                    1 -> navController.navigate(Routes.ADD_IP_ADDRESS)
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

/**
 * ✅ ZMENA 2: Vytvorili sme samostatnú komponentu pre SearchBar, aby sme sa neopakovali.
 * Je to čistejšie.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { /* Hľadá sa priebežne */ },
        active = false,
        onActiveChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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


/**
 * ✅ ZMENA 3: Z komponenty TabContent sme ODSTRÁNILI SearchBar.
 * Teraz sa stará iba o zobrazenie zoznamu alebo prázdneho stavu.
 */
@Composable
private fun <T> TabContent(
    searchText: String,
    data: List<T>,
    emptyListText: String,
    noResultsText: String,
    itemContent: @Composable (T) -> Unit
) {
    // Spacer(modifier = Modifier.height(8.dp)) // Už nie je potrebný

    // Zobrazenie výsledkov alebo správy
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), // padding aby text nebol za FAB
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
            contentPadding = PaddingValues(bottom = 80.dp) // Priestor pre FAB
        ) {
            items(data) { item -> // Používame priamo item, je to bezpečnejšie
                itemContent(item)
            }
        }
    }
}


// Ostatné pomocné Composable funkcie (PasswordListItem, ColoredPasswordText, IpListItem) zostávajú bez zmeny
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
                item.username?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                ColoredPasswordText(
                    password = item.password,
                    modifier = if (item.username.isNullOrBlank()) Modifier.weight(1f) else Modifier
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

