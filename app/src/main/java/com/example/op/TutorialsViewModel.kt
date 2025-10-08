package com.example.op

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    var content: List<TutorialContentBlock>
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

    init {
        loadInitialTutorials()
    }

    private fun loadInitialTutorials() {
        val initialData = listOf(
            Tutorial(
                title = "Ako sa prihlásiť do systému",
                category = "Prihlásenie",
                content = listOf(
                    TutorialContentBlock.TextBlock(text = "Pre prihlásenie do systému použite vaše pridelené meno a heslo."),
                    // ✅ ZMENA: Používame URI string pre placeholder
                    TutorialContentBlock.ImageBlock(uriString = "android.resource://com.example.op/${R.drawable.image_placeholder}"),
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
        filterTutorials()
    }

    // --- FUNKCIE PRE PRIDÁVANIE OBRÁZKOV (NOVÉ A UPRAVENÉ) ---

    /**
     * Skopíruje obrázok z galérie (sourceUri) do interného úložiska aplikácie.
     * Vráti nové, stabilné URI, ktoré je bezpečné dlhodobo používať.
     */
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

    /**
     * ✅ NOVÁ FUNKCIA: Hlavná funkcia, ktorú volá UI po výbere obrázku z galérie.
     */
    fun addImageBlockFromUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            val stableUri = saveImageToInternalStorageAndGetUri(context, uri)
            if (stableUri != null) {
                val newBlock = TutorialContentBlock.ImageBlock(uriString = stableUri.toString())
                _contentBlocks.update { it + newBlock }
            } else {
                // V prípade chyby môžeme informovať používateľa
                Log.e("TutorialsViewModel", "Nepodarilo sa spracovať obrázok z URI: $uri")
            }
        }
    }


    // --- OSTATNÉ FUNKCIE ---

    fun onCategorySelected(category: String) {
        selectedCategory = category
        filterTutorials()
    }

    private fun filterTutorials() {
        _filteredTutorials.value = if (selectedCategory == "Všetky") {
            _tutorials.value
        } else {
            _tutorials.value.filter { it.category == selectedCategory }
        }
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

    // ❌ STARÁ FUNKCIA addImageBlock() JE ODSTRÁNENÁ

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

    fun saveTutorial() {
        if (tutorialTitle.isBlank()) return

        viewModelScope.launch {
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
            filterTutorials()
            resetForm()
        }
    }

    fun loadTutorialForEditing(tutorialId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val tutorial = _tutorials.value.firstOrNull { it.id == tutorialId }
                if (tutorial != null) {
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
            filterTutorials()
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
    }
}
