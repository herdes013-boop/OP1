// súbor: app/src/main/java/com/example/op/TutorialsScreen.kt
package com.example.op

import androidx.compose.foundation.clickable // Uistite sa, že tento import je prítomný
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme // Pridaný import pre lepší prístup k štýlom
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(
    navController: NavController,
    viewModel: TutorialsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val tutorials by viewModel.displayedTutorials.collectAsState(initial = emptyList())
    val categories = viewModel.categories
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Pred prechodom na pridávanie vyčistíme formulár
                    viewModel.resetForm()
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
                selectedCategory = selectedTab,
                onCategorySelected = { category -> viewModel.selectCategory(category) }
            )

            // ====================== ZMENENÁ ČASŤ ======================
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tutorials, key = { it.id }) { tutorial ->
                    // Poskytneme funkciu onClick pre každú kartu
                    TutorialCard(
                        tutorial = tutorial,
                        onClick = {
                            // Navigujeme na obrazovku úpravy s ID konkrétneho návodu
                            navController.navigate(Routes.editTutorial(tutorial.id))
                        }
                    )
                }
            }
            // ==================== KONIEC ZMENENEJ ČASTI ====================
        }
    }
}

@Composable
fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val selectedIndex = categories.indexOf(selectedCategory)

    ScrollableTabRow(
        selectedTabIndex = if (selectedIndex == -1) 0 else selectedIndex
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

// ====================== ZMENENÁ FUNKCIA ======================
@OptIn(ExperimentalMaterial3Api::class) // Pridaná anotácia
@Composable
fun TutorialCard(
    tutorial: TutorialItem,
    onClick: () -> Unit // Pridaný parameter onClick
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Aplikovaný clickable modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = tutorial.title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
// ==================== KONIEC ZMENENEJ FUNKCIE ====================
