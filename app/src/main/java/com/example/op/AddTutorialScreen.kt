package com.example.op

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // ✅ ZMENA: Potrebujeme index pre lepšiu stabilitu
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
import androidx.compose.ui.layout.ContentScale // ✅ ZMENA: Pre zobrazenie obrázku
import androidx.compose.ui.platform.LocalContext // ✅ ZMENA: Potrebujeme context pre ViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri // ✅ ZMENA: Pre konverziu Stringu na Uri
import androidx.navigation.NavController
import coil.compose.AsyncImage // ✅ ZMENA: Kľúčový import pre obrázky
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.UUID // ✅ ZMENA: Import pre unikátny kľúč

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTutorialScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val contentBlocks by tutorialsViewModel.contentBlocks.collectAsState()
    val isEditing = tutorialsViewModel.isEditing

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // ✅ ZMENA: Launcher pre výber obrázkov z galérie
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Po úspešnom výbere obrázku voláme novú funkciu vo ViewModele
                tutorialsViewModel.addImageBlockFromUri(it, context)
            }
        }
    )

    // Skryjeme hlavnú hornú lištu
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(TopBarState(isVisible = false))
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                tutorialsViewModel.deleteCurrentlyEditingTutorial()
                navController.popBackStack()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            // Pri odchode resetujeme formulár a hornú lištu
            tutorialsViewModel.resetForm()
            sharedViewModel.resetTopBarState()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť návod" else "Nový návod") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, "Zmazať návod", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Button(
                        onClick = {
                            tutorialsViewModel.saveTutorial()
                            navController.popBackStack()
                        },
                        enabled = tutorialsViewModel.tutorialTitle.isNotBlank()
                    ) { Text("Uložiť") }
                }
            )
        },
        bottomBar = {
            EditorControls(
                onAddText = { tutorialsViewModel.addTextBlock() },
                // ✅ ZMENA: Pri kliknutí na tlačidlo "Obrázok" spustíme image picker
                onAddImage = { imagePickerLauncher.launch("image/*") }
            )
        }
    ) { innerPadding ->
        val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
            // Indexy sú posunuté o 1, pretože prvou položkou v LazyColumn je hlavička
            val fromIndex = from.index - 1
            val toIndex = to.index - 1
            if (fromIndex >= 0 && toIndex >= 0) {
                tutorialsViewModel.moveContentBlock(fromIndex, toIndex)
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

            // ✅ ZMENA: Používame `itemsIndexed` pre stabilné indexy
            itemsIndexed(contentBlocks, key = { _, block -> block.id }) { index, block ->
                ReorderableItem(reorderableState, key = block.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation_anim")
                    val reorderModifier = Modifier
                        .detectReorderAfterLongPress(reorderableState)
                        .shadow(elevation, RoundedCornerShape(8.dp))

                    when (block) {
                        is TutorialContentBlock.TextBlock -> {
                            TextBlockEditor(
                                block = block,
                                onTextChange = { newText ->
                                    tutorialsViewModel.onContentBlockChange(index, block.copy(text = newText))
                                },
                                onRemove = { tutorialsViewModel.removeContentBlock(index) },
                                modifier = reorderModifier
                            )
                        }
                        is TutorialContentBlock.ImageBlock -> {
                            // ✅ ZMENA: Voláme opravený ImageBlockEditor
                            ImageBlockEditor(
                                block = block,
                                onRemove = { tutorialsViewModel.removeContentBlock(index) },
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
    var localText by remember(block.id) { mutableStateOf(block.text) }

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

// ✅ ZMENA: Kompletne prepracovaný ImageBlockEditor
@Composable
fun ImageBlockEditor(
    block: TutorialContentBlock.ImageBlock,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Presunúť",
            modifier = Modifier.padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Kontajner pre obrázok a tlačidlo na zmazanie
        Box(modifier = Modifier.weight(1f)) {
            // Používame AsyncImage z knižnice Coil na bezpečné načítanie obrázku
            AsyncImage(
                model = block.uriString.toUri(), // Konvertujeme String na Uri
                contentDescription = "Obrázok návodu",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop // Oreže obrázok, aby vyplnil priestor
            )
            // Tlačidlo na zmazanie v rohu
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Odstrániť blok", tint = Color.White)
            }
        }
    }
}


// Zvyšok súboru (dialogy, controls, selector) zostáva bez zmeny
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
@Composable
fun DeleteConfirmDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmazať návod") },
        text = { Text("Naozaj chcete natrvalo zmazať tento návod?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Zmazať")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Zrušiť")
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    // ... (bez zmeny)
}
// ... (ostatné dialogy bez zmeny)

