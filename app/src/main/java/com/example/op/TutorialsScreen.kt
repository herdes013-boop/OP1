
package com.example.op

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun TutorialsScreen(
    navController: NavController,
    viewModel: TutorialsViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val tutorials by viewModel.filteredTutorials.collectAsState()
    val categories = viewModel.categories
    val selectedCategory by viewModel::selectedCategory

    // ✅ ZMENA: Celý obsah obrazovky dáme do jedného Columnu.
    Column(modifier = modifier.fillMaxSize()) {

        // 1. ZÁLOŽKY - sú hneď na vrchu, bez paddingu
        CategoryTabs(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { category -> viewModel.onCategorySelected(category) }
        )

        // 2. ZVYŠOK OBSAHU - tento je zabalený do Scaffold-u
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.resetForm()
                        navController.navigate(Routes.ADD_TUTORIAL)
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Pridať návod")
                }
            }
        ) { innerPadding ->
            // Column pre zvyšný obsah, ktorý už POUŽÍVA innerPadding
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {

                Spacer(modifier = Modifier.height(8.dp))

                if (tutorials.isEmpty()) {
                    val message = if (selectedCategory == "Všetky") {
                        "Zatiaľ neboli pridané žiadne návody."
                    } else {
                        "V kategórii \"$selectedCategory\" zatiaľ nie sú žiadne návody."
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), // bottom padding pre lepší vzhľad
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
        }
    }
}

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
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
    onClick: () -> Unit,
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
