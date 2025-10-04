
package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun TutorialsScreen(
    navController: NavController,
    viewModel: TutorialsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    // ======================= OPRAVA TU =======================
    val tutorials by viewModel.filteredTutorials.collectAsState()
    val categories = viewModel.categories
    val selectedCategory by viewModel::selectedCategory // Zmena 'selectedTab' na 'selectedCategory'
    // =========================================================

    Scaffold(
        modifier = modifier.fillMaxSize(), // Pridané pre istotu
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.resetForm() // Resetujeme stav pred prechodom
                    navController.navigate(Routes.ADD_TUTORIAL)
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Pridať návod")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                // ======================= OPRAVA TU =======================
                onCategorySelected = { category -> viewModel.onCategorySelected(category) } // Zmena 'selectCategory' na 'onCategorySelected'
                // =========================================================
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Zmenšená medzera
            ) {
                items(tutorials, key = { it.id }) { tutorial ->
                    TutorialCard(
                        tutorial = tutorial,
                        onClick = {
                            // Navigujeme na obrazovku detailu
                            navController.navigate(Routes.tutorialDetail(tutorial.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 8.dp // Pridané malé odsadenie
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
    tutorial: Tutorial, // Zmenené z TutorialItem na Tutorial, aby sedel dátový typ
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = tutorial.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = tutorial.category,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
            // Pridaný náhľad textu pre lepší kontext
            val firstText = tutorial.content.filterIsInstance<TutorialContentBlock.TextBlock>().firstOrNull()?.text
            if (firstText != null) {
                Text(
                    text = firstText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
