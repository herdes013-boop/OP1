package com.example.op


import android.net.Uri // ✅ Potrebný import
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ✅ Potrebný import
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource // ✅ Potrebný import
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri // ✅ Potrebný import
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
    modifier: Modifier = Modifier,
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

    // ==========================================================
    // ===== KROK 1: Pridanie stavu pre zväčšený obrázok =====
    // ==========================================================
    var enlargedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Ak je URI nastavené, zobrazíme dialóg
    if (enlargedImageUri != null) {
        ImageDialog(
            imageModel = enlargedImageUri,
            onDismiss = { enlargedImageUri = null } // Pri zatvorení URI vynulujeme
        )
    }

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
                        Box {
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
                                        sharedViewModel.setTopBarState(TopBarState(isVisible = false))
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
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Card(
                modifier = Modifier.fillMaxSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
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
                        if (categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
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
                            // =====================================================
                            // ===== KROK 2: Posielame lambda funkciu do View =====
                            // =====================================================
                            is TutorialContentBlock.ImageBlock -> ImageBlockView(
                                block = block,
                                onImageClick = { uri ->
                                    enlargedImageUri = uri // Nastavíme URI na zväčšenie
                                }
                            )
                        }
                    }
                }
            }
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

// =========================================================================
// ===== KROK 3: Upravíme ImageBlockView, aby prijímal a použil onImageClick =====
// =========================================================================
@Composable
fun ImageBlockView(
    block: TutorialContentBlock.ImageBlock,
    onImageClick: (Uri) -> Unit, // Nový parameter
) {
    // Odstránime neexistujúci 'imageRes' a bezpečne pracujeme s 'uriString'
    val imageUri = remember(block.uriString) {
        try {
            block.uriString?.toUri()
        } catch (e: Exception) {
            null // Vráti null, ak je string neplatný
        }
    }

    if (imageUri != null) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Obrázok v návode",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onImageClick(imageUri) }, // Použijeme lambda funkciu
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_background)
        )
    } else {
        // Zobrazí chybový stav, ak URI neexistuje alebo je neplatné
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Obrázok sa nepodarilo načítať",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

// Tento dialóg je univerzálny a teraz ho budeme používať
@Composable
private fun ImageDialog(imageModel: Any?, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        // --- STAVY PRE TRANSFORMÁCIU ---
        // Pamätá si aktuálnu mierku (priblíženie)
        var scale by remember { mutableStateOf(1f) }
        // Pamätá si aktuálny posun (posúvanie priblíženého obrázka)
        var offset by remember { mutableStateOf(Offset.Zero) }

        // Box, ktorý slúži ako pozadie a zároveň spracováva kliknutie pre zatvorenie
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        // Ak je obrázok oddialený, zatvoríme dialóg.
                        // Ak je priblížený, resetujeme priblíženie.
                        if (scale <= 1f) {
                            onDismiss()
                        } else {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Stav, ktorý spracováva gestá (pinch-to-zoom, pan)
            val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                // Aktualizujeme mierku, pričom ju obmedzíme (napr. min 1x, max 5x)
                scale = (scale * zoomChange).coerceIn(1f, 5f)

                // Aktualizujeme posun
                offset += offsetChange
            }

            // Samotný obrázok
            AsyncImage(
                model = imageModel,
                contentDescription = "Zväčšený obrázok",
                modifier = Modifier
                    .fillMaxWidth()
                    // Aplikujeme transformácie (mierka a posun) na grafickú vrstvu
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    // Povolíme spracovanie gest na tomto prvku
                    .transformable(state = state),
                contentScale = ContentScale.Fit
            )
        }
    }
}
