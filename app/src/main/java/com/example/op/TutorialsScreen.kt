// súbor: app/src/main/java/com/example/op/TutorialsScreen.kt
package com.example.op

// Všetky potrebné importy...
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
    // displayedTutorials je typu Flow<List<TutorialItem>>
    val tutorials by viewModel.displayedTutorials.collectAsState(initial = emptyList())
    val categories = viewModel.categories
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        modifier = modifier,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedTab,
                // Použijeme správny názov metódy z ViewModelu
                // Použijeme správny názov metódy z ViewModelu
                onCategorySelected = { category -> viewModel.selectCategory(category) }

            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 'tutorial' tu bude automaticky typu TutorialItem
                items(tutorials) { tutorial ->
                    TutorialCard(tutorial = tutorial)
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

// === OPRAVENÁ FUNKCIA ===
@Composable
fun TutorialCard(tutorial: TutorialItem) { // Používame typ TutorialItem, ktorý ViewModel poskytuje
    Card(
        modifier = Modifier.fillMaxWidth() // fillMaxSize by spôsobilo, že jedna karta zaberie celú obrazovku
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = tutorial.title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            // Môžete pridať ďalšie detaily, ak potrebujete
        }
    }
}
