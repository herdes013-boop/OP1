package com.example.op


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
import com.google.accompanist.flowlayout.FlowRow

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
    val categories by tutorialsViewModel::tutorialCategories
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
                    // ===================================
                    // ===== KROK 1: TU JE KĽÚČOVÁ ZMENA =====
                    // ===================================
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Možnosti")
                            }
                            // Menu je teraz ukotvené k rodičovskému Boxu
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Upraviť") },
                                    onClick = {
                                        // KROK 1: OKAMŽITE SKRYJEME STARÚ HORNÚ LIŠTU
                                        sharedViewModel.setTopBarState(TopBarState(isVisible = false))

                                        // KROK 2: AŽ POTOM ZATVORÍME MENU A NAVIGUJEME
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp), // ✅ KROK 1: Pridáme odsadenie okolo karty
        contentAlignment = Alignment.Center // Centrovanie pre ProgressIndicator
    ) {
        if (isLoading) {
            CircularProgressIndicator() // Už nepotrebuje align, lebo Box ho centruje
        } else {
            // ✅ KROK 2: Vložíme Card, ktorá obalí celý obsah
            Card(
                modifier = Modifier.fillMaxSize(), // Karta vyplní priestor daný vonkajším Boxom
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // ✅ KROK 3: Pôvodný LazyColumn presunieme dovnútra karty
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sem patrí pôvodný obsah LazyColumn
                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (categories.isNotEmpty()) {Spacer(modifier =Modifier.height(8.dp))
                            FlowRow(
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 4.dp
                            ) {
                                categories.forEach { categoryName ->
                                    SuggestionChip(onClick = { /* Čip nie je klikateľný */ },
                                        label = { Text(categoryName) }
                                    )
                                }
                            }
                        }
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
        }

        // =============================================
        // ===== KROK 2: TENTO BLOK SME ODTIAĽTO ZMAZALI =====
        // =============================================
        // DropdownMenu(...) bol predtým tu, teraz je hore v `actions`
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

// Funkcie TextBlockView a ImageBlockView zostávajú bez zmeny
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
    val imageModel = block.uriString ?: block.imageRes

    if (imageModel != null) {
        AsyncImage(
            model = imageModel,
            contentDescription = "Obrázok v návode",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Obrázok sa nepodarilo načítať",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
