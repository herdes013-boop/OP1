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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.op.ui.theme.TelekomMagenta
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTutorialScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // --- STAVY ---
    var localTitle by remember { mutableStateOf("") }
    var localCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCategorySelectionDialog by remember { mutableStateOf(false) }
    var localContentBlocks by remember { mutableStateOf<List<TutorialContentBlock>>(emptyList()) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    // --- DETEKCIA ZMIEN ---
    val hasChanges = localTitle.isNotBlank() || localContentBlocks.isNotEmpty()

    // --- POMOCNÉ FUNKCIE ---
    fun saveAndGoBack() {
        focusManager.clearFocus()
        scope.launch {
            val tutorialToSave = Tutorial(
                id = UUID.randomUUID().toString(),
                title = localTitle,
                categories = localCategories, // <-- POUŽÍVAME NOVÝ STAV A PARAMETER
                content = localContentBlocks
            )
            tutorialsViewModel.saveTutorial(tutorialToSave)
            navController.popBackStack()
        }
    }

    fun handleBackNavigation() {
        if (hasChanges) {
            showUnsavedChangesDialog = true
        } else {
            navController.popBackStack()
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
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

// 2. Spúšťač pre fotoaparát
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                tempPhotoUri?.let { uri ->
                    scope.launch {
                        // Použijeme existujúcu funkciu na uloženie URI do interného úložiska
                        val stableUri = tutorialsViewModel.saveImageToInternalStorageAndGetUri(context, uri)
                        if (stableUri != null) {
                            localContentBlocks = localContentBlocks + TutorialContentBlock.ImageBlock(uriString = stableUri.toString())
                        }
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
            onSave = { showUnsavedChangesDialog = false; saveAndGoBack() },
            onDiscard = { showUnsavedChangesDialog = false; navController.popBackStack() },
            onCancel = { showUnsavedChangesDialog = false }
        )
    }

    if (showCategorySelectionDialog) {
        CategorySelectionDialog(
            allCategories = tutorialsViewModel.categories.filter { it != "Všetky" },
            selectedCategories = localCategories,
            onDismiss = { showCategorySelectionDialog = false },
            onConfirm = { newSelectedCategories ->
                localCategories = newSelectedCategories
                showCategorySelectionDialog = false
            }
        )
    }

    // --- HLAVNÉ UI ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Nový návod") },
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
                CategorySelector(
                    allCategories = tutorialsViewModel.categories.filter { it != "Všetky" },
                    selectedCategories = localCategories,
                    onOpenDialog = { showCategorySelectionDialog = true }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Zmenšená medzera
                ) {
                    val buttonColor = Color(0xFF4CAF50)
                    // Tlačidlo Text
                    Button(
                        onClick = {
                            scope.launch {
                                localContentBlocks = localContentBlocks + TutorialContentBlock.TextBlock(text = "")
                                if (localContentBlocks.isNotEmpty()) listState.animateScrollToItem(localContentBlocks.lastIndex)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(horizontal = 8.dp) // Menší padding
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Text")
                    }
                    // Tlačidlo Obrázok
                    Button(
                        onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(horizontal = 8.dp) // Menší padding
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Obrázok")
                    }
                    // NOVÉ TLAČIDLO FOTOAPARÁT
                    Button(
                        onClick = {
                            // ===== UPRAVTE TÚTO ČASŤ =====
                            scope.launch {
                                // Vytvoríme dočasný súbor, kam fotoaparát uloží obrázok
                                val newUri = tutorialsViewModel.createTempImageFile(context)
                                tempPhotoUri = newUri // Uložíme si URI
                                cameraLauncher.launch(newUri) // Spustíme fotoaparát s týmto URI
                            }
                            // =============================
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(horizontal = 8.dp) // Menší padding
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null) // Nová ikona
                        Spacer(Modifier.width(4.dp))
                        Text("Foto")
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

// Dialog na neuložené zmeny (pomocný komponent)

