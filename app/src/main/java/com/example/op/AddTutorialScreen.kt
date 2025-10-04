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
    // ======================= OPRAVA (KROK 1) =======================
    // Používame novú, verejnú premennú `isEditing` z ViewModelu.
    val isEditing = tutorialsViewModel.isEditing
    // ===============================================================

    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Skryjeme hlavnú hornú lištu, keď sme na tejto obrazovke
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(TopBarState(isVisible = false))
    }

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
                // ======================= OPRAVA (KROK 2) =======================
                // Používame novú, bezpečnú funkciu z ViewModelu.
                tutorialsViewModel.deleteCurrentlyEditingTutorial()
                // ===============================================================
                navController.popBackStack()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            // Pri odchode z obrazovky resetujeme formulár vo ViewModeli
            tutorialsViewModel.resetForm()
            // A zabezpečíme, aby sa horná lišta opäť ukázala na predchádzajúcej obrazovke
            sharedViewModel.resetTopBarState()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť návod" else "Nový návod") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Kontrola neuložených zmien by mala byť vo ViewModeli
                        // if (tutorialsViewModel.hasUnsavedChanges()) {
                        //     showUnsavedChangesDialog = true
                        // } else {
                        //     navController.popBackStack()
                        // }
                        // Zatiaľ zjednodušené:
                        navController.popBackStack()
                    }) { Icon(Icons.Filled.ArrowBack, "Naspäť") }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, "Zmazať návod", tint = MaterialTheme.colorScheme.error)
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

        // Presunul som tieto premenné dnu do obsahu Scaffold, kde patria
        val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
            // Potrebujeme overiť, či sú indexy platné pre zoznam blokov
            if (from.index >= 1 && to.index >= 1) { // 0 je titulok
                tutorialsViewModel.moveContentBlock(from.index - 1, to.index - 1)
            }
        })

        LazyColumn(
            state = reorderableState.listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .reorderable(reorderableState),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "tutorial_meta") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tutorialsViewModel.tutorialTitle,
                        onValueChange = { tutorialsViewModel.onTitleChange(it) },
                        label = { Text("Názov návodu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    CategorySelector(
                        categories = tutorialsViewModel.categories.filter { it != "Všetky" },
                        selectedCategory = tutorialsViewModel.tutorialCategory,
                        onCategorySelected = { tutorialsViewModel.onCategoryChange(it) }
                    )
                }
            }

            items(contentBlocks, key = { it.id }) { block ->
                ReorderableItem(reorderableState, key = block.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation_anim")
                    val reorderModifier = Modifier
                        .detectReorderAfterLongPress(reorderableState)
                        .shadow(elevation, RoundedCornerShape(8.dp))

                    // Tu bola chyba v logike, id blokov sa prenáša inak
                    when (block) {
                        is TutorialContentBlock.TextBlock -> {
                            TextBlockEditor(
                                block = block,
                                onTextChange = { newText ->
                                    val index = contentBlocks.indexOf(block)
                                    if(index != -1) tutorialsViewModel.onContentBlockChange(index, block.copy(text = newText))
                                },
                                onRemove = {
                                    val index = contentBlocks.indexOf(block)
                                    if(index != -1) tutorialsViewModel.removeContentBlock(index)
                                },
                                modifier = reorderModifier
                            )
                        }
                        is TutorialContentBlock.ImageBlock -> {
                            ImageBlockEditor(
                                block = block,
                                onImageChange = { newImageRes ->
                                    val index = contentBlocks.indexOf(block)
                                    if(index != -1) tutorialsViewModel.onContentBlockChange(index, block.copy(imageRes = newImageRes))
                                },
                                onRemove = {
                                    val index = contentBlocks.indexOf(block)
                                    if(index != -1) tutorialsViewModel.removeContentBlock(index)
                                },
                                modifier = reorderModifier
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TextBlockEditor(
    block: TutorialContentBlock.TextBlock,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localText by remember(block.id, block.text) { mutableStateOf(block.text) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Presunúť",
            modifier = Modifier.padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = localText,
            onValueChange = { localText = it },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && localText != block.text) {
                        onTextChange(localText)
                    }
                },
            label = { Text("Textový blok") },
            trailingIcon = {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Odstrániť blok", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
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
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { /* Logika pre výber obrázka z galérie by bola tu */ },
                contentAlignment = Alignment.Center
            ) {
                val imageResource = block.imageRes
                if (imageResource != null) {
                    androidx.compose.foundation.Image(
                        painterResource(id = imageResource),
                        "Obrázok bloku",
                        Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, "Pridať obrázok", tint = MaterialTheme.colorScheme.onSurface)
                        Text("Klikni pre pridanie obrázka", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Odstrániť blok", tint = MaterialTheme.colorScheme.onErrorContainer)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onAddText) {
                Icon(Icons.Default.TextFields, "Pridať text")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Text")
            }
            Button(onClick = onAddImage) {
                Icon(Icons.Default.AddAPhoto, "Pridať obrázok")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Obrázok")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Column {
        Text("Kategória", style = MaterialTheme.typography.labelLarge)
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
