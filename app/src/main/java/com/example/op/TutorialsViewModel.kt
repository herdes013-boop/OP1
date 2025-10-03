package com.example.op

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.util.UUID

class TutorialsViewModel : ViewModel() {

    // --- Zdroje dát a filtre ---
    private val _tutorialList = MutableStateFlow<List<TutorialItem>>(emptyList())
    // ... (ostatné premenné ostávajú rovnaké)
    private val _selectedTab = MutableStateFlow("Všetky")
    val selectedTab = _selectedTab.asStateFlow()
    val categories = listOf("Všetky", "Pre začiatočníkov", "Pre pokročilých")

    val displayedTutorials = combine(_tutorialList, _selectedTab) { tutorials, selectedCategory ->
        if (selectedCategory == "Všetky") {
            tutorials
        } else {
            tutorials.filter { it.category == selectedCategory }
        }
    }

    // --- Stavy pre formulár ---
    var tutorialTitle by mutableStateOf("")
        private set
    var tutorialCategory by mutableStateOf(categories[1])
        private set
    private val _contentBlocks = MutableStateFlow<List<TutorialContentBlock>>(emptyList())
    val contentBlocks = _contentBlocks.asStateFlow()
    var editingTutorialId by mutableStateOf<String?>(null)
        private set

    // === KROK 1.1: Premenná pre pôvodný stav ===
    private var originalTutorialState: TutorialItem? = null

    init {
        loadInitialTutorials()
    }

    // --- Funkcie pre prácu s formulárom ---

    // ... (všetky funkcie onTitleChange, onCategoryChange, addTextBlock, atď. ostávajú bez zmeny)

    /**
     * Vyčistí formulár a resetuje ID upravovaného návodu.
     */
    fun resetForm() {
        tutorialTitle = ""
        tutorialCategory = categories[1]
        _contentBlocks.value = emptyList()
        editingTutorialId = null
        // === KROK 1.2: Vyčistíme aj pôvodný stav ===
        originalTutorialState = null
    }

    /**
     * Načíta dáta existujúceho návodu do formulára a nastaví jeho ID.
     */
    fun loadTutorialForEditing(tutorialId: String) {
        val tutorial = _tutorialList.value.find { it.id == tutorialId }
        if (tutorial != null) {
            tutorialTitle = tutorial.title
            tutorialCategory = tutorial.category
            _contentBlocks.value = tutorial.contentBlocks
            editingTutorialId = tutorial.id
            // === KROK 1.3: Uložíme si kópiu pôvodného stavu ===
            originalTutorialState = tutorial.copy()
        }
    }

    // === KROK 1.4: Nová funkcia na detekciu zmien ===
    /**
     * Vráti `true`, ak sa aktuálny stav formulára líši od pôvodného.
     */
    fun hasUnsavedChanges(): Boolean {
        // Ak neupravujeme, nemôžu byť neuložené zmeny
        if (originalTutorialState == null) return false

        // Porovnáme každý atribút
        val titleChanged = tutorialTitle != originalTutorialState?.title
        val categoryChanged = tutorialCategory != originalTutorialState?.category
        val contentChanged = _contentBlocks.value != originalTutorialState?.contentBlocks

        return titleChanged || categoryChanged || contentChanged
    }


    // --- CRUD Operácie ---
    // ... (saveTutorial, addTutorial, updateTutorial, deleteTutorial ostávajú bez zmeny)
    // ...

    // --- Všetky ostatné funkcie z vášho ViewModelu sem patria ---
    fun selectCategory(category: String) { _selectedTab.value = category }
    fun onTitleChange(newTitle: String) { tutorialTitle = newTitle }
    fun onCategoryChange(newCategory: String) { tutorialCategory = newCategory }
    fun addTextBlock() { _contentBlocks.update { it + TutorialContentBlock.TextBlock() } }
    fun addImageBlock() { _contentBlocks.update { it + TutorialContentBlock.ImageBlock() } }
    fun removeBlock(blockId: String) { _contentBlocks.update { it.filterNot { b -> b.id == blockId } } }
    fun onTextBlockChange(blockId: String, newText: String) {
        _contentBlocks.update { blocks ->
            blocks.map { if (it.id == blockId && it is TutorialContentBlock.TextBlock) it.copy(text = newText) else it }
        }
    }
    fun onImageBlockChange(blockId: String, @DrawableRes newImageRes: Int) {
        _contentBlocks.update { blocks ->
            blocks.map { if (it.id == blockId && it is TutorialContentBlock.ImageBlock) it.copy(imageRes = newImageRes) else it }
        }
    }
    private fun addTutorial() {
        val newTutorial = TutorialItem(title = tutorialTitle, category = tutorialCategory, contentBlocks = _contentBlocks.value)
        _tutorialList.update { it + newTutorial }
    }
    private fun updateTutorial() {
        val idToUpdate = editingTutorialId ?: return
        _tutorialList.update { list ->
            list.map {
                if (it.id == idToUpdate) it.copy(title = tutorialTitle, category = tutorialCategory, contentBlocks = _contentBlocks.value) else it
            }
        }
    }
    fun saveTutorial() { if (editingTutorialId == null) addTutorial() else updateTutorial() }
    fun deleteTutorial(tutorialId: String) { _tutorialList.update { it.filterNot { t -> t.id == tutorialId } } }

    private fun loadInitialTutorials() {
        _tutorialList.value = listOf(
            TutorialItem(id = UUID.randomUUID().toString(), title = "Ako pridať nové heslo", category = "Pre začiatočníkov", contentBlocks = listOf(TutorialContentBlock.TextBlock("1. Prejdite do sekcie 'Heslá'."), TutorialContentBlock.TextBlock("2. Kliknite na tlačidlo '+' vpravo dole."), TutorialContentBlock.TextBlock("3. Vyplňte všetky polia a kliknite na 'Uložiť'."))),
            TutorialItem(id = UUID.randomUUID().toString(), title = "Kopírovanie hesla do schránky", category = "Pre začiatočníkov", contentBlocks = listOf(TutorialContentBlock.TextBlock("V zozname hesiel jednoducho kliknite na ikonu kópie vedľa položky, ktorú chcete skopírovať."), TutorialContentBlock.ImageBlock(R.drawable.ic_launcher_foreground), TutorialContentBlock.TextBlock("Heslo sa bezpečne uloží do schránky."))),
            TutorialItem(id = UUID.randomUUID().toString(), title = "Pokročilá správa kontaktov", category = "Pre pokročilých", contentBlocks = listOf(TutorialContentBlock.TextBlock("V sekcii 'Kontakty' môžete spravovať aj kanály cez príslušné tlačidlo.")))
        )
    }
}
