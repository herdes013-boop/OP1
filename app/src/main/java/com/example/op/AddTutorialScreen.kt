package com.example.op

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTutorialScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val contentBlocks by tutorialsViewModel.contentBlocks.collectAsState()
    val isEditing = tutorialsViewModel.editingTutorialId != null

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedChangesDialog = false },
            onConfirm = {
                showUnsavedChangesDialog = false
                navController.popBackStack()
            }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                tutorialsViewModel.editingTutorialId?.let { tutorialsViewModel.deleteTutorial(it) }
                navController.popBackStack()
            }
        )
    }

    // ========= PRIDANÝ BLOK KÓDU =========
    // Tento blok zabezpečí, že po opustení tejto obrazovky sa stav TopAppBar
    // vráti do predvoleného stavu (t.j. stane sa opäť viditeľnou).
    DisposableEffect(Unit) {
        onDispose {
            sharedViewModel.resetTopBarState()
        }
    }
    // ========= KONIEC PRIDANÉHO BLOKU =========

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť návod" else "Nový návod") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (tutorialsViewModel.hasUnsavedChanges()) {
                            showUnsavedChangesDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) { Icon(Icons.Filled.ArrowBack, "Naspäť") }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, "Zmazať návod")
                        }
                    }
                    Button(
                        onClick = {
                            if (tutorialsViewModel.tutorialTitle.isNotBlank()) {
                                tutorialsViewModel.saveTutorial()
                                navController.popBackStack()
                            }
                        },
                        enabled = tutorialsViewModel.tutorialTitle.isNotBlank()
                    ) { Text("Uložiť") }
                }
            )
        },
        bottomBar = {
            EditorControls(
                onAddText = { tutorialsViewModel.addTextBlock() },
                onAddImage = { tutorialsViewModel.addImageBlock() }
            )
        }
    ) { innerPadding ->

        val nonReorderableItemCount = 2
        val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
            val fromIndex = from.index - nonReorderableItemCount
            val toIndex = to.index - nonReorderableItemCount
            if (fromIndex >= 0 && toIndex >= 0) {
                tutorialsViewModel.moveBlock(fromIndex, toIndex)
            }
        })

        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .reorderable(reorderableState),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "tutorial_title") {
                OutlinedTextField(
                    value = tutorialsViewModel.tutorialTitle,
                    onValueChange = { tutorialsViewModel.onTitleChange(it) },
                    label = { Text("Názov návodu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item(key = "tutorial_category") {
                CategorySelector(
                    categories = tutorialsViewModel.categories.filter { it != "Všetky" },
                    selectedCategory = tutorialsViewModel.tutorialCategory,
                    onCategorySelected = { tutorialsViewModel.onCategoryChange(it) }
                )
            }

            items(contentBlocks, key = { it.id }) { block ->
                ReorderableItem(reorderableState, key = block.id) { isDragging ->
                    val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation_anim")
                    val reorderModifier = Modifier
                        .detectReorderAfterLongPress(reorderableState)
                        .shadow(elevation.value, RoundedCornerShape(8.dp))

                    when (block) {
                        is TutorialContentBlock.TextBlock -> {
                            TextBlockEditor(
                                block = block,
                                onTextChange = { newText -> tutorialsViewModel.onTextBlockChange(block.id, newText) },
                                onRemove = { tutorialsViewModel.removeBlock(block.id) },
                                modifier = reorderModifier
                            )
                        }
                        is TutorialContentBlock.ImageBlock -> {
                            ImageBlockEditor(
                                block = block,
                                onImageChange = { newImage -> tutorialsViewModel.onImageBlockChange(block.id, newImage) },
                                onRemove = { tutorialsViewModel.removeBlock(block.id) },
                                modifier = reorderModifier
                            )
                        }
                    }
                }
            }
        }
    }
}

// Ostatné funkcie (TextBlockEditor, ImageBlockEditor, dialógy, atď.) zostávajú bez zmeny.
// ... (všetok váš ostatný kód v tomto súbore) ...

@Composable
fun TextBlockEditor(
    block: TutorialContentBlock.TextBlock,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localText by remember(block.text) { mutableStateOf(block.text) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Presunúť",
            modifier = Modifier.padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = localText,
                onValueChange = { newText -> localText = newText },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp, top = 4.dp, bottom = 4.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            onTextChange(localText)
                        }
                    },
                label = { Text("Textový blok") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Odstrániť blok", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ImageBlockEditor(
    block: TutorialContentBlock.ImageBlock,
    onImageChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Presunúť",
            modifier = Modifier.padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        val nextImage = if (block.imageRes == R.drawable.ic_launcher_foreground) R.drawable.ic_launcher_background else R.drawable.ic_launcher_foreground
                        onImageChange(nextImage)
                    },
                contentAlignment = Alignment.Center
            ) {
                val imageResource = block.imageRes
                if (imageResource != null) {
                    androidx.compose.foundation.Image(painterResource(id = imageResource), "Obrázok bloku", Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, "Pridať obrázok", tint = Color.Gray)
                        Text("Klikni pre pridanie obrázka", color = Color.Gray)
                    }
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Odstrániť blok")
            }
        }
    }
}

@Composable
fun UnsavedChangesDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neuložené zmeny") },
        text = { Text("Naozaj chcete odísť bez uloženia zmien?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Odísť") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zostať") } }
    )
}

@Composable
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmazať návod") },
        text = { Text("Naozaj chcete natrvalo zmazať tento návod?") },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Zmazať")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušiť") } }
    )
}

@Composable
fun EditorControls(modifier: Modifier = Modifier, onAddText: () -> Unit, onAddImage: () -> Unit) {
    Surface(modifier = modifier.fillMaxWidth(), shadowElevation = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onAddText) {
                Icon(Icons.Default.TextFields, "Pridať text")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pridať Text")
            }
            Button(onClick = onAddImage) {
                Icon(Icons.Default.AddAPhoto, "Pridať obrázok")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pridať Obrázok")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column {
        Text("Kategória", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = (category == selectedCategory),
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}
