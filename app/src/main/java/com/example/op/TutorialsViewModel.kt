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

/**
 * ViewModel pre správu a logiku týkajúcu sa návodov.
 */
class TutorialsViewModel : ViewModel() {

    // --- Zdroje dát a filtre (Táto časť ostáva podobná) ---

    private val _tutorialList = MutableStateFlow<List<TutorialItem>>(emptyList())
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

    // --- ZMENA 1: NOVÉ STAVY PRE DYNAMICKÝ FORMULÁR ---

    // Stav pre názov návodu
    var tutorialTitle by mutableStateOf("")
        private set

    // Stav pre vybranú kategóriu návodu
    var tutorialCategory by mutableStateOf(categories[1]) // Predvolená kategória
        private set

    // STAV, KTORÝ DRŽÍ ZOZNAM BLOKOV PRE NOVÝ/UPRAVOVANÝ NÁVOD
    private val _contentBlocks = MutableStateFlow<List<TutorialContentBlock>>(emptyList())
    val contentBlocks = _contentBlocks.asStateFlow()


    init {
        loadInitialTutorials()
    }

    // --- ZMENA 2: AKTUALIZÁCIA TESTOVACÍCH DÁT ---
    private fun loadInitialTutorials() {
        _tutorialList.value = listOf(
            TutorialItem(
                title = "Ako pridať nové heslo",
                category = "Pre začiatočníkov",
                contentBlocks = listOf(
                    TutorialContentBlock.TextBlock("1. Prejdite do sekcie 'Heslá'."),
                    TutorialContentBlock.TextBlock("2. Kliknite na tlačidlo '+' vpravo dole."),
                    TutorialContentBlock.TextBlock("3. Vyplňte všetky polia a kliknite na 'Uložiť'.")
                )
            ),
            TutorialItem(
                title = "Kopírovanie hesla do schránky",
                category = "Pre začiatočníkov",
                contentBlocks = listOf(
                    TutorialContentBlock.TextBlock("V zozname hesiel jednoducho kliknite na ikonu kópie vedľa položky, ktorú chcete skopírovať."),
                    TutorialContentBlock.ImageBlock(R.drawable.ic_launcher_foreground),
                    TutorialContentBlock.TextBlock("Heslo sa bezpečne uloží do schránky.")
                )
            ),
            TutorialItem(
                title = "Pokročilá správa kontaktov",
                category = "Pre pokročilých",
                contentBlocks = listOf(
                    TutorialContentBlock.TextBlock("V sekcii 'Kontakty' môžete spravovať aj kanály cez príslušné tlačidlo.")
                )
            )
        )
    }

    // --- ZMENA 3: NOVÉ FUNKCIE PRE PRÁCU S FORMULÁROM A BLOKMI ---

    fun selectCategory(category: String) {
        _selectedTab.value = category
    }

    fun onTitleChange(newTitle: String) {
        tutorialTitle = newTitle
    }

    fun onCategoryChange(newCategory: String) {
        tutorialCategory = newCategory
    }

    // Pridá nový prázdny textový blok na koniec zoznamu
    fun addTextBlock() {
        _contentBlocks.update { currentBlocks ->
            currentBlocks + TutorialContentBlock.TextBlock()
        }
    }

    // Pridá nový prázdny obrázkový blok na koniec zoznamu
    fun addImageBlock() {
        _contentBlocks.update { currentBlocks ->
            currentBlocks + TutorialContentBlock.ImageBlock()
        }
    }

    // Odstráni blok zo zoznamu podľa jeho ID
    fun removeBlock(blockId: String) {
        _contentBlocks.update { currentBlocks ->
            currentBlocks.filterNot { it.id == blockId }
        }
    }

    // Aktualizuje text v konkrétnom textovom bloku
    fun onTextBlockChange(blockId: String, newText: String) {
        _contentBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId && block is TutorialContentBlock.TextBlock) {
                    block.copy(text = newText)
                } else {
                    block
                }
            }
        }
    }

    // Aktualizuje obrázok v konkrétnom obrázkovom bloku
    fun onImageBlockChange(blockId: String, @DrawableRes newImageRes: Int) {
        _contentBlocks.update { currentBlocks ->
            currentBlocks.map { block ->
                if (block.id == blockId && block is TutorialContentBlock.ImageBlock) {
                    block.copy(imageRes = newImageRes)
                } else {
                    block
                }
            }
        }
    }

    /**
     * Vyčistí celý formulár (názov, kategóriu aj všetky bloky).
     * Volá sa pred otvorením obrazovky pre pridanie návodu.
     */
    fun resetForm() {
        tutorialTitle = ""
        tutorialCategory = categories[1] // Reset na predvolenú
        _contentBlocks.value = emptyList()
    }

    // TODO: Funkciu `loadTutorialForEditing` upravíme neskôr, keď budeme riešiť úpravu.
    fun loadTutorialForEditing(tutorialId: String) {
        val tutorial = _tutorialList.value.find { it.id == tutorialId }
        if (tutorial != null) {
            tutorialTitle = tutorial.title
            tutorialCategory = tutorial.category
            _contentBlocks.value = tutorial.contentBlocks
        }
    }

    // --- ZMENA 4: AKTUALIZÁCIA CRUD OPERÁCIÍ ---

    /**
     * Pridá nový návod do zoznamu na základe aktuálnych dát z formulára.
     */
    fun addTutorial() {
        val newTutorial = TutorialItem(
            title = tutorialTitle,
            category = tutorialCategory,
            contentBlocks = _contentBlocks.value
        )
        _tutorialList.update { currentList ->
            currentList + newTutorial
        }
    }

    /**
     * Upraví existujúci návod v zozname.
     */
    fun updateTutorial(tutorialId: String) {
        _tutorialList.update { currentList ->
            currentList.map {
                if (it.id == tutorialId) {
                    it.copy(
                        title = tutorialTitle,
                        category = tutorialCategory,
                        contentBlocks = _contentBlocks.value
                    )
                } else {
                    it
                }
            }
        }
    }

    fun deleteTutorial(tutorialId: String) {
        _tutorialList.update { currentList ->
            currentList.filterNot { it.id == tutorialId }
        }
    }
}
