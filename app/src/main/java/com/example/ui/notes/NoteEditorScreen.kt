package com.example.ui.notes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.ThemeMode
import com.example.ui.theme.NoteColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
  title: String,
  content: String,
  category: String,
  colorIndex: Int,
  isPinned: Boolean,
  lastEditedTime: Long,
  themeMode: ThemeMode,
  categories: List<String>,
  onTitleChange: (String) -> Unit,
  onContentChange: (String) -> Unit,
  onCategoryChange: (String) -> Unit,
  onColorChange: (Int) -> Unit,
  onTogglePin: () -> Unit,
  onBack: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showColorPicker by remember { mutableStateOf(false) }
  var showCustomCategoryDialog by remember { mutableStateOf(false) }
  var customCategoryInput by remember { mutableStateOf("") }

  // Formatting toolbar state (compact and non-intrusive)
  var isFormattingToolbarVisible by remember { mutableStateOf(false) }

  // Rich Content Model initialized from database content (HTML or legacy markdown)
  var richContent by remember { mutableStateOf(RichNoteContent.fromString(content)) }

  // TextFieldValue with plainText (NEVER contains ** or * symbols!)
  var contentTextFieldValue by remember {
    mutableStateOf(TextFieldValue(text = richContent.plainText, selection = TextRange(richContent.plainText.length)))
  }

  // BackHandler to auto-save and close
  BackHandler {
    onContentChange(richContent.toHtml())
    onBack()
  }

  val isDark = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val palette = NoteColors.get(colorIndex)
  val editorBg = if (isDark) palette.darkBg else palette.lightBg

  // Delete Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Note") },
      text = { Text("Are you sure you want to discard this note?") },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteDialog = false
            onDelete()
          },
          modifier = Modifier.testTag("confirm_delete_editor_button")
        ) {
          Text("Delete", color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Custom Category Dialog
  if (showCustomCategoryDialog) {
    AlertDialog(
      onDismissRequest = { showCustomCategoryDialog = false },
      title = { Text("Add Category") },
      text = {
        OutlinedTextField(
          value = customCategoryInput,
          onValueChange = { customCategoryInput = it },
          label = { Text("Category name") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_category_input")
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            val trimmed = customCategoryInput.trim()
            if (trimmed.isNotEmpty()) {
              onCategoryChange(trimmed)
            }
            customCategoryInput = ""
            showCustomCategoryDialog = false
          },
          modifier = Modifier.testTag("save_custom_category_button")
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCustomCategoryDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Active styles at cursor/selection
  val currentCursor = contentTextFieldValue.selection.start.coerceIn(0, richContent.plainText.length)
  val currentLineIdx = if (richContent.plainText.isEmpty()) 0 else richContent.plainText.substring(0, currentCursor).count { it == '\n' }
  val currentLineType = richContent.lineStyles[currentLineIdx] ?: LineType.NORMAL
  val activeStyles = richContent.spans.filter { span ->
    if (!contentTextFieldValue.selection.collapsed) {
      span.start <= contentTextFieldValue.selection.min && span.end >= contentTextFieldValue.selection.max
    } else {
      span.start <= currentCursor && span.end >= currentCursor
    }
  }.map { it.type }.toSet()

  // Word & character counter
  val fullText = "$title ${richContent.plainText}".trim()
  val wordCount = if (fullText.isEmpty()) 0 else fullText.split("\\s+".toRegex()).size
  val charCount = title.length + richContent.plainText.length

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = editorBg,
    topBar = {
      TopAppBar(
        title = {},
        navigationIcon = {
          IconButton(
            onClick = {
              onContentChange(richContent.toHtml())
              onBack()
            },
            modifier = Modifier.testTag("editor_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Save and go back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        },
        actions = {
          // Text Style Formatting Toolbar Toggle ("Aa" / Format size)
          IconButton(
            onClick = { isFormattingToolbarVisible = !isFormattingToolbarVisible },
            modifier = Modifier.testTag("editor_format_toggle_button")
          ) {
            Icon(
              imageVector = Icons.Default.FormatSize,
              contentDescription = "Text styles (Bold, Italic, Headings)",
              tint = if (isFormattingToolbarVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }

          // Color Palette Toggle
          IconButton(
            onClick = { showColorPicker = !showColorPicker },
            modifier = Modifier.testTag("editor_palette_button")
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = "Color Theme",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Pin Toggle
          IconButton(
            onClick = onTogglePin,
            modifier = Modifier.testTag("editor_pin_button")
          ) {
            Icon(
              imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
              contentDescription = if (isPinned) "Unpin" else "Pin",
              tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }

          // Share Note
          IconButton(
            onClick = {
              val shareText = buildString {
                if (title.isNotBlank()) {
                  append(title)
                  append("\n\n")
                }
                append(richContent.plainText)
              }
              val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { "Mind Notepad Note" })
                putExtra(Intent.EXTRA_TEXT, shareText)
              }
              context.startActivity(Intent.createChooser(intent, "Share Note"))
            },
            modifier = Modifier.testTag("editor_share_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }

          // Delete Note
          IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.testTag("editor_delete_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.error
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = editorBg
        )
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .imePadding()
      ) {
        // Formatting Toolbar (Sleek, unobtrusive, toggles with Aa button)
        AnimatedVisibility(
          visible = isFormattingToolbarVisible,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          FormattingToolbar(
            currentLineType = currentLineType,
            activeStyles = activeStyles,
            onH1Click = {
              richContent = RichTextEditorManager.toggleLineStyle(richContent, contentTextFieldValue.selection.start, LineType.H1)
              onContentChange(richContent.toHtml())
            },
            onH2Click = {
              richContent = RichTextEditorManager.toggleLineStyle(richContent, contentTextFieldValue.selection.start, LineType.H2)
              onContentChange(richContent.toHtml())
            },
            onBoldClick = {
              richContent = RichTextEditorManager.toggleInlineStyle(richContent, contentTextFieldValue.selection, StyleType.BOLD)
              onContentChange(richContent.toHtml())
            },
            onItalicClick = {
              richContent = RichTextEditorManager.toggleInlineStyle(richContent, contentTextFieldValue.selection, StyleType.ITALIC)
              onContentChange(richContent.toHtml())
            },
            onStrikeClick = {
              richContent = RichTextEditorManager.toggleInlineStyle(richContent, contentTextFieldValue.selection, StyleType.STRIKE)
              onContentChange(richContent.toHtml())
            },
            onBulletClick = {
              val res = RichTextEditorManager.toggleBullet(richContent, contentTextFieldValue)
              richContent = res.first
              contentTextFieldValue = res.second
              onContentChange(richContent.toHtml())
            },
            onChecklistClick = {
              val res = RichTextEditorManager.toggleChecklist(richContent, contentTextFieldValue)
              richContent = res.first
              contentTextFieldValue = res.second
              onContentChange(richContent.toHtml())
            },
            onQuoteClick = {
              richContent = RichTextEditorManager.toggleLineStyle(richContent, contentTextFieldValue.selection.start, LineType.QUOTE)
              onContentChange(richContent.toHtml())
            },
            onCodeClick = {
              richContent = RichTextEditorManager.toggleInlineStyle(richContent, contentTextFieldValue.selection, StyleType.CODE)
              onContentChange(richContent.toHtml())
            },
            onClose = { isFormattingToolbarVisible = false }
          )
        }

        // Bottom Status Bar
        Surface(
          color = editorBg,
          tonalElevation = 1.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Edited ${formatEditorTimestamp(lastEditedTime)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$wordCount ${if (wordCount == 1) "word" else "words"} | $charCount chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
              )

              Spacer(modifier = Modifier.width(8.dp))

              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Mind Notepad Note", "$title\n\n${richContent.plainText}".trim())
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy text",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Color Picker Palette Bar (Reveals on tap)
      AnimatedVisibility(visible = showColorPicker) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            items(NoteColors.palettes) { colorDef ->
              val isSelected = colorIndex == colorDef.id
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(colorDef.dotColor)
                  .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                  )
                  .clickable { onColorChange(colorDef.id) }
                  .testTag("color_picker_${colorDef.id}"),
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Category Selector Chips Row
      val availableCats = categories.filter { it != "All" && it != "Pinned" }
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(availableCats) { cat ->
          val isSelected = category.equals(cat, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { onCategoryChange(cat) },
            label = { Text(cat, fontSize = 13.sp) },
            shape = RoundedCornerShape(16.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
          )
        }

        // Custom Category Button
        item {
          FilterChip(
            selected = false,
            onClick = { showCustomCategoryDialog = true },
            label = { Text("+ Category", fontSize = 13.sp) },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("add_custom_category_chip")
          )
        }
      }

      // Title Input
      TextField(
        value = title,
        onValueChange = onTitleChange,
        placeholder = {
          Text(
            text = "Title",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
          )
        },
        textStyle = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = false,
        maxLines = 3,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp)
          .testTag("note_title_input"),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent
        )
      )

      // Rich Text Editor - Direct WYSIWYG without markdown symbols
      val visualTransformation = remember(isDark, richContent) {
        RichVisualTransformation(
          contentProvider = { richContent },
          primaryColor = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA),
          onSurfaceColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
          codeBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        )
      }

      TextField(
        value = contentTextFieldValue,
        onValueChange = { newTfv ->
          val oldText = contentTextFieldValue.text
          val newText = newTfv.text
          val delta = newText.length - oldText.length
          val editIndex = newTfv.selection.min

          richContent = richContent.adjustForEdit(newText, editIndex, delta)
          contentTextFieldValue = newTfv
          onContentChange(richContent.toHtml())
        },
        visualTransformation = visualTransformation,
        placeholder = {
          Text(
            text = "Type your thoughts, notes, checklist, or ideas here...\n\nTap 'Aa' at the top to format headings, bold, italic, & checklists without any tags!",
            style = MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
              lineHeight = 24.sp
            )
          )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 24.sp
        ),
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 6.dp)
          .testTag("note_content_input"),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent
        )
      )
    }
  }
}

fun formatEditorTimestamp(millis: Long): String {
  val date = Date(millis)
  return SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(date)
}
