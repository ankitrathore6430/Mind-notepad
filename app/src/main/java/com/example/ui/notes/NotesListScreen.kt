package com.example.ui.notes

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BannerAdView
import com.example.data.local.Note
import com.example.data.preferences.ThemeMode
import com.example.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
  notes: List<Note>,
  categories: List<String>,
  selectedCategory: String,
  searchQuery: String,
  isGridView: Boolean,
  themeMode: ThemeMode,
  onCategorySelect: (String) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onToggleViewMode: () -> Unit,
  onThemeModeChange: (ThemeMode) -> Unit,
  onNoteClick: (Note) -> Unit,
  onAddNoteClick: () -> Unit,
  onTogglePin: (Note) -> Unit,
  onDuplicateNote: (Note) -> Unit,
  onDeleteNote: (Note) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showThemeDialog by remember { mutableStateOf(false) }
  var noteToDelete by remember { mutableStateOf<Note?>(null) }
  var isSearchActive by remember { mutableStateOf(false) }

  if (showThemeDialog) {
    ThemeSelectionDialog(
      currentMode = themeMode,
      onModeSelected = onThemeModeChange,
      onDismiss = { showThemeDialog = false }
    )
  }

  // Delete Confirmation Dialog
  noteToDelete?.let { note ->
    AlertDialog(
      onDismissRequest = { noteToDelete = null },
      title = { Text("Delete Note") },
      text = {
        Text("Are you sure you want to delete \"${if (note.title.isNotBlank()) note.title else "Untitled Note"}\"?")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteNote(note)
            noteToDelete = null
          },
          modifier = Modifier.testTag("confirm_delete_note_button")
        ) {
          Text("Delete", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { noteToDelete = null },
          modifier = Modifier.testTag("cancel_delete_note_button")
        ) {
          Text("Cancel")
        }
      }
    )
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.PushPin,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Mind Notepad",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          // Search Toggle Button
          IconButton(
            onClick = { isSearchActive = !isSearchActive },
            modifier = Modifier.testTag("search_toggle_button")
          ) {
            Icon(
              imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
              contentDescription = "Search Notes",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // View Mode Toggle Button (Grid vs List)
          IconButton(
            onClick = onToggleViewMode,
            modifier = Modifier.testTag("view_mode_toggle_button")
          ) {
            Icon(
              imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
              contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Theme Mode Selector Button
          IconButton(
            onClick = { showThemeDialog = true },
            modifier = Modifier.testTag("theme_selector_button")
          ) {
            val icon = when (themeMode) {
              ThemeMode.LIGHT -> Icons.Default.LightMode
              ThemeMode.DARK -> Icons.Default.DarkMode
              ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
            }
            Icon(
              imageVector = icon,
              contentDescription = "Select Theme Mode",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onAddNoteClick,
        modifier = Modifier.testTag("add_note_fab"),
        icon = {
          Icon(Icons.Default.Add, contentDescription = null)
        },
        text = {
          Text("New Note", fontWeight = FontWeight.SemiBold)
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(18.dp)
      )
    },
    bottomBar = {
      BannerAdView()
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Search Bar (Animated reveal or shown when query is not empty)
      AnimatedVisibility(
        visible = isSearchActive || searchQuery.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("search_text_field"),
          placeholder = { Text("Search in title, content, or tag...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
          )
        )
      }

      // Categories Horizontal Bar
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(categories) { category ->
          val isSelected = selectedCategory.equals(category, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { onCategorySelect(category) },
            label = {
              Text(
                text = category,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        }
      }

      // Notes Content Area
      if (notes.isEmpty()) {
        EmptyNotesState(
          isSearching = searchQuery.isNotEmpty(),
          onAddNote = onAddNoteClick
        )
      } else {
        val isDark = when (themeMode) {
          ThemeMode.LIGHT -> false
          ThemeMode.DARK -> true
          ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

        val columns = if (isGridView) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1)

        LazyVerticalStaggeredGrid(
          columns = columns,
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalItemSpacing = 12.dp,
          modifier = Modifier
            .fillMaxSize()
            .testTag("notes_staggered_grid")
        ) {
          items(notes, key = { it.id }) { note ->
            NoteCardItem(
              note = note,
              isDark = isDark,
              onClick = { onNoteClick(note) },
              onTogglePin = { onTogglePin(note) },
              onDuplicate = { onDuplicateNote(note) },
              onDelete = { noteToDelete = note },
              onShare = { shareNote(context, note) }
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCardItem(
  note: Note,
  isDark: Boolean,
  onClick: () -> Unit,
  onTogglePin: () -> Unit,
  onDuplicate: () -> Unit,
  onDelete: () -> Unit,
  onShare: () -> Unit,
  modifier: Modifier = Modifier
) {
  val palette = NoteColors.get(note.colorIndex)
  val cardBg = if (isDark) palette.darkBg else palette.lightBg
  val cardBorder = if (isDark) palette.darkBorder else palette.lightBorder
  var showMenu by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(18.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = { showMenu = true }
      )
      .testTag("note_card_${note.id}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.dp, cardBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Top row: Title / Pin
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        if (note.title.isNotBlank()) {
          Text(
            text = note.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
        } else {
          Text(
            text = "Untitled",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.weight(1f)
          )
        }

        // Pin Button
        IconButton(
          onClick = onTogglePin,
          modifier = Modifier
            .size(28.dp)
            .testTag("note_pin_button_${note.id}")
        ) {
          Icon(
            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Content Preview
      if (note.content.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = cleanMarkdownForPreview(note.content),
          style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 20.sp
          ),
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
          maxLines = 6,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Footer Row: Category chip, Date, and Menu
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Category tag
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text(
              text = note.category,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          // Formatted Time
          Text(
            text = formatTimestamp(note.updatedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
          )
        }

        // More options dropdown
        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Options",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Share Note") },
              leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
              onClick = {
                showMenu = false
                onShare()
              }
            )
            DropdownMenuItem(
              text = { Text("Duplicate") },
              leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
              onClick = {
                showMenu = false
                onDuplicate()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
              leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun EmptyNotesState(
  isSearching: Boolean,
  onAddNote: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.size(80.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (isSearching) Icons.Default.Search else Icons.Default.PushPin,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = if (isSearching) "No matching notes found" else "Your Mind is clear",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = if (isSearching) "Try searching for another keyword or check categories."
               else "Tap the button below to capture your thoughts, ideas, or to-do lists.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )

      if (!isSearching) {
        Spacer(modifier = Modifier.height(18.dp))
        TextButton(
          onClick = onAddNote,
          modifier = Modifier.testTag("empty_state_add_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Write first note")
        }
      }
    }
  }
}

fun formatTimestamp(millis: Long): String {
  val date = Date(millis)
  val now = System.currentTimeMillis()
  val diff = now - millis

  return when {
    diff < 60_000L -> "Just now"
    diff < 3600_000L -> "${diff / 60_000L}m ago"
    diff < 86400_000L -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
  }
}

fun shareNote(context: Context, note: Note) {
  val cleanContent = RichNoteContent.fromString(note.content).plainText
  val shareText = buildString {
    if (note.title.isNotBlank()) {
      append(note.title)
      append("\n\n")
    }
    append(cleanContent)
  }
  val intent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Note from Mind Notepad" })
    putExtra(Intent.EXTRA_TEXT, shareText)
  }
  context.startActivity(Intent.createChooser(intent, "Share Note"))
}

fun cleanMarkdownForPreview(text: String): String {
  return RichNoteContent.fromString(text).plainText.trim()
}
