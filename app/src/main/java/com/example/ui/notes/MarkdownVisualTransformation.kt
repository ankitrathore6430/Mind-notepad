package com.example.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class MarkdownVisualTransformation(
  private val primaryColor: Color,
  private val onSurfaceColor: Color,
  private val codeBgColor: Color
) : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    val rawText = text.text
    val builder = AnnotatedString.Builder(text)

    // Heading 1 (# Heading)
    val h1Regex = Regex("(?m)^#\\s+(.*)$")
    h1Regex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = primaryColor
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Heading 2 (## Heading)
    val h2Regex = Regex("(?m)^##\\s+(.*)$")
    h2Regex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = primaryColor
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Bold (**bold**)
    val boldRegex = Regex("\\*\\*([^*\\n]+)\\*\\*")
    boldRegex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          fontWeight = FontWeight.Bold,
          color = onSurfaceColor
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Italic (*italic* or _italic_)
    val italicRegex = Regex("(?<!\\*)\\*([^*\\n]+)\\*(?!\\*)")
    italicRegex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          fontStyle = FontStyle.Italic
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Strikethrough (~~text~~)
    val strikeRegex = Regex("~~([^~\\n]+)~~")
    strikeRegex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          textDecoration = TextDecoration.LineThrough,
          color = onSurfaceColor.copy(alpha = 0.6f)
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Inline code (`code`)
    val codeRegex = Regex("`([^`\\n]+)`")
    codeRegex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          fontFamily = FontFamily.Monospace,
          background = codeBgColor,
          color = primaryColor
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Bullet and Checklist lines (- [ ], - [x], •)
    val checklistRegex = Regex("(?m)^-\\s+\\[([ xX])\\]\\s+")
    checklistRegex.findAll(rawText).forEach { match ->
      val isChecked = match.groupValues[1].equals("x", ignoreCase = true)
      builder.addStyle(
        SpanStyle(
          fontWeight = FontWeight.Bold,
          color = if (isChecked) primaryColor else onSurfaceColor.copy(alpha = 0.7f)
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    // Quote lines (> quote)
    val quoteRegex = Regex("(?m)^>\\s+(.*)$")
    quoteRegex.findAll(rawText).forEach { match ->
      builder.addStyle(
        SpanStyle(
          fontStyle = FontStyle.Italic,
          color = primaryColor.copy(alpha = 0.9f)
        ),
        match.range.first,
        match.range.last + 1
      )
    }

    return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
  }
}
