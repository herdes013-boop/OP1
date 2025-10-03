// súbor: app/src/main/java/com/example/op/TutorialsScreen.ktpackage com.example.op

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.op.Routes
import com.example.op.TutorialItem
import com.example.op.TutorialsViewModel


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

            // ====================== KĽÚČOVÁ ZMENA TU ======================
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tutorials, key = { it.id }) { tutorial ->
                    TutorialCard(
                        tutorial = tutorial,
                        onClick = {
                            // NAVIGUJEME NA OBRAZOVKU DETAILU, NIE ÚPRAVY!
                            navController.navigate(Routes.tutorialDetail(tutorial.id))
                        }
                    )
                }
            }
            // ==================== KONIEC ZMENY ====================
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialCard(
    tutorial: TutorialItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = tutorial.title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
