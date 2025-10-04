package com.example.op

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun TutorialDetailScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    tutorialId: String?,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tutorialId == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(tutorialId) {
        tutorialsViewModel.loadTutorialForEditing(tutorialId)
    }

    val isLoading by tutorialsViewModel.isLoading.collectAsState()
    val title by tutorialsViewModel::tutorialTitle
    val category by tutorialsViewModel::tutorialCategory
    val contentBlocks by tutorialsViewModel.contentBlocks.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading, title) {
        if (!isLoading && title.isNotBlank()) {
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = title,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Naspäť")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Možnosti")
                        }
                    }
                )
            )
        } else if (isLoading) {
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = "Načítava sa...",
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Naspäť")
                        }
                    },
                    actions = {}
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                items(contentBlocks, key = { it.id }) { block ->
                    when (block) {
                        is TutorialContentBlock.TextBlock -> TextBlockView(block = block)
                        is TutorialContentBlock.ImageBlock -> ImageBlockView(block = block)
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            DropdownMenuItem(
                text = { Text("Upraviť") },
                onClick = {
                    showMenu = false
                    onNavigateToEdit(tutorialId)
                },
                leadingIcon = { Icon(Icons.Default.Edit, null) }
            )
            DropdownMenuItem(
                text = { Text("Zmazať", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                tutorialsViewModel.deleteTutorial(tutorialId)
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun TextBlockView(block: TutorialContentBlock.TextBlock) {
    Text(
        text = block.text,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 24.sp
    )
}

@Composable
fun ImageBlockView(block: TutorialContentBlock.ImageBlock) {
    val imageRes = block.imageRes

    if (imageRes != null && imageRes != 0) {
        // Vytvoríme si chybový placeholder, ktorý sa zobrazí, ak sa obrázok nepodarí načítať
        val errorPlaceholder = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chyba: Obrázok sa nepodarilo načítať.",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // AsyncImage je špeciálna Composable funkcia z knižnice Coil,
        // ktorá je navrhnutá na bezpečné načítavanie obrázkov.
        AsyncImage(
            model = imageRes, // Modelom je naše ID zdroja
            contentDescription = "Obrázok v návode",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            // error parameter sa automaticky použije, ak nastane chyba
            error = painterResource(id = R.drawable.ic_launcher_background), // Tu môžeme dať aj iný placeholder
            // alebo ešte lepšie, náš vlastný error composable
            // error = errorPlaceholder
        )
    }
}
