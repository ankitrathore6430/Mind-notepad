package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.Note
import com.example.data.preferences.ThemeMode
import com.example.ui.notes.NotesListScreen
import com.example.ui.theme.MindNotepadTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleNotes = listOf(
      Note(
        id = 1,
        title = "Mind Notepad Launch 💡",
        content = "Distraction-free thoughts and to-dos with Light and Dark themes.",
        category = "Ideas",
        colorIndex = 1,
        isPinned = true
      ),
      Note(
        id = 2,
        title = "Project Goals",
        content = "Deliver full notepad functionality with seamless theme switching.",
        category = "Work",
        colorIndex = 3,
        isPinned = false
      )
    )

    composeTestRule.setContent {
      MindNotepadTheme(themeMode = ThemeMode.LIGHT) {
        NotesListScreen(
          notes = sampleNotes,
          categories = listOf("All", "Pinned", "General", "Ideas", "Work"),
          selectedCategory = "All",
          searchQuery = "",
          isGridView = true,
          themeMode = ThemeMode.LIGHT,
          onCategorySelect = {},
          onSearchQueryChange = {},
          onToggleViewMode = {},
          onThemeModeChange = {},
          onNoteClick = {},
          onAddNoteClick = {},
          onTogglePin = {},
          onDuplicateNote = {},
          onDeleteNote = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
