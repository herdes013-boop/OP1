package com.example.op

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController

// ======================= NOVÁ POMOCNÁ FUNKCIA =======================
@Composable
fun LifecycleEffect(event: Lifecycle.Event, onEvent: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, event) {
        val observer = LifecycleEventObserver { _, e ->
            if (e == event) {
                onEvent()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
// ====================================================================

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
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // Toto načítanie je v poriadku, spúšťa sa len raz pre dané ID
    LaunchedEffect(tutorialId) {
        tutorialsViewModel.loadTutorialForEditing(tutorialId)
    }

    val title by tutorialsViewModel::tutorialTitle
    val category by tutorialsViewModel::tutorialCategory
    val contentBlocks by tutorialsViewModel.contentBlocks.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ======================= KĽÚČOVÁ ZMENA TU =======================
    // Tento blok sa teraz spustí VŽDY, keď sa obrazovka vráti do popredia (ON_RESUME),
    // teda aj pri návrate z obrazovky úprav.
    LifecycleEffect(event = Lifecycle.Event.ON_RESUME) {
        if (title.isNotBlank()) {
            sharedViewModel.setTopBarState(
                TopBarState(
                    title = title,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Naspäť")
                        }
                    },
                    actions = {
                        // Tlačidlá presunuté sem, aby mali vždy čerstvý stav `showMenu`
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Možnosti")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
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
                )
            )
        }
    }
    // ===================== KONIEC KĽÚČOVEJ ZMENY =====================

    if (showDeleteDialog) {
        // Použijeme dialóg, ktorý už máme definovaný v AddTutorialScreen.kt
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                tutorialsViewModel.deleteTutorial(tutorialId)
                navController.popBackStack()
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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

// Ostatné funkcie pre zobrazenie zostávajú nezmenené
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
    if (imageRes != null) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Obrázok v návode",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}
