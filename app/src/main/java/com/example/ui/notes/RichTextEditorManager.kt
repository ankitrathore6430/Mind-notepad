package com.example.ui.notes

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

object RichTextEditorManager {

  /**
   * Toggles bold, italic, strikethrough, or code on selected text, or word at cursor.
   */
  fun toggleInlineStyle(
    content: RichNoteContent,
    selection: TextRange,
    styleType: StyleType
  ): RichNoteContent {
    val text = content.plainText
    if (text.isEmpty()) return content

    val range = if (!selection.collapsed) {
      TextRange(selection.min.coerceIn(0, text.length), selection.max.coerceIn(0, text.length))
    } else {
      // Find current word around cursor
      findWordRangeAt(text, selection.start)
    }

    if (range.collapsed) return content

    val start = range.min
    val end = range.max

    // Check if this range is already fully styled with styleType
    val overlapping = content.spans.filter { it.type == styleType && it.start < end && it.end > start }
    val isFullyCovered = overlapping.any { it.start <= start && it.end >= end }

    val remainingSpans = content.spans.filterNot { it.type == styleType && it.start < end && it.end > start }.toMutableList()

    if (isFullyCovered) {
      // Remove style by splitting existing span
      val original = overlapping.first { it.start <= start && it.end >= end }
      if (original.start < start) {
        remainingSpans.add(StyleSpan(styleType, original.start, start))
      }
      if (original.end > end) {
        remainingSpans.add(StyleSpan(styleType, end, original.end))
      }
    } else {
      // Add new style span, merging adjacent or overlapping spans of same type
      var newStart = start
      var newEnd = end
      overlapping.forEach {
        newStart = minOf(newStart, it.start)
        newEnd = maxOf(newEnd, it.end)
      }
      remainingSpans.add(StyleSpan(styleType, newStart, newEnd))
    }

    return content.copy(spans = remainingSpans.sortedBy { it.start })
  }

  /**
   * Toggles H1, H2, or Quote on the current line.
   */
  fun toggleLineStyle(
    content: RichNoteContent,
    cursor: Int,
    targetLineType: LineType
  ): RichNoteContent {
    val text = content.plainText
    val lineIdx = getLineIndexAt(text, cursor)
    val currentLineType = content.lineStyles[lineIdx] ?: LineType.NORMAL

    val updatedLineStyles = content.lineStyles.toMutableMap()
    if (currentLineType == targetLineType) {
      updatedLineStyles.remove(lineIdx)
    } else {
      updatedLineStyles[lineIdx] = targetLineType
    }

    return content.copy(lineStyles = updatedLineStyles)
  }

  /**
   * Toggles bullet list item "• " on current line.
   */
  fun toggleBullet(
    content: RichNoteContent,
    tfv: TextFieldValue
  ): Pair<RichNoteContent, TextFieldValue> {
    val text = content.plainText
    val cursor = tfv.selection.start.coerceIn(0, text.length)
    val lineStart = getLineStart(text, cursor)
    val lineRemaining = text.substring(lineStart)

    return when {
      lineRemaining.startsWith("• ") -> {
        // Remove bullet
        val newText = text.substring(0, lineStart) + lineRemaining.substring(2)
        val adjustedContent = content.adjustForEdit(newText, lineStart, -2)
        val newCursor = (cursor - 2).coerceAtLeast(lineStart)
        Pair(adjustedContent, tfv.copy(text = newText, selection = TextRange(newCursor)))
      }
      lineRemaining.startsWith("☐ ") || lineRemaining.startsWith("☑ ") -> {
        // Replace checkbox with bullet
        val newText = text.substring(0, lineStart) + "• " + lineRemaining.substring(2)
        val adjustedContent = content.copy(plainText = newText)
        Pair(adjustedContent, tfv.copy(text = newText))
      }
      else -> {
        // Insert bullet
        val newText = text.substring(0, lineStart) + "• " + lineRemaining
        val adjustedContent = content.adjustForEdit(newText, lineStart, 2)
        val newCursor = cursor + 2
        Pair(adjustedContent, tfv.copy(text = newText, selection = TextRange(newCursor)))
      }
    }
  }

  /**
   * Toggles checklist item "☐ " / "☑ " on current line.
   */
  fun toggleChecklist(
    content: RichNoteContent,
    tfv: TextFieldValue
  ): Pair<RichNoteContent, TextFieldValue> {
    val text = content.plainText
    val cursor = tfv.selection.start.coerceIn(0, text.length)
    val lineStart = getLineStart(text, cursor)
    val lineRemaining = text.substring(lineStart)

    return when {
      lineRemaining.startsWith("☐ ") -> {
        // Toggle unchecked to checked
        val newText = text.substring(0, lineStart) + "☑ " + lineRemaining.substring(2)
        val adjustedContent = content.copy(plainText = newText)
        Pair(adjustedContent, tfv.copy(text = newText))
      }
      lineRemaining.startsWith("☑ ") -> {
        // Remove checkbox
        val newText = text.substring(0, lineStart) + lineRemaining.substring(2)
        val adjustedContent = content.adjustForEdit(newText, lineStart, -2)
        val newCursor = (cursor - 2).coerceAtLeast(lineStart)
        Pair(adjustedContent, tfv.copy(text = newText, selection = TextRange(newCursor)))
      }
      lineRemaining.startsWith("• ") -> {
        // Replace bullet with checkbox
        val newText = text.substring(0, lineStart) + "☐ " + lineRemaining.substring(2)
        val adjustedContent = content.copy(plainText = newText)
        Pair(adjustedContent, tfv.copy(text = newText))
      }
      else -> {
        // Insert checkbox
        val newText = text.substring(0, lineStart) + "☐ " + lineRemaining
        val adjustedContent = content.adjustForEdit(newText, lineStart, 2)
        val newCursor = cursor + 2
        Pair(adjustedContent, tfv.copy(text = newText, selection = TextRange(newCursor)))
      }
    }
  }

  private fun getLineStart(text: String, cursor: Int): Int {
    if (text.isEmpty() || cursor <= 0) return 0
    val idx = text.lastIndexOf('\n', cursor - 1)
    return if (idx == -1) 0 else idx + 1
  }

  private fun getLineIndexAt(text: String, cursor: Int): Int {
    if (text.isEmpty() || cursor <= 0) return 0
    return text.substring(0, cursor.coerceIn(0, text.length)).count { it == '\n' }
  }

  private fun findWordRangeAt(text: String, cursor: Int): TextRange {
    if (text.isEmpty()) return TextRange.Zero
    val c = cursor.coerceIn(0, text.length)

    var start = (c - 1).coerceAtLeast(0)
    while (start >= 0 && text[start].isLetterOrDigit()) {
      start--
    }
    start++

    var end = c
    while (end < text.length && text[end].isLetterOrDigit()) {
      end++
    }

    return if (start < end) TextRange(start, end) else TextRange(c, c)
  }
}
