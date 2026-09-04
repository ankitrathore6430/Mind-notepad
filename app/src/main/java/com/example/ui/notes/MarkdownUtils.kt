package com.example.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

object MarkdownUtils {

  /**
   * Wraps selected text with prefix and suffix (e.g. **bold**, *italic*, ~~strike~~, `code`),
   * or inserts them with cursor in between if no text is selected.
   */
  fun applyWrapFormatting(
    current: TextFieldValue,
    prefix: String,
    suffix: String = prefix,
    placeholder: String = ""
  ): TextFieldValue {
    val text = current.text
    val selection = current.selection

    return if (selection.collapsed) {
      val insertText = if (placeholder.isNotEmpty()) "$prefix$placeholder$suffix" else "$prefix$suffix"
      val newText = text.substring(0, selection.start) + insertText + text.substring(selection.start)
      val newCursor = if (placeholder.isNotEmpty()) {
        selection.start + prefix.length + placeholder.length
      } else {
        selection.start + prefix.length
      }
      TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
      )
    } else {
      val min = selection.min
      val max = selection.max
      val selected = text.substring(min, max)
      val newText = text.substring(0, min) + prefix + selected + suffix + text.substring(max)
      TextFieldValue(
        text = newText,
        selection = TextRange(min + prefix.length, max + prefix.length)
      )
    }
  }

  /**
   * Toggles line prefix (e.g. "# ", "## ", "- ", "- [ ] ", "> ") at the beginning of the current line.
   */
  fun toggleLinePrefix(
    current: TextFieldValue,
    prefix: String
  ): TextFieldValue {
    val text = current.text
    val selection = current.selection
    val cursor = selection.start

    // Find beginning of current line
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let {
      if (it == -1) 0 else it + 1
    }

    val lineRemaining = text.substring(lineStart)
    return if (lineRemaining.startsWith(prefix)) {
      // Remove prefix (toggle off)
      val newText = text.substring(0, lineStart) + lineRemaining.substring(prefix.length)
      val newCursor = (cursor - prefix.length).coerceAtLeast(lineStart)
      TextFieldValue(text = newText, selection = TextRange(newCursor))
    } else {
      // Check if line starts with any other heading/list prefix and replace it if so
      val prefixesToReplace = listOf("## ", "# ", "- [ ] ", "- [x] ", "• ", "- ", "> ")
      val existingPrefix = prefixesToReplace.firstOrNull { lineRemaining.startsWith(it) }

      if (existingPrefix != null) {
        val newText = text.substring(0, lineStart) + prefix + lineRemaining.substring(existingPrefix.length)
        val shift = prefix.length - existingPrefix.length
        val newCursor = (cursor + shift).coerceIn(0, newText.length)
        TextFieldValue(text = newText, selection = TextRange(newCursor))
      } else {
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        val newCursor = cursor + prefix.length
        TextFieldValue(text = newText, selection = TextRange(newCursor))
      }
    }
  }

  /**
   * Converts markdown text into a clean AnnotatedString for read/preview mode.
   */
  fun renderToAnnotatedString(
    markdown: String,
    primaryColor: Color,
    onSurfaceColor: Color,
    codeBgColor: Color
  ): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = markdown.lines()

    lines.forEachIndexed { index, line ->
      when {
        line.startsWith("# ") -> {
          val headingText = line.removePrefix("# ").trim()
          val start = builder.length
          builder.append(headingText)
          builder.addStyle(
            SpanStyle(
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = primaryColor
            ),
            start,
            builder.length
          )
        }
        line.startsWith("## ") -> {
          val headingText = line.removePrefix("## ").trim()
          val start = builder.length
          builder.append(headingText)
          builder.addStyle(
            SpanStyle(
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = primaryColor
            ),
            start,
            builder.length
          )
        }
        line.startsWith("- [ ] ") -> {
          val itemText = "☐  " + line.removePrefix("- [ ] ")
          val start = builder.length
          builder.append(itemText)
          builder.addStyle(
            SpanStyle(
              fontWeight = FontWeight.Medium,
              color = onSurfaceColor
            ),
            start,
            builder.length
          )
        }
        line.startsWith("- [x] ") || line.startsWith("- [X] ") -> {
          val itemText = "☑  " + line.substring(6)
          val start = builder.length
          builder.append(itemText)
          builder.addStyle(
            SpanStyle(
              textDecoration = TextDecoration.LineThrough,
              color = onSurfaceColor.copy(alpha = 0.6f)
            ),
            start,
            builder.length
          )
        }
        line.startsWith("> ") -> {
          val quoteText = "“ " + line.removePrefix("> ")
          val start = builder.length
          builder.append(quoteText)
          builder.addStyle(
            SpanStyle(
              fontStyle = FontStyle.Italic,
              color = primaryColor
            ),
            start,
            builder.length
          )
        }
        line.startsWith("- ") || line.startsWith("• ") -> {
          val bulletText = "•  " + line.replaceFirst(Regex("^[-•]\\s+"), "")
          builder.append(bulletText)
        }
        else -> {
          // Normal line, parse inline styles
          appendFormattedInline(builder, line, onSurfaceColor, codeBgColor)
        }
      }

      if (index < lines.size - 1) {
        builder.append("\n")
      }
    }

    return builder.toAnnotatedString()
  }

  private fun appendFormattedInline(
    builder: AnnotatedString.Builder,
    line: String,
    onSurfaceColor: Color,
    codeBgColor: Color
  ) {
    // Simple inline parser for bold **text**, italic *text*, code `text`, and strikethrough ~~text~~
    var idx = 0
    val len = line.length

    while (idx < len) {
      if (idx + 1 < len && line[idx] == '*' && line[idx + 1] == '*') {
        val nextEnd = line.indexOf("**", idx + 2)
        if (nextEnd != -1) {
          val content = line.substring(idx + 2, nextEnd)
          val start = builder.length
          builder.append(content)
          builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor), start, builder.length)
          idx = nextEnd + 2
          continue
        }
      }

      if (idx + 1 < len && line[idx] == '~' && line[idx + 1] == '~') {
        val nextEnd = line.indexOf("~~", idx + 2)
        if (nextEnd != -1) {
          val content = line.substring(idx + 2, nextEnd)
          val start = builder.length
          builder.append(content)
          builder.addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = onSurfaceColor.copy(alpha = 0.6f)), start, builder.length)
          idx = nextEnd + 2
          continue
        }
      }

      if (line[idx] == '`') {
        val nextEnd = line.indexOf('`', idx + 1)
        if (nextEnd != -1) {
          val content = line.substring(idx + 1, nextEnd)
          val start = builder.length
          builder.append(content)
          builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBgColor), start, builder.length)
          idx = nextEnd + 1
          continue
        }
      }

      if (line[idx] == '*' && (idx + 1 >= len || line[idx + 1] != '*')) {
        val nextEnd = line.indexOf('*', idx + 1)
        if (nextEnd != -1) {
          val content = line.substring(idx + 1, nextEnd)
          val start = builder.length
          builder.append(content)
          builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, builder.length)
          idx = nextEnd + 1
          continue
        }
      }

      builder.append(line[idx].toString())
      idx++
    }
  }
}
