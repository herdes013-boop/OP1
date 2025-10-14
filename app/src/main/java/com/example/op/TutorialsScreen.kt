package com.example.op

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(
    navController: NavController,
    viewModel: TutorialsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val tutorials by viewModel.filteredTutorials.collectAsState()
    val categories = viewModel.categories
    val selectedCategory by viewModel::selectedCategory
    val searchQuery by viewModel::searchQuery
    val onSearchQueryChange = viewModel::onSearchQueryChange

    // =========================================================================
    // ✅ ÚPRAVA ŠTRUKTÚRY: Box > Column, presne ako v PasswordsScreen
    // =========================================================================
    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // 1. ZÁLOŽKY - zostávajú na vrchu
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> viewModel.onCategorySelected(category) }
            )

            // 2. SEARCHBAR - presne ako v ostatných obrazovkách
            // Zobrazí sa, len ak je zvolená kategória "Všetky"
            if (selectedCategory == "Všetky") {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = { /* Hľadá sa priebežne */ },
                    active = false, // Vždy neaktívny
                    onActiveChange = {},
                    // ✅ MODIFIER S PADDING A OFFSETOM
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                        .offset(y = (-18).dp),
                    placeholder = { Text("Hľadať v návodoch...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Black) },
                    trailingIcon = {
                        // ✅ ÚPRAVA: Pridaná logika pre zobrazenie ikony filtra
                        if (searchQuery.isNotEmpty()) {
                            // Ak sa vyhľadáva, zobraz krížik na zmazanie
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Vymazať text", tint = Color.Black)
                            }
                        } else {
                            // Ak je vyhľadávanie prázdne, zobraz ikonu filtra
                            IconButton(onClick = { /* TODO: Otvoriť dialóg s filtrami pre návody */ }) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filtrovať zoznam", tint = Color.Black)
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.White,
                        dividerColor = Color.Transparent
                    )
                ) {}
            }

            // 3. ZOZNAM ALEBO SPRÁVA O PRÁZDNOM STAVE
            if (tutorials.isEmpty()) {
                val message = when {
                    selectedCategory == "Všetky" && searchQuery.isNotBlank() ->
                        "Nenašli sa žiadne návody pre '${searchQuery}'."
                    selectedCategory != "Všetky" ->
                        "V kategórii \"$selectedCategory\" zatiaľ nie sú žiadne návody."
                    else -> "Zatiaľ neboli pridané žiadne návody."
                }
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    // ✅ Upravený padding, aby bol hore priestor pre SearchBar
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tutorials, key = { it.id }) { tutorial ->
                        TutorialCard(
                            tutorial = tutorial,
                            onClick = { navController.navigate(Routes.tutorialDetail(tutorial.id)) }
                        )
                    }
                }
            }
        }

        // FloatingActionButton je zarovnaný v hlavnom Boxe
        FloatingActionButton(
            onClick = {
                viewModel.resetForm()
                navController.navigate(Routes.ADD_TUTORIAL)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Pridať návod")
        }
    }
}

// Funkcie CategoryTabs a TutorialCard zostávajú bez zmeny
@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 8.dp
    ) {
        categories.forEach { category ->
            Tab(
                selected = (category == selectedCategory),
                onClick = { onCategorySelected(category) },
                text = { Text(category) }
            )
        }
    }
}

@Composable
fun TutorialCard(
    tutorial: Tutorial,
    onClick: () -> Unit,
) {
    // ✅ KROK 1: Nájdeme prvý obrázok v návode
    val firstImageBlock = tutorial.content.filterIsInstance<TutorialContentBlock.ImageBlock>().firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // ✅ KROK 2: Použijeme Row pre usporiadanie obrázka a textu vedľa seba
        Row(
            modifier = Modifier.padding(12.dp), // Zmenšený padding pre kompaktnejší vzhľad
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- OBRÁZKOVÁ ČASŤ (vľavo) ---
            Box(
                modifier = Modifier
                    .size(80.dp) // Pevná veľkosť pre náhľadový obrázok
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                // ✅ KROK 3: Použijeme knižnicu Coil na zobrazenie obrázka
                AsyncImage(
                    model = firstImageBlock?.uriString ?: firstImageBlock?.imageRes,
                    contentDescription = "Náhľad návodu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.ic_image_not_supported),
                    error = painterResource(id = R.drawable.ic_image_not_supported)
                )
            } // ✅ TÁTO ZÁTVORKA UKONČUJE BOX

            Spacer(Modifier.width(16.dp)) // Teraz je Spacer na správnom mieste

            // --- TEXTOVÁ ČASŤ (vpravo) ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tutorial.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2, // Povolenie 2 riadkov pre dlhšie názvy
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tutorial.category,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                // Pôvodný textový náhľad je odstránený
            }
        }
    }
}
