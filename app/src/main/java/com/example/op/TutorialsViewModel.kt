package com.example.op

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

// Dátová trieda pre celý návod
data class Tutorial(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var category: String,
    var content: List<TutorialContentBlock>,
)

class TutorialsViewModel : ViewModel() {

    // --- STAVY PRE ZOZNAM NÁVODOV ---
    private val _tutorials = MutableStateFlow<List<Tutorial>>(emptyList())
    val tutorials: StateFlow<List<Tutorial>> = _tutorials.asStateFlow()

    private val _filteredTutorials = MutableStateFlow<List<Tutorial>>(emptyList())
    val filteredTutorials: StateFlow<List<Tutorial>> = _filteredTutorials.asStateFlow()

    val categories = listOf("Všetky", "Prihlásenie", "Hardvér", "Softvér", "Iné")
    var selectedCategory by mutableStateOf(categories.first())
        private set

    var searchQuery by mutableStateOf("")
        private set

    // --- NOVÉ: STAVY PRE FILTER DIALÓG ---
    var isFilterDialogVisible by mutableStateOf(false)
        private set
    private val _activeCategoryFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeCategoryFilters: StateFlow<Set<String>> = _activeCategoryFilters.asStateFlow()
    val allCategoriesForFilter: List<String> // Zoznam kategórií pre dialóg
        get() = categories.filter { it != "Všetky" }

    // --- STAVY PRE OBRAZOVKU PRIDANIA/ÚPRAVY ---
    var tutorialTitle by mutableStateOf("")
        private set
    var tutorialCategory by mutableStateOf(categories.drop(1).first())
        private set

    private val _contentBlocks = MutableStateFlow<List<TutorialContentBlock>>(emptyList())
    val contentBlocks: StateFlow<List<TutorialContentBlock>> = _contentBlocks.asStateFlow()

    private var editingTutorialId: String? = null
    val isEditing: Boolean get() = editingTutorialId != null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var originalTutorial: Tutorial? = null
    var hasChanges by mutableStateOf(false)
        private set

    init {
        loadInitialTutorials()

        // Sledovanie zmien pre formulár úprav/pridania (bez zmeny)
        viewModelScope.launch {
            combine(
                _contentBlocks,
                snapshotFlow { tutorialTitle },
                snapshotFlow { tutorialCategory }
            ) { blocks, title, category ->
                val currentTutorial = originalTutorial?.copy(
                    title = title,
                    category = category,
                    content = blocks
                )
                hasChanges = currentTutorial != originalTutorial
            }.collect {}
        }

        // UPRAVENÉ: Reaktívne filtrovanie, ktoré teraz zahŕňa aj filter z dialógu
        viewModelScope.launch {
            combine(
                _tutorials,
                snapshotFlow { selectedCategory },
                snapshotFlow { searchQuery }.debounce(300L),
                _activeCategoryFilters // NOVÉ: Sledujeme aj zmeny v aktívnych filtroch
            ) { tutorials, category, query, activeFilters ->
                // Filter podľa záložky (TabRow)
                val tutorialsByCategory = if (category == "Všetky") {
                    tutorials
                } else {
                    tutorials.filter { it.category == category }
                }

                // NOVÉ: Filter z dialógu (Checkboxy) - aplikuje sa na výsledok z TabRow
                val tutorialsByDialogFilter = if (activeFilters.isEmpty()) {
                    tutorialsByCategory
                } else {
                    // Ak je záložka "Všetky", filtrujeme z nej. Ak je iná, filtrujeme z nej.
                    tutorialsByCategory.filter { it.category in activeFilters }
                }

                // Finálny filter podľa vyhľadávania (bez zmeny)
                val finalFilteredList = if (query.isBlank()) {
                    tutorialsByDialogFilter
                } else {
                    tutorialsByDialogFilter.filter { tutorial ->
                        tutorial.title.contains(query, ignoreCase = true) ||
                                tutorial.content.any { block ->
                                    block is TutorialContentBlock.TextBlock && block.text.contains(query, ignoreCase = true)
                                }
                    }
                }
                _filteredTutorials.value = finalFilteredList
            }.collect {}
        }
    }

    private fun loadInitialTutorials() {
        val initialData = listOf(
            Tutorial(
                title = "Ako sa prihlásiť do systému",
                category = "Prihlásenie",
                content = listOf(
                    TutorialContentBlock.TextBlock(text = "Pre prihlásenie do systému použite vaše pridelené meno a heslo."),
                    TutorialContentBlock.ImageBlock(imageRes = R.drawable.ic_launcher_background),
                    TutorialContentBlock.TextBlock(text = "V prípade zabudnutého hesla kontaktujte administrátora.")
                )
            ),
            Tutorial(
                title = "Čistenie hardvéru počítača",
                category = "Hardvér",
                content = listOf(
                    TutorialContentBlock.TextBlock(text = "Pravidelne čistite ventilátory a chladiče od prachu, aby ste predišli prehrievaniu komponentov.")
                )
            )
        )
        _tutorials.value = initialData
    }

    // --- FUNKCIE PRE Ukladanie a Pridávanie obrázkov (bez zmeny) ---
    suspend fun saveTutorial() {
        if (tutorialTitle.isBlank()) return

        val editedTutorial = Tutorial(
            id = editingTutorialId ?: UUID.randomUUID().toString(),
            title = tutorialTitle,
            category = tutorialCategory,
            content = _contentBlocks.value
        )

        if (editingTutorialId == null) {
            _tutorials.update { it + editedTutorial }
        } else {
            _tutorials.update { list ->
                list.map { if (it.id == editingTutorialId) editedTutorial else it }
            }
        }
    }

    private suspend fun saveImageToInternalStorageAndGetUri(context: Context, sourceUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val fileName = "tutorial_image_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.filesDir, fileName)

                inputStream?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destinationFile.toUri()
            } catch (e: Exception) {
                Log.e("TutorialsViewModel", "Chyba pri kopírovaní obrázka", e)
                null
            }
        }
    }

    fun addImageBlockFromUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            val stableUri = saveImageToInternalStorageAndGetUri(context, uri)
            if (stableUri != null) {
                val newBlock = TutorialContentBlock.ImageBlock(uriString = stableUri.toString())
                _contentBlocks.update { it + newBlock }
            } else {
                Log.e("TutorialsViewModel", "Nepodarilo sa spracovať obrázok z URI: $uri")
            }
        }
    }

    // --- NOVÉ: FUNKCIE PRE OVLÁDANIE FILTRA ---
    fun onFilterDialogOpen() {
        isFilterDialogVisible = true
    }

    fun onFilterDialogDismiss() {
        isFilterDialogVisible = false
    }

    fun onFilterCategorySelected(category: String, isSelected: Boolean) {
        _activeCategoryFilters.update { currentFilters ->
            val newFilters = currentFilters.toMutableSet()
            if (isSelected) {
                newFilters.add(category)
            } else {
                newFilters.remove(category)
            }
            newFilters
        }
    }

    fun clearAllFilters() {
        _activeCategoryFilters.value = emptySet()
    }


    // --- OSTATNÉ FUNKCIE (s drobnou úpravou) ---

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    // ✅ TÚTO FUNKCIU SEM PRIDAJTE
    /**
     * Resetuje filter kategórií (záložiek) na predvolenú hodnotu ("Všetky").
     * Volá sa pri opustení sekcie Návodov, aby sa pri návrate vždy
     * zobrazila východzia záložka.
     */
    fun resetTabToDefault() {
        // Jednoducho zavoláme existujúcu funkciu `onCategorySelected`
        // s predvolenou hodnotou "Všetky".
        onCategorySelected(categories.first())
    }

    // UPRAVENÉ: Pri zmene kategórie resetujeme aj filter z dialógu
    fun onCategorySelected(category: String) {
        selectedCategory = category
        // Pri prepnutí záložky chceme vždy zrušiť filtre z dialógu aj vyhľadávanie
        clearAllFilters()
        onSearchQueryChange("")
    }

    fun onTitleChange(newTitle: String) {
        tutorialTitle = newTitle
    }

    fun onCategoryChange(newCategory: String) {
        tutorialCategory = newCategory
    }

    fun addTextBlock() {
        _contentBlocks.update { it + TutorialContentBlock.TextBlock(text = "") }
    }

    fun onContentBlockChange(index: Int, newBlock: TutorialContentBlock) {
        val currentBlocks = _contentBlocks.value.toMutableList()
        if (index in currentBlocks.indices) {
            currentBlocks[index] = newBlock
            _contentBlocks.value = currentBlocks
        }
    }

    fun removeContentBlock(index: Int) {
        _contentBlocks.update {
            it.toMutableList().apply { removeAt(index) }
        }
    }

    fun moveContentBlock(from: Int, to: Int) {
        _contentBlocks.update {
            it.toMutableList().apply {
                val item = removeAt(from)
                add(to, item)
            }
        }
    }

    fun loadTutorialForEditing(tutorialId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val tutorial = _tutorials.value.firstOrNull { it.id == tutorialId }
                if (tutorial != null) {
                    originalTutorial = tutorial.copy()
                    editingTutorialId = tutorial.id
                    tutorialTitle = tutorial.title
                    tutorialCategory = tutorial.category
                    _contentBlocks.value = tutorial.content
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTutorial(tutorialId: String) {
        viewModelScope.launch {
            _tutorials.update { list -> list.filterNot { it.id == tutorialId } }
        }
    }

    fun deleteCurrentlyEditingTutorial() {
        editingTutorialId?.let {
            deleteTutorial(it)
        }
    }

    fun resetForm() {
        editingTutorialId = null
        tutorialTitle = ""
        tutorialCategory = categories.drop(1).first()
        _contentBlocks.value = emptyList()

        originalTutorial = Tutorial(id = "new", title = "", category = tutorialCategory, content = emptyList())
        hasChanges = false
    }
}
