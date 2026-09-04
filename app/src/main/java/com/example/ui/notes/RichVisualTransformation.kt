package com.example.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class RichVisualTransformation(
  private val contentProvider: () -> RichNoteContent,
  private val primaryColor: Color,
  private val onSurfaceColor: Color,
  private val codeBgColor: Color
) : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    val richContent = contentProvider()
    val annotated = richContent.toAnnotatedString(
      primaryColor = primaryColor,
      onSurfaceColor = onSurfaceColor,
      codeBgColor = codeBgColor
    )
    return TransformedText(annotated, OffsetMapping.Identity)
  }
}
