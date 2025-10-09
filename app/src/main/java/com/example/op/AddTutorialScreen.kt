package com.example.op

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.op.ui.theme.TelekomMagenta
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
    modifier: Modifier = Modifier,
) {
    val contentBlocks by tutorialsViewModel.contentBlocks.collectAsState()
    val isEditing = tutorialsViewModel.isEditing
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { tutorialsViewModel.addImageBlockFromUri(it, context) }
        }
    )

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
            tutorialsViewModel.resetForm()
            sharedViewModel.resetTopBarState()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Upraviť návod" else "Nový návod") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    // Tlačidlo je naozaj viditeľné, len ak sú zmeny a je vyplnený názov
                    if (tutorialsViewModel.hasChanges && tutorialsViewModel.tutorialTitle.isNotBlank()) {
                        Button(
                            onClick = {
                                tutorialsViewModel.saveTutorial()
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50), // Zelená
                                contentColor = Color.White
                            )
                        ) {
                            Text("Uložiť")
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelekomMagenta,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                )
            )
        }
    ) { innerPadding ->
        // FINÁLNA ŠTRUKTÚRA: Statická horná časť a skrolovateľná dolná časť
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

            // --- STATICKÁ HORNÁ SEKCIA (nebude sa skrolovať) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Názov a Kategória
                OutlinedTextField(
                    value = tutorialsViewModel.tutorialTitle,
                    onValueChange = { tutorialsViewModel.onTitleChange(it) },
                    label = { Text("Názov návodu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                CategoryDropDown(
                    categories = tutorialsViewModel.categories.filter { it != "Všetky" },
                    selectedCategory = tutorialsViewModel.tutorialCategory,
                    onCategorySelected = { tutorialsViewModel.onCategoryChange(it) }
                )

                // Tlačidlá na pridávanie
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val buttonColor = Color(0xFF4CAF50) // Zelená
                    Button(
                        onClick = { tutorialsViewModel.addTextBlock() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Text")
                    }
                    Button(
                        onClick = {
                            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Obrázok")
                    }
                }
            }

            // Oddeľovač
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- SKROLOVATEĽNÁ SEKCIA S OBSAHOM ---
            val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
                // Indexy sú teraz jednoduché, lebo itemsIndexed začína od 0
                tutorialsViewModel.moveContentBlock(from.index, to.index)
            })

            // ✅ SPRÁVNE MIESTO PRE FOCUS MANAGER
            val focusManager = LocalFocusManager.current

            LazyColumn(
                state = reorderableState.listState,
                // ✅ SPRÁVNE PRIDANÝ MODIFIER PRE KLIKNUTIE
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(reorderableState)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Bez vizuálneho efektu pri kliknutí
                    ) {
                        focusManager.clearFocus()
                    },
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(contentBlocks, key = { _, block -> block.id }) { index, block ->
                    ReorderableItem(reorderableState, key = block.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation_anim")
                        val reorderModifier = Modifier
                            .detectReorderAfterLongPress(reorderableState)
                            .shadow(elevation, RoundedCornerShape(8.dp))

                        when (block) {
                            is TutorialContentBlock.TextBlock -> TextBlockEditor(
                                block = block,
                                onTextChange = { newText -> tutorialsViewModel.onContentBlockChange(index, block.copy(text = newText)) },
                                onRemove = { tutorialsViewModel.removeContentBlock(index) },
                                modifier = reorderModifier
                            )
                            is TutorialContentBlock.ImageBlock -> ImageBlockEditor(
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


// --- POMOCNÉ KOMPONENTY (bezo zmeny) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropDown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Kategória") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TextBlockEditor(
    block: TutorialContentBlock.TextBlock,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var localText by remember(block.id) { mutableStateOf(block.text) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageModel = block.uriString?.toUri() ?: block.imageRes

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Presunúť",
            modifier = Modifier.padding(end = 8.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.weight(1f)) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Obrázok návodu",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_background)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Chyba obrázka",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
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
