package com.example.op

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.requestFocus
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
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTutorialScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    tutorialId: String, // Pre editáciu je ID povinné
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // --- STAVY ---
    var localTitle by remember { mutableStateOf("") }
    var localCategory by remember { mutableStateOf("") }
    var localContentBlocks by remember { mutableStateOf<List<TutorialContentBlock>>(emptyList()) }
    var originalTutorial by remember { mutableStateOf<Tutorial?>(null) }
    var isDataLoaded by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // --- NAČÍTANIE DÁT ---
    LaunchedEffect(tutorialId) {
        val tutorial = tutorialsViewModel.getTutorialById(tutorialId)
        if (tutorial != null) {
            localTitle = tutorial.title
            localCategory = tutorial.category
            localContentBlocks = tutorial.content
            originalTutorial = tutorial.copy()
            isDataLoaded = true
        } else {
            // Ak sa návod nenájde, vrátime sa späť
            navController.popBackStack()
        }
    }

    // --- DETEKCIA ZMIEN ---
    val hasChanges by remember(localTitle, localCategory, localContentBlocks, originalTutorial) {
        derivedStateOf {
            originalTutorial?.let {
                it.title != localTitle || it.category != localCategory || it.content != localContentBlocks
            } ?: false
        }
    }

    // --- POMOCNÉ FUNKCIE ---
    fun saveAndGoBack() {
        scope.launch {
            val tutorialToSave = Tutorial(
                id = tutorialId,
                title = localTitle,
                category = localCategory,
                content = localContentBlocks
            )
            tutorialsViewModel.saveTutorial(tutorialToSave)
            // ✅ SPRÁVNY NÁVRAT O 2 KROKY:
            // Vráti sa na obrazovku PRED TutorialDetail a zároveň TutorialDetail odstráni z histórie.
            navController.popBackStack(Routes.TUTORIAL_DETAIL, true)
        }
    }

    fun handleBackNavigation() {
        if (hasChanges) {
            showUnsavedChangesDialog = true
        } else {
            // ✅ SPRÁVNY NÁVRAT O 2 KROKY aj tu:
            navController.popBackStack(Routes.TUTORIAL_DETAIL, true)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    val stableUri = tutorialsViewModel.saveImageToInternalStorageAndGetUri(context, it)
                    if (stableUri != null) {
                        localContentBlocks = localContentBlocks + TutorialContentBlock.ImageBlock(uriString = stableUri.toString())
                    }
                }
            }
        }
    )

    // --- TOP BAR A BACK HANDLER ---
    LaunchedEffect(Unit) {
        sharedViewModel.setTopBarState(TopBarState(isVisible = false))
    }
    BackHandler(onBack = ::handleBackNavigation)

    // --- DIALÓGY ---
    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSave = { showUnsavedChangesDialog = false; saveAndGoBack() }, // Toto je už v poriadku po kroku 1
            onDiscard = {
                showUnsavedChangesDialog = false
                // ✅ SPRÁVNY NÁVRAT O 2 KROKY aj tu:
                navController.popBackStack(Routes.TUTORIAL_DETAIL, true)
            },
            onCancel = { showUnsavedChangesDialog = false }
        )
    }
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                tutorialsViewModel.deleteTutorial(tutorialId)
                // Navigácia o 2 kroky späť (z Editácie cez Detail na Zoznam)
                navController.popBackStack(Routes.TUTORIAL_DETAIL, true)
            }
        )
    }

    // --- HLAVNÉ UI ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Upraviť návod") },
                navigationIcon = {
                    IconButton(onClick = ::handleBackNavigation) {
                        Icon(Icons.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    if (hasChanges && localTitle.isNotBlank()) {
                        Button(
                            onClick = ::saveAndGoBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) { Text("ULOŽIŤ") }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Viac")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Zmazať", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, "Zmazať", tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelekomMagenta,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (!isDataLoaded) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .imePadding()
            ) {
                // HORNÁ ČASŤ
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = localTitle,
                        onValueChange = { localTitle = it },
                        label = { Text("Názov návodu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    CategoryDropDown(
                        categories = tutorialsViewModel.categories.filter { it != "Všetky" },
                        selectedCategory = localCategory,
                        onCategorySelected = { localCategory = it }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val buttonColor = Color(0xFF4CAF50)
                        Button(
                            onClick = {
                                scope.launch {
                                    localContentBlocks = localContentBlocks + TutorialContentBlock.TextBlock(text = "")
                                    if (localContentBlocks.isNotEmpty()) listState.animateScrollToItem(localContentBlocks.lastIndex)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Text")
                        }
                        Button(
                            onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Obrázok")
                        }
                    }
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // SPODNÁ ČASŤ (ZOZNAM BLOKOV)
                val reorderableState = rememberReorderableLazyListState(onMove = { from, to ->
                    localContentBlocks = localContentBlocks.toMutableList().apply { add(to.index, removeAt(from.index)) }
                })
                val focusManager = LocalFocusManager.current
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .reorderable(reorderableState)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { focusManager.clearFocus() }
                        ),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(localContentBlocks, key = { _, block -> block.id }) { index, block ->
                        ReorderableItem(reorderableState, key = block.id) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "")
                            val reorderModifier = Modifier
                                .detectReorderAfterLongPress(reorderableState)
                                .shadow(elevation, RoundedCornerShape(8.dp))

                            when (block) {
                                is TutorialContentBlock.TextBlock -> TextBlockEditor(
                                    block = block,
                                    onTextChange = { newText ->
                                        localContentBlocks = localContentBlocks.toMutableList().apply { this[index] = block.copy(text = newText) }
                                    },
                                    onRemove = {
                                        localContentBlocks = localContentBlocks.toMutableList().apply { removeAt(index) }
                                    },
                                    modifier = reorderModifier
                                )
                                is TutorialContentBlock.ImageBlock -> ImageBlockEditor(
                                    block = block,
                                    onRemove = {
                                        localContentBlocks = localContentBlocks.toMutableList().apply { removeAt(index) }
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
}
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
    val focusRequester = remember { FocusRequester() }

    // Požiada o focus, keď sa blok prvýkrát zobrazí a je prázdny
    LaunchedEffect(Unit) {
        if (block.text.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

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
                .focusRequester(focusRequester)
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
        title = { Text("Zmazať návod") },text = { Text("Naozaj chcete natrvalo zmazať tento návod?") },
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