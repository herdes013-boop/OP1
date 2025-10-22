package com.example.op

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.core.content.FileProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Dátová trieda pre celý návod
data class Tutorial(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var categories: List<String>, // <-- ZMENENÝ RIADOK
    var content: List<TutorialContentBlock>,
)

class TutorialsViewModel : ViewModel() {

    // --- STAVY PRE ZOZNAM NÁVODOV ---
    private val _tutorials = MutableStateFlow<List<Tutorial>>(emptyList())
    val tutorials: StateFlow<List<Tutorial>> = _tutorials.asStateFlow()

    private val _filteredTutorials = MutableStateFlow<List<Tutorial>>(emptyList())
    val filteredTutorials: StateFlow<List<Tutorial>> = _filteredTutorials.asStateFlow()

    val managedCategories =
        mutableStateListOf("Prihlásenie", "Hardvér", "Softvér", "Iné")

    // Toto je odvodený zoznam, ktorý budeme používať v celej aplikácii.
    // UI tak nemusíme meniť na všetkých miestach.
    val categories: List<String> by derivedStateOf {
        listOf("Všetky") + managedCategories
    }
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
    // NOVÉ: Uchovávame zoznam vybraných kategórií, nie len jednu
    var tutorialCategories by mutableStateOf<List<String>>(emptyList())
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
                snapshotFlow { tutorialCategories } // SLEDUJEME NOVÝ STAV
            ) { blocks, title, categoriesList -> // PREMENOVANÉ
                val currentTutorial = originalTutorial?.copy(
                    title = title,
                    categories = categoriesList, // POUŽÍVAME NOVÝ PARAMETER
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
                    // ZMENA: Kontrolujeme, či zoznam kategórií tutoriálu obsahuje vybranú kategóriu
                    tutorials.filter { it.categories.contains(category) }
                }

                // NOVÉ: Filter z dialógu (Checkboxy) - aplikuje sa na výsledok z TabRow
                val tutorialsByDialogFilter = if (activeFilters.isEmpty()) {
                    tutorialsByCategory
                } else {
                    // Ak je záložka "Všetky", filtrujeme z nej. Ak je iná, filtrujeme z nej.
                    // ZMENA: Kontrolujeme, či existuje PRIENIK medzi kategóriami tutoriálu a aktívnymi filtrami
                    tutorialsByCategory.filter { tutorial -> tutorial.categories.any { it in activeFilters } }
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
                // ZMENA: "category" na "categories" a hodnota je zoznam
                categories = listOf("Prihlásenie", "Softvér"),
                content = listOf(
                    TutorialContentBlock.TextBlock(text = "Pre prihlásenie do systému použite vaše pridelené meno a heslo."),
                    TutorialContentBlock.ImageBlock(imageRes = R.drawable.ic_launcher_background),
                    TutorialContentBlock.TextBlock(text = "V prípade zabudnutého hesla kontaktujte administrátora.")
                )
            ),
            Tutorial(
                title = "Čistenie hardvéru počítača",
                // ZMENA: "category" na "categories" a hodnota je zoznam
                categories = listOf("Hardvér"),
                content = listOf(
                    TutorialContentBlock.TextBlock(text = "Pravidelne čistite ventilátory a chladiče od prachu, aby ste predišli prehrievaniu komponentov.")
                )
            )
        )
        _tutorials.value = initialData
    }

    // --- FUNKCIE PRE Ukladanie a Pridávanie obrázkov (bez zmeny) ---
    suspend fun saveTutorial(tutorial: Tutorial) {
        _tutorials.update { list ->
            // Ak návod s daným ID už existuje, nahradíme ho. Inak ho pridáme.
            val existingIndex = list.indexOfFirst { it.id == tutorial.id }
            if (existingIndex != -1) {
                // Návod existuje, aktualizujeme ho v zozname
                list.toMutableList().apply { this[existingIndex] = tutorial }
            } else {
                // Návod neexistuje, pridáme ho na koniec zoznamu
                list + tutorial
            }
        }
    }

    suspend fun saveImageToInternalStorageAndGetUri(context: Context, sourceUri: Uri): Uri? {
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

    /**
     * Odstráni jeden konkrétny filter kategórie.
     * Volá sa po kliknutí na krížik na čipe filtra.
     */
    fun removeFilterCategory(category: String) {
        _activeCategoryFilters.update { currentFilters ->
            currentFilters - category
        }
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

    fun onCategorySelectionChange(newCategories: List<String>) {
        tutorialCategories = newCategories
    }

    fun addTextBlock(): Int {
        val newBlock = TutorialContentBlock.TextBlock(text = "")
        _contentBlocks.update { it + newBlock }
        // Vrátime index poslednej (práve pridanej) položky
        return _contentBlocks.value.lastIndex
    }

    // ✅ TÚTO FUNKCIU VLOŽTE SEM
    fun getTutorialById(tutorialId: String): Tutorial? {
        return _tutorials.value.firstOrNull { it.id == tutorialId }
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
                    tutorialCategories = tutorial.categories
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
        tutorialCategories = emptyList() // ZMENA: Resetujeme na prázdny zoznam
        _contentBlocks.value = emptyList()

        // ZMENA: Vytvárame "original" s prázdnym zoznamom kategórií
        originalTutorial = Tutorial(id = "new", title = "", categories = emptyList(), content = emptyList())
        hasChanges = false
    }
    fun addCategory(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isNotBlank() && managedCategories.none { it.equals(trimmedName, ignoreCase = true) }) {
            managedCategories.add(trimmedName)
        }
    }

    /**
     * Odstráni existujúcu kategóriu.
     */
    fun removeCategory(name: String) {
        // Odstránime kategóriu zo zoznamu spravovaných kategórií
        managedCategories.remove(name)

        // Prejdeme všetky tutoriály a odstránime z nich túto kategóriu
        _tutorials.update { currentTutorials ->
            currentTutorials.map { tutorial ->
                if (tutorial.categories.contains(name)) {
                    tutorial.copy(categories = tutorial.categories - name)
                } else {
                    tutorial
                }
            }
        }
    }

    /**
     * Aktualizuje názov existujúcej kategórie.
     */
    fun updateCategory(oldName: String, newName: String) {
        val trimmedNewName = newName.trim()
        // Overíme, či je nový názov platný a či už neexistuje
        if (trimmedNewName.isBlank() || managedCategories.any { it.equals(trimmedNewName, ignoreCase = true) }) {
            return // Neplatná operácia, neurobíme nič
        }

        // Nájdeme index starej kategórie a nahradíme ju
        val index = managedCategories.indexOf(oldName)
        if (index != -1) {
            managedCategories[index] = trimmedNewName
        }

        // Prejdeme všetky tutoriály a aktualizujeme v nich názov kategórie
        _tutorials.update { currentTutorials ->
            currentTutorials.map { tutorial ->
                if (tutorial.categories.contains(oldName)) {
                    // Odstránime starý názov a pridáme nový
                    val updatedCategories = tutorial.categories - oldName + trimmedNewName
                    tutorial.copy(categories = updatedCategories)
                } else {
                    tutorial
                }
            }
        }
    }
    fun createTempImageFile(context: Context): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir("temp_images")
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        val file = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        // Dôležité: Získanie URI cez FileProvider
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Musí sa zhodovať s autoritou v manifeste
            file
        )
    }

}
