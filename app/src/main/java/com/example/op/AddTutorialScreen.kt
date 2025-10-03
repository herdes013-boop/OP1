package com.example.op

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTutorialScreen(
    navController: NavController,
    tutorialsViewModel: TutorialsViewModel,
    sharedViewModel: SharedViewModel,
    modifier: Modifier = Modifier // Tento modifier dostane padding z MainScreen
) {
    val contentBlocks by tutorialsViewModel.contentBlocks.collectAsState()

    // Tento DisposableEffect sa postará o resetovanie líšt pri odchode z obrazovky
    DisposableEffect(Unit) {
        onDispose {
            sharedViewModel.resetTopBarState()
            sharedViewModel.setShowBottomBar(true)
        }
    }

    // ======================== ZAČIATOK FINÁLNEJ OPRAVY ========================

    // Vytvoríme si vlastný Scaffold, ktorý bude rešpektovať padding z MainScreen
    Scaffold(
        modifier = modifier, // 1. Aplikujeme padding z MainScreen na celý náš Scaffold
        topBar = {
            // 2. Naša vlastná horná lišta pre túto obrazovku
            TopAppBar(
                title = { Text("Nový návod") },
                navigationIcon = {
                    IconButton(onClick = {
                        sharedViewModel.resetTopBarState() // Pri návrate resetujeme lištu
                        sharedViewModel.setShowBottomBar(true)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "Naspäť")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (tutorialsViewModel.tutorialTitle.isNotBlank()) {
                                tutorialsViewModel.addTutorial()
                                navController.popBackStack() // onDispose sa postará o reset
                            }
                        },
                        enabled = tutorialsViewModel.tutorialTitle.isNotBlank()
                    ) {
                        Text("Uložiť")
                    }
                }
            )
        },
        bottomBar = {
            // 3. Naša vlastná spodná lišta pre pridávanie blokov
            EditorControls(
                onAddText = { tutorialsViewModel.addTextBlock() },
                onAddImage = { tutorialsViewModel.addImageBlock() }
            )
        },
        // Tu už nepoužívame LaunchedEffect, lebo topBar sa mení priamo tu.
        // Skrytie hlavnej spodnej lišty zabezpečíme v TutorialsNavHost.
    ) { innerPadding ->
        // 4. LazyColumn dostane 'innerPadding' z NÁŠHO Scaffoldu, ktorý už vie o našich lištách
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Kľúčová zmena!
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                when (block) {
                    is TutorialContentBlock.TextBlock -> {
                        TextBlockEditor(
                            block = block,
                            onTextChange = { newText -> tutorialsViewModel.onTextBlockChange(block.id, newText) },
                            onRemove = { tutorialsViewModel.removeBlock(block.id) }
                        )
                    }
                    is TutorialContentBlock.ImageBlock -> {
                        ImageBlockEditor(
                            block = block,
                            onImageChange = { newImage -> tutorialsViewModel.onImageBlockChange(block.id, newImage) },
                            onRemove = { tutorialsViewModel.removeBlock(block.id) }
                        )
                    }
                }
            }
        }
    }
    // ======================== KONIEC FINÁLNEJ OPRAVY ========================
}


// Ostatné funkcie (TextBlockEditor, ImageBlockEditor, atď.) ostávajú úplne bez zmeny.
// ... ( zvyšok vášho súboru )
@Composable
fun TextBlockEditor(
    block: TutorialContentBlock.TextBlock,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var localText by remember { mutableStateOf(block.text) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        OutlinedTextField(
            value = localText,
            onValueChange = { newText ->
                localText = newText
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp)
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

@Composable
fun ImageBlockEditor(
    block: TutorialContentBlock.ImageBlock,
    onImageChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable {
                    val nextImage =
                        if (block.imageRes == R.drawable.ic_launcher_foreground) R.drawable.ic_launcher_background else R.drawable.ic_launcher_foreground
                    onImageChange(nextImage)
                },
            contentAlignment = Alignment.Center
        ) {
            val imageResource = block.imageRes
            if (imageResource != null) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = "Obrázok bloku",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Pridať obrázok", tint = Color.Gray)
                    Text("Klikni pre pridanie obrázka", color = Color.Gray)
                }
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(Color.White.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Odstrániť blok")
        }
    }
}

@Composable
fun EditorControls(
    modifier: Modifier = Modifier,
    onAddText: () -> Unit,
    onAddImage: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onAddText) {
                Icon(Icons.Default.TextFields, contentDescription = "Pridať text")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pridať Text")
            }
            Button(onClick = onAddImage) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Pridať obrázok")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pridať Obrázok")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
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
