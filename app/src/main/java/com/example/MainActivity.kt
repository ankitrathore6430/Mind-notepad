package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.InterstitialAdManager
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.notes.NoteViewModel
import com.example.ui.notes.NotesListScreen
import com.example.ui.theme.MindNotepadTheme

class MainActivity : ComponentActivity() {

  private val viewModel: NoteViewModel by viewModels {
    NoteViewModel.Factory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize AdMob MobileAds SDK & Preload Interstitial Ad
    InterstitialAdManager.init(applicationContext)

    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

      MindNotepadTheme(themeMode = themeMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MindNotepadApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun MindNotepadApp(viewModel: NoteViewModel) {
  val context = LocalContext.current
  val editingNoteId by viewModel.editingNoteId.collectAsStateWithLifecycle()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

  AnimatedContent(
    targetState = editingNoteId,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "ScreenTransition"
  ) { currentId ->
    if (currentId == null) {
      // List Screen
      val notes by viewModel.notesList.collectAsStateWithLifecycle()
      val categories by viewModel.categories.collectAsStateWithLifecycle()
      val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
      val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
      val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()

      NotesListScreen(
        notes = notes,
        categories = categories,
        selectedCategory = selectedCategory,
        searchQuery = searchQuery,
        isGridView = isGridView,
        themeMode = themeMode,
        onCategorySelect = viewModel::selectCategory,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleViewMode = viewModel::toggleViewMode,
        onThemeModeChange = viewModel::setThemeMode,
        onNoteClick = viewModel::openExistingNote,
        onAddNoteClick = viewModel::openNewNote,
        onTogglePin = viewModel::togglePin,
        onDuplicateNote = viewModel::duplicateNote,
        onDeleteNote = viewModel::deleteNote
      )
    } else {
      // Editor Screen
      val draftTitle by viewModel.draftTitle.collectAsStateWithLifecycle()
      val draftContent by viewModel.draftContent.collectAsStateWithLifecycle()
      val draftCategory by viewModel.draftCategory.collectAsStateWithLifecycle()
      val draftColorIndex by viewModel.draftColorIndex.collectAsStateWithLifecycle()
      val draftIsPinned by viewModel.draftIsPinned.collectAsStateWithLifecycle()
      val lastEditedTime by viewModel.lastEditedTime.collectAsStateWithLifecycle()
      val categories by viewModel.categories.collectAsStateWithLifecycle()

      NoteEditorScreen(
        title = draftTitle,
        content = draftContent,
        category = draftCategory,
        colorIndex = draftColorIndex,
        isPinned = draftIsPinned,
        lastEditedTime = lastEditedTime,
        themeMode = themeMode,
        categories = categories,
        onTitleChange = {
          viewModel.draftTitle.value = it
          viewModel.saveCurrentDraft()
        },
        onContentChange = {
          viewModel.draftContent.value = it
          viewModel.saveCurrentDraft()
        },
        onCategoryChange = {
          viewModel.draftCategory.value = it
          viewModel.saveCurrentDraft()
        },
        onColorChange = {
          viewModel.draftColorIndex.value = it
          viewModel.saveCurrentDraft()
        },
        onTogglePin = {
          viewModel.draftIsPinned.value = !viewModel.draftIsPinned.value
          viewModel.saveCurrentDraft()
        },
        onBack = {
          viewModel.closeEditor()
          (context as? Activity)?.let { act ->
            InterstitialAdManager.onNoteSaved(act)
          }
        },
        onDelete = viewModel::deleteCurrentDraft
      )
    }
  }
}
