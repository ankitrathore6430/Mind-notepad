package com.example.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

enum class StyleType {
  BOLD,
  ITALIC,
  STRIKE,
  CODE
}

enum class LineType {
  NORMAL,
  H1,
  H2,
  QUOTE
}

data class StyleSpan(
  val type: StyleType,
  val start: Int,
  val end: Int
)

data class RichNoteContent(
  val plainText: String,
  val spans: List<StyleSpan>,
  val lineStyles: Map<Int, LineType>
) {
  /**
   * Adjusts spans when plain text changes by [delta] at [editIndex].
   */
  fun adjustForEdit(newText: String, editIndex: Int, delta: Int): RichNoteContent {
    if (delta == 0 && newText.length == plainText.length) {
      return copy(plainText = newText)
    }

    val updatedSpans = spans.mapNotNull { span ->
      when {
        // Edit happened after this span: span is unaffected
        editIndex >= span.end -> {
          if (span.end <= newText.length) span else span.copy(end = newText.length)
        }
        // Edit happened before this span: shift entire span
        editIndex <= span.start -> {
          val newStart = (span.start + delta).coerceIn(0, newText.length)
          val newEnd = (span.end + delta).coerceIn(newStart, newText.length)
          if (newStart < newEnd) span.copy(start = newStart, end = newEnd) else null
        }
        // Edit happened inside this span: expand or shrink span end
        else -> {
          val newEnd = (span.end + delta).coerceIn(span.start, newText.length)
          if (span.start < newEnd) span.copy(end = newEnd) else null
        }
      }
    }.filter { it.start < it.end && it.end <= newText.length }

    // Re-index line styles based on newline counts
    val newLineCount = newText.count { it == '\n' } + 1
    val updatedLineStyles = lineStyles.filterKeys { it < newLineCount }

    return RichNoteContent(
      plainText = newText,
      spans = updatedSpans,
      lineStyles = updatedLineStyles
    )
  }

  /**
   * Creates an AnnotatedString with all styles applied directly without any symbols.
   */
  fun toAnnotatedString(
    primaryColor: Color,
    onSurfaceColor: Color,
    codeBgColor: Color
  ): AnnotatedString {
    val builder = AnnotatedString.Builder(plainText)
    val len = plainText.length

    // 1. Apply Line Styles (H1, H2, QUOTE)
    var currentLine = 0
    var lineStart = 0
    while (lineStart <= len) {
      val nextNewline = plainText.indexOf('\n', lineStart).let { if (it == -1) len else it }
      val lineType = lineStyles[currentLine] ?: LineType.NORMAL

      if (lineStart < nextNewline) {
        when (lineType) {
          LineType.H1 -> {
            builder.addStyle(
              SpanStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
              ),
              lineStart,
              nextNewline
            )
          }
          LineType.H2 -> {
            builder.addStyle(
              SpanStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
              ),
              lineStart,
              nextNewline
            )
          }
          LineType.QUOTE -> {
            builder.addStyle(
              SpanStyle(
                fontStyle = FontStyle.Italic,
                color = primaryColor
              ),
              lineStart,
              nextNewline
            )
          }
          LineType.NORMAL -> {
            // If line starts with checkbox checked ☑, add strikethrough on whole line
            val lineSub = plainText.substring(lineStart, nextNewline)
            if (lineSub.startsWith("☑")) {
              builder.addStyle(
                SpanStyle(
                  textDecoration = TextDecoration.LineThrough,
                  color = onSurfaceColor.copy(alpha = 0.6f)
                ),
                lineStart,
                nextNewline
              )
            }
          }
        }
      }

      currentLine++
      if (nextNewline == len) break
      lineStart = nextNewline + 1
    }

    // 2. Apply Inline Spans (Bold, Italic, Strikethrough, Code)
    spans.forEach { span ->
      val s = span.start.coerceIn(0, len)
      val e = span.end.coerceIn(s, len)
      if (s < e) {
        val style = when (span.type) {
          StyleType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)
          StyleType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
          StyleType.STRIKE -> SpanStyle(textDecoration = TextDecoration.LineThrough, color = onSurfaceColor.copy(alpha = 0.6f))
          StyleType.CODE -> SpanStyle(fontFamily = FontFamily.Monospace, background = codeBgColor, color = primaryColor)
        }
        builder.addStyle(style, s, e)
      }
    }

    return builder.toAnnotatedString()
  }

  /**
   * Serializes to clean HTML for persistent Room storage.
   */
  fun toHtml(): String {
    val lines = plainText.split('\n')
    val result = StringBuilder()

    var charOffset = 0
    lines.forEachIndexed { lineIdx, lineText ->
      val lineEnd = charOffset + lineText.length
      val lineType = lineStyles[lineIdx] ?: LineType.NORMAL

      // Find spans that fall within this line
      val lineSpans = spans.filter { it.start < lineEnd && it.end > charOffset }
        .map { span ->
          val relStart = (span.start - charOffset).coerceIn(0, lineText.length)
          val relEnd = (span.end - charOffset).coerceIn(0, lineText.length)
          span.copy(start = relStart, end = relEnd)
        }
        .filter { it.start < it.end }

      val formattedLine = buildInlineHtml(lineText, lineSpans)

      val wrappedLine = when (lineType) {
        LineType.H1 -> "<h1>$formattedLine</h1>"
        LineType.H2 -> "<h2>$formattedLine</h2>"
        LineType.QUOTE -> "<blockquote>$formattedLine</blockquote>"
        LineType.NORMAL -> "<p>$formattedLine</p>"
      }

      result.append(wrappedLine)
      if (lineIdx < lines.size - 1) {
        result.append("\n")
      }
      charOffset = lineEnd + 1 // +1 for '\n'
    }

    return result.toString()
  }

  companion object {
    private fun buildInlineHtml(text: String, spans: List<StyleSpan>): String {
      if (spans.isEmpty()) return escapeHtml(text)

      // Mark openings and closings at indices
      val opens = mutableMapOf<Int, MutableList<String>>()
      val closes = mutableMapOf<Int, MutableList<String>>()

      for (span in spans) {
        val tag = when (span.type) {
          StyleType.BOLD -> "b"
          StyleType.ITALIC -> "i"
          StyleType.STRIKE -> "s"
          StyleType.CODE -> "code"
        }
        opens.getOrPut(span.start) { mutableListOf() }.add("<$tag>")
        closes.getOrPut(span.end) { mutableListOf() }.add("</$tag>")
      }

      val sb = StringBuilder()
      for (i in 0..text.length) {
        closes[i]?.forEach { sb.append(it) }
        opens[i]?.forEach { sb.append(it) }
        if (i < text.length) {
          val ch = text[i]
          when (ch) {
            '&' -> sb.append("&amp;")
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            else -> sb.append(ch)
          }
        }
      }
      return sb.toString()
    }

    private fun escapeHtml(text: String): String {
      return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
    }

    /**
     * Parses HTML or legacy markdown string into RichNoteContent.
     */
    fun fromString(rawContent: String): RichNoteContent {
      if (rawContent.isBlank()) {
        return RichNoteContent("", emptyList(), emptyMap())
      }

      // Check if it's HTML format
      if (rawContent.contains("<h1") || rawContent.contains("<h2") ||
          rawContent.contains("<p") || rawContent.contains("<blockquote") ||
          rawContent.contains("<b>") || rawContent.contains("<i>")) {
        return parseHtml(rawContent)
      }

      // Otherwise parse legacy markdown or plain text
      return parseLegacyMarkdown(rawContent)
    }

    private fun parseHtml(html: String): RichNoteContent {
      val plainSb = StringBuilder()
      val spans = mutableListOf<StyleSpan>()
      val lineStyles = mutableMapOf<Int, LineType>()

      val rawLines = html.split('\n')
      rawLines.forEachIndexed { lineIdx, rawLine ->
        var lineType = LineType.NORMAL
        var inner = rawLine.trim()

        when {
          inner.startsWith("<h1>") && inner.endsWith("</h1>") -> {
            lineType = LineType.H1
            inner = inner.removeSurrounding("<h1>", "</h1>")
          }
          inner.startsWith("<h2>") && inner.endsWith("</h2>") -> {
            lineType = LineType.H2
            inner = inner.removeSurrounding("<h2>", "</h2>")
          }
          inner.startsWith("<blockquote>") && inner.endsWith("</blockquote>") -> {
            lineType = LineType.QUOTE
            inner = inner.removeSurrounding("<blockquote>", "</blockquote>")
          }
          inner.startsWith("<p>") && inner.endsWith("</p>") -> {
            lineType = LineType.NORMAL
            inner = inner.removeSurrounding("<p>", "</p>")
          }
        }

        if (lineType != LineType.NORMAL) {
          lineStyles[lineIdx] = lineType
        }

        // Parse inline tags <b>, <i>, <s>, <code>
        val lineStartOffset = plainSb.length
        val tagRegex = Regex("</?(b|i|s|code)>", RegexOption.IGNORE_CASE)
        var lastIdx = 0
        val activeSpans = mutableMapOf<String, Int>()

        tagRegex.findAll(inner).forEach { match ->
          // Append text before tag
          val textChunk = unescapeHtml(inner.substring(lastIdx, match.range.first))
          plainSb.append(textChunk)

          val tag = match.value.lowercase()
          val tagName = match.groupValues[1].lowercase()

          if (tag.startsWith("</")) {
            val startPos = activeSpans.remove(tagName)
            if (startPos != null && plainSb.length > startPos) {
              val type = when (tagName) {
                "b" -> StyleType.BOLD
                "i" -> StyleType.ITALIC
                "s" -> StyleType.STRIKE
                "code" -> StyleType.CODE
                else -> StyleType.BOLD
              }
              spans.add(StyleSpan(type, startPos, plainSb.length))
            }
          } else {
            activeSpans[tagName] = plainSb.length
          }

          lastIdx = match.range.last + 1
        }

        // Append remaining text after last tag
        if (lastIdx < inner.length) {
          plainSb.append(unescapeHtml(inner.substring(lastIdx)))
        }

        // Close any unclosed tags at end of line
        activeSpans.forEach { (tagName, startPos) ->
          if (plainSb.length > startPos) {
            val type = when (tagName) {
              "b" -> StyleType.BOLD
              "i" -> StyleType.ITALIC
              "s" -> StyleType.STRIKE
              "code" -> StyleType.CODE
              else -> StyleType.BOLD
            }
            spans.add(StyleSpan(type, startPos, plainSb.length))
          }
        }

        if (lineIdx < rawLines.size - 1) {
          plainSb.append('\n')
        }
      }

      return RichNoteContent(plainSb.toString(), spans, lineStyles)
    }

    private fun parseLegacyMarkdown(markdown: String): RichNoteContent {
      val plainSb = StringBuilder()
      val spans = mutableListOf<StyleSpan>()
      val lineStyles = mutableMapOf<Int, LineType>()

      val lines = markdown.split('\n')
      lines.forEachIndexed { lineIdx, line ->
        var cleanLine = line
        var lineType = LineType.NORMAL

        when {
          cleanLine.startsWith("# ") -> {
            lineType = LineType.H1
            cleanLine = cleanLine.removePrefix("# ").trimStart()
          }
          cleanLine.startsWith("## ") -> {
            lineType = LineType.H2
            cleanLine = cleanLine.removePrefix("## ").trimStart()
          }
          cleanLine.startsWith("> ") -> {
            lineType = LineType.QUOTE
            cleanLine = cleanLine.removePrefix("> ").trimStart()
          }
          cleanLine.startsWith("- [ ] ") -> {
            cleanLine = "☐ " + cleanLine.removePrefix("- [ ] ")
          }
          cleanLine.startsWith("- [x] ") || cleanLine.startsWith("- [X] ") -> {
            cleanLine = "☑ " + cleanLine.substring(6)
          }
          cleanLine.startsWith("- ") -> {
            cleanLine = "• " + cleanLine.removePrefix("- ")
          }
        }

        if (lineType != LineType.NORMAL) {
          lineStyles[lineIdx] = lineType
        }

        // Remove markdown symbols and record spans
        val lineStart = plainSb.length
        val parsed = parseInlineMarkdownToSpans(cleanLine, lineStart)
        plainSb.append(parsed.first)
        spans.addAll(parsed.second)

        if (lineIdx < lines.size - 1) {
          plainSb.append('\n')
        }
      }

      return RichNoteContent(plainSb.toString(), spans, lineStyles)
    }

    private fun parseInlineMarkdownToSpans(line: String, baseOffset: Int): Pair<String, List<StyleSpan>> {
      val sb = StringBuilder()
      val spans = mutableListOf<StyleSpan>()
      var i = 0
      val len = line.length

      while (i < len) {
        // Bold: **text**
        if (i + 1 < len && line[i] == '*' && line[i + 1] == '*') {
          val endIdx = line.indexOf("**", i + 2)
          if (endIdx != -1) {
            val content = line.substring(i + 2, endIdx)
            val startPos = baseOffset + sb.length
            sb.append(content)
            spans.add(StyleSpan(StyleType.BOLD, startPos, baseOffset + sb.length))
            i = endIdx + 2
            continue
          }
        }

        // Strikethrough: ~~text~~
        if (i + 1 < len && line[i] == '~' && line[i + 1] == '~') {
          val endIdx = line.indexOf("~~", i + 2)
          if (endIdx != -1) {
            val content = line.substring(i + 2, endIdx)
            val startPos = baseOffset + sb.length
            sb.append(content)
            spans.add(StyleSpan(StyleType.STRIKE, startPos, baseOffset + sb.length))
            i = endIdx + 2
            continue
          }
        }

        // Code: `text`
        if (line[i] == '`') {
          val endIdx = line.indexOf('`', i + 1)
          if (endIdx != -1) {
            val content = line.substring(i + 1, endIdx)
            val startPos = baseOffset + sb.length
            sb.append(content)
            spans.add(StyleSpan(StyleType.CODE, startPos, baseOffset + sb.length))
            i = endIdx + 1
            continue
          }
        }

        // Italic: *text*
        if (line[i] == '*' && (i + 1 >= len || line[i + 1] != '*')) {
          val endIdx = line.indexOf('*', i + 1)
          if (endIdx != -1) {
            val content = line.substring(i + 1, endIdx)
            val startPos = baseOffset + sb.length
            sb.append(content)
            spans.add(StyleSpan(StyleType.ITALIC, startPos, baseOffset + sb.length))
            i = endIdx + 1
            continue
          }
        }

        sb.append(line[i])
        i++
      }

      return Pair(sb.toString(), spans)
    }

    private fun unescapeHtml(text: String): String {
      return text
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
    }
  }
}
