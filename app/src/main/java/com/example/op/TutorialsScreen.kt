package com.example.op

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import com.google.accompanist.flowlayout.FlowRow



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


    // NOVÉ: Získame stavy pre filter dialóg
    val isFilterDialogVisible by viewModel::isFilterDialogVisible
    val activeFilters by viewModel.activeCategoryFilters.collectAsState()
    val allCategoriesForFilter = viewModel.allCategoriesForFilter

    // NOVÉ: Zobrazenie dialógu
    if (isFilterDialogVisible) {
        FilterTutorialsDialog(
            allCategories = allCategoriesForFilter,
            activeFilters = activeFilters,
            onDismiss = viewModel::onFilterDialogDismiss,
            onCategorySelected = viewModel::onFilterCategorySelected,
            onClearFilters = viewModel::clearAllFilters
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            // =================================================================
            // ✅ KROK 1: POUŽITIE ROVNAKÉHO SEARCHBAR-u AKO V KONTAKTOCH
            // =================================================================
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = { /* Hľadá sa priebežne */ },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    // Použijeme odsadenie zhora a posunutie nadol, aby sa SearchBar
                    // vizuálne prekryl s hornou časťou obrazovky a vytvoril tak
                    // rovnakú medzeru ako v ContactsScreen.
                    .padding(top = 4.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
                    .offset(y = (-18).dp),
                placeholder = { Text("Hľadať v návodoch...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Vymazať text")
                        }
                    } else {
                        // UPRAVENÁ LOGIKA PRE IKONU FILTRA
                        IconButton(onClick = viewModel::onFilterDialogOpen) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (activeFilters.isEmpty()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant // Alebo iná default farba
                                } else {
                                    MaterialTheme.colorScheme.primary // Zvýraznená farba
                                }
                            )
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    // Biela farba a žiadna deliaca čiara, presne ako v Kontaktoch
                    containerColor = Color.White,
                    dividerColor = Color.Transparent
                )
            )
            {}

            ActiveFiltersRow(
                activeFilters = activeFilters,
                onRemoveFilter = viewModel::removeFilterCategory,
                modifier = Modifier
                    .fillMaxWidth()
                    // Použijeme podobné odsadenie, aby to bolo zarovnané
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp)
                    .offset(y = (-18).dp)
            )

            // =================================================================
            // ✅ KROK 2: ZOBRAZENIE ZOZNAMU ALEBO SPRÁVY
            // =================================================================
            if (tutorials.isEmpty()) {
                val message = if (searchQuery.isNotBlank()) {
                    "Nenašli sa žiadne návody pre '${searchQuery}'."
                } else {
                    "Zatiaľ neboli pridané žiadne návody."
                }
                Box(
                    // Box sa už nerozťahuje na celú obrazovku, aby bol text centrovaný
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
                    // Vrchný padding je upravený, aby bol zoznam správne pod SearchBar-om
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

        // FloatingActionButton zostáva na svojom mieste
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

// Funkcia TutorialCard zostáva bez zmeny
@Composable
fun TutorialCard(
    tutorial: Tutorial,
    onClick: () -> Unit,
) {
    val firstImageBlock = tutorial.content.filterIsInstance<TutorialContentBlock.ImageBlock>().firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = firstImageBlock?.uriString ?: firstImageBlock?.imageRes,
                    contentDescription = "Náhľad návodu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    // ✅ Použijeme rememberVectorPainter na konverziu ikony na Painter
                    placeholder = rememberVectorPainter(image = Icons.Filled.Article),
                    error = rememberVectorPainter(image = Icons.Filled.Article)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tutorial.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // ZMENA: Zobrazíme všetky kategórie pomocou FlowRow a SuggestionChip
                if (tutorial.categories.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        mainAxisSpacing = 6.dp,        crossAxisSpacing = 2.dp
                    ) {
                        tutorial.categories.forEach { categoryName ->
                            // Použijeme SuggestionChip pre vizuálne odlíšenie kategórií
                            SuggestionChip(
                                onClick = { /* Čipy na karte nie sú klikateľné */ },
                                label = { Text(categoryName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

            }
        }
    }
}
// PRIDAJTE TENTO KÓD NA KONIEC SÚBORU TutorialsScreen.kt
@Composable
private fun FilterTutorialsDialog(
    allCategories: List<String>,
    activeFilters: Set<String>,
    onDismiss: () -> Unit,
    onCategorySelected: (String, Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrovať podľa kategórie") },
        text = {
            LazyColumn {
                items(allCategories) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category, category !in activeFilters) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = category in activeFilters,
                            onCheckedChange = { isChecked -> onCategorySelected(category, isChecked) }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(text = category)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zavrieť") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClearFilters()
                    onDismiss()
                },
                enabled = activeFilters.isNotEmpty()
            ) {
                Text("Zrušiť filtre")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveFiltersRow(
    activeFilters: Set<String>,
    onRemoveFilter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Zobrazíme tento riadok, iba ak je aspoň jeden filter aktívny.
    if (activeFilters.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                "Aktívne filtre:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 4.dp
            ) {
                activeFilters.forEach { filter ->
                    InputChip(
                        selected = true, // Vždy je "vybraný", keďže je v zozname aktívnych
                        onClick = { onRemoveFilter(filter) }, // Kliknutie na celý čip ho odstráni
                        label = { Text(filter) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Odstrániť filter $filter",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
