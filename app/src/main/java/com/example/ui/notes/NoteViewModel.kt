package com.example.ui.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MindDatabase
import com.example.data.local.Note
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.ThemePreferences
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
  application: Application,
  private val repository: NoteRepository,
  private val themePrefs: ThemePreferences
) : AndroidViewModel(application) {

  init {
    viewModelScope.launch {
      val firstList = repository.getAllNotes().first()
      if (firstList.isEmpty()) {
        val note1 = Note(
          title = "Welcome to Mind Notepad! 💡",
          content = "# Rich Text & Formatted Notes\nMind Notepad supports **bold**, *italic*, ~~strikethrough~~, and `inline code`.\n\n## Quick Checklist\n- [x] Create first thought\n- [ ] Try formatting tools by tapping 'Aa' icon\n- [ ] Switch between Light and Dark theme\n\n> \"Ideas are the currency of the mind.\"",
          category = "Ideas",
          colorIndex = 1,
          isPinned = true
        )
        val note2 = Note(
          title = "Quick Tips & Shortcuts 📝",
          content = "• Tap '+ New Note' to create your thoughts\n• Tap the 'Aa' button in editor to format with H1, H2, Bold, Italic, & Checklists\n• Tap the Eye icon for clean formatted preview\n• Notes are saved automatically as you write\n• Long-press or tap 3 dots on any note card to duplicate, share, or delete",
          category = "General",
          colorIndex = 3,
          isPinned = false
        )
        repository.insert(note1)
        repository.insert(note2)
      }
    }
  }

  // Theme
  val themeMode: StateFlow<ThemeMode> = themePrefs.themeMode

  fun setThemeMode(mode: ThemeMode) {
    themePrefs.setThemeMode(mode)
  }

  // Layout View Mode (Grid / List)
  private val _isGridView = MutableStateFlow(true)
  val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

  fun toggleViewMode() {
    _isGridView.value = !_isGridView.value
  }

  // Search
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  // Category Filter
  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

  fun selectCategory(category: String) {
    _selectedCategory.value = category
  }

  // All notes flow
  private val _allNotes = repository.getAllNotes()

  // Filtered Notes
  val notesList: StateFlow<List<Note>> = combine(
    _allNotes,
    _searchQuery,
    _selectedCategory
  ) { notes, query, category ->
    var result = notes
    val trimmed = query.trim()
    if (trimmed.isNotEmpty()) {
      result = result.filter {
        it.title.contains(trimmed, ignoreCase = true) ||
          it.content.contains(trimmed, ignoreCase = true) ||
          it.category.contains(trimmed, ignoreCase = true)
      }
    }
    if (category == "Pinned") {
      result = result.filter { it.isPinned }
    } else if (category != "All") {
      result = result.filter { it.category.equals(category, ignoreCase = true) }
    }
    result
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Categories available
  val categories: StateFlow<List<String>> = _allNotes.combine(_selectedCategory) { notes, _ ->
    val defaultCats = listOf("All", "Pinned", "General", "Ideas", "Personal", "Work", "Study")
    val userCats = notes.map { it.category }.filter { it.isNotBlank() && it !in defaultCats }.distinct()
    defaultCats + userCats
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = listOf("All", "Pinned", "General", "Ideas", "Personal", "Work", "Study")
  )

  // Quick operations on notes
  fun togglePin(note: Note) {
    viewModelScope.launch {
      repository.togglePin(note.id, note.isPinned)
    }
  }

  fun deleteNote(note: Note) {
    viewModelScope.launch {
      repository.delete(note)
    }
  }

  fun duplicateNote(note: Note) {
    viewModelScope.launch {
      val duplicate = note.copy(
        id = 0,
        title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Copy",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      repository.insert(duplicate)
    }
  }

  // --- Editor State ---
  // null = list screen, -1 = new note, >0 = editing existing note
  private val _editingNoteId = MutableStateFlow<Long?>(null)
  val editingNoteId: StateFlow<Long?> = _editingNoteId.asStateFlow()

  val draftTitle = MutableStateFlow("")
  val draftContent = MutableStateFlow("")
  val draftCategory = MutableStateFlow("General")
  val draftColorIndex = MutableStateFlow(0)
  val draftIsPinned = MutableStateFlow(false)
  val lastEditedTime = MutableStateFlow(System.currentTimeMillis())

  fun openNewNote() {
    _editingNoteId.value = -1L
    draftTitle.value = ""
    draftContent.value = ""
    draftCategory.value = if (_selectedCategory.value != "All" && _selectedCategory.value != "Pinned") {
      _selectedCategory.value
    } else {
      "General"
    }
    draftColorIndex.value = 0
    draftIsPinned.value = false
    lastEditedTime.value = System.currentTimeMillis()
  }

  fun openExistingNote(note: Note) {
    _editingNoteId.value = note.id
    draftTitle.value = note.title
    draftContent.value = note.content
    draftCategory.value = note.category.ifBlank { "General" }
    draftColorIndex.value = note.colorIndex
    draftIsPinned.value = note.isPinned
    lastEditedTime.value = note.updatedAt
  }

  fun closeEditor() {
    // Auto-save before closing if non-empty
    saveCurrentDraft()
    _editingNoteId.value = null
  }

  fun saveCurrentDraft() {
    val currentId = _editingNoteId.value ?: return
    val title = draftTitle.value.trim()
    val content = draftContent.value.trim()

    // If completely empty and is new note, do nothing
    if (title.isEmpty() && content.isEmpty()) {
      if (currentId > 0) {
        // user deleted all text from existing note, keep it or remove
        viewModelScope.launch {
          repository.deleteById(currentId)
        }
      }
      return
    }

    viewModelScope.launch {
      val now = System.currentTimeMillis()
      if (currentId == -1L) {
        // New note
        val newNote = Note(
          id = 0,
          title = title,
          content = content,
          category = draftCategory.value.ifBlank { "General" },
          colorIndex = draftColorIndex.value,
          isPinned = draftIsPinned.value,
          createdAt = now,
          updatedAt = now
        )
        val createdId = repository.insert(newNote)
        _editingNoteId.value = createdId
      } else {
        // Update existing note
        val existing = repository.getNoteByIdOnce(currentId)
        val updated = Note(
          id = currentId,
          title = title,
          content = content,
          category = draftCategory.value.ifBlank { "General" },
          colorIndex = draftColorIndex.value,
          isPinned = draftIsPinned.value,
          createdAt = existing?.createdAt ?: now,
          updatedAt = now
        )
        repository.update(updated)
      }
      lastEditedTime.value = now
    }
  }

  fun deleteCurrentDraft() {
    val currentId = _editingNoteId.value
    if (currentId != null && currentId > 0) {
      viewModelScope.launch {
        repository.deleteById(currentId)
      }
    }
    _editingNoteId.value = null
  }

  // Factory
  class Factory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      val database = MindDatabase.getDatabase(application)
      val repository = NoteRepository(database.noteDao())
      val themePrefs = ThemePreferences(application)
      return NoteViewModel(application, repository, themePrefs) as T
    }
  }
}
