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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver



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

    // TOTO VLOŽTE NAMIESTO PÔVODNÉHO LaunchedEffect
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Toto sa spustí VŽDY, keď sa obrazovka stane viditeľnou
            if (event == Lifecycle.Event.ON_START) {
                // Nastavíme správny stav hornej a dolnej lišty pre túto obrazovku
                sharedViewModel.setTopBarState(TopBarState(title = "Heslá", isVisible = true))
                sharedViewModel.setShowBottomBar(true)
            }
        }



        // Pripojíme nášho "pozorovateľa"
        lifecycleOwner.lifecycle.addObserver(observer)

        // Odpojíme ho, keď obrazovka zmizne, aby sme predišli únikom pamäte
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // ==================================================================
    //                         KĽÚČOVÁ ZMENA
    // Použijeme Box namiesto Column + Scaffold, aby sme mohli umiestniť FAB
    // ==================================================================
    Box(
        // Tento modifier bol predtým na hlavnom Columne
        modifier = modifier.fillMaxSize()
    ) {
        // Vnútorný Column pre obsah (záložky, search, zoznam)
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. ZÁLOŽKY - zostávajú ako boli
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }

            // 2. SEARCHBAR - presunutý sem, už nie je vnútri Scaffoldu
            when (selectedTabIndex) {
                0 -> androidx.compose.material3.SearchBar(
                    query = passwordSearchText,
                    onQueryChange = viewModel::onPasswordSearchTextChange,
                    onSearch = { },
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                        .offset(y = (-18).dp),

                    placeholder = { Text("Vyhľadať v heslách...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
                    trailingIcon = {
                        if (passwordSearchText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onPasswordSearchTextChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Black)
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.White,
                        dividerColor = Color.Transparent
                    )
                ) { }

                1 -> androidx.compose.material3.SearchBar(
                    query = ipSearchText,
                    onQueryChange = viewModel::onIpSearchTextChange,
                    onSearch = { },
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                        .offset(y = (-18).dp),
                    placeholder = { Text("Vyhľadať v IP adresách...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
                    trailingIcon = {
                        if (ipSearchText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onIpSearchTextChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Black)
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.White,
                        dividerColor = Color.Transparent
                    )
                ) { }
            }

            // 3. OBSAH KARIET (ZOZNAM)
            when (selectedTabIndex) {
                0 -> {
                    TabContent(
                        // Modifier.weight(1f) už nie je potrebný, lebo LazyColumn je v Columne
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
                            onClick = { navController.navigate("ip_detail/${ip.id}") }
                        )
                    }
                }
            }
        }

        // FloatingActionButton je teraz v Boxe a zarovnaný doprava dole
        FloatingActionButton(
            onClick = {
                when (selectedTabIndex) {
                    0 -> navController.navigate(Routes.ADD_PASSWORD)
                    1 -> navController.navigate(Routes.ADD_IP_ADDRESS)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Štandardný padding pre FAB
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Pridať")
        }
    }
}





@Composable
private fun <T> TabContent(
    modifier: Modifier = Modifier,
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
            // Zmena: Použijeme vertikálny padding priamo tu, aby bol priestor nad prvou položkou
            contentPadding = PaddingValues(top = 8.dp, bottom = 0.dp)
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
        // ✅ KROK 1: Hlavný kontajner zmeníme z Column na Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically // Zarovnáme všetko na stred vertikálne
        ) {
            // --- ČASŤ VĽAVO (NÁZOV) ---
            // ✅ KROK 2: Názov zaberie všetok voľný priestor
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2, // Povolené 2 riadky pre dlhšie názvy
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f) // Kľúčová zmena
            )

            Spacer(modifier = Modifier.width(16.dp)) // Medzera medzi názvom a prihlasovacími údajmi

            // --- ČASŤ VPRAVO (USER + HESLO) ---
            // ✅ KROK 3: Zoskupíme username a password do stĺpca
            Column(
                horizontalAlignment = Alignment.End // Zarovnáme obsah tohto stĺpca doprava
            ) {
                // Používateľské meno (ak existuje)
                if (!item.username.isNullOrBlank()) {
                    Text(
                        text = item.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Heslo (vždy viditeľné)
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

