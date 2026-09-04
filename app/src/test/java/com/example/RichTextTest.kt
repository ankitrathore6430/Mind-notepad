package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.ui.notes.LineType
import com.example.ui.notes.RichNoteContent
import com.example.ui.notes.RichTextEditorManager
import com.example.ui.notes.StyleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RichTextTest {

  @Test
  fun testBoldToggleDoesNotAddAsterisks() {
    val initial = RichNoteContent(
      plainText = "Mind Notepad is awesome",
      spans = emptyList(),
      lineStyles = emptyMap()
    )

    // Select "Mind Notepad" (indices 0..12)
    val selection = TextRange(0, 12)
    val styled = RichTextEditorManager.toggleInlineStyle(initial, selection, StyleType.BOLD)

    // Verify plainText contains NO asterisks
    assertEquals("Mind Notepad is awesome", styled.plainText)
    assertFalse(styled.plainText.contains("*"))

    // Verify bold span exists for 0..12
    assertEquals(1, styled.spans.size)
    assertEquals(StyleType.BOLD, styled.spans[0].type)
    assertEquals(0, styled.spans[0].start)
    assertEquals(12, styled.spans[0].end)

    // Verify HTML serialization contains <b>
    val html = styled.toHtml()
    assertTrue(html.contains("<b>Mind Notepad</b>"))

    // Verify deserialization restores the exact text and span without asterisks
    val parsed = RichNoteContent.fromString(html)
    assertEquals("Mind Notepad is awesome", parsed.plainText)
    assertEquals(1, parsed.spans.size)
    assertEquals(StyleType.BOLD, parsed.spans[0].type)
  }

  @Test
  fun testItalicAndStrikeToggle() {
    val initial = RichNoteContent("Design and Code", emptyList(), emptyMap())

    // Italic on "Design" (0..6)
    val italic = RichTextEditorManager.toggleInlineStyle(initial, TextRange(0, 6), StyleType.ITALIC)
    assertEquals("Design and Code", italic.plainText)
    assertFalse(italic.plainText.contains("*"))

    // Strikethrough on "Code" (11..15)
    val strike = RichTextEditorManager.toggleInlineStyle(italic, TextRange(11, 15), StyleType.STRIKE)
    assertEquals("Design and Code", strike.plainText)
    assertFalse(strike.plainText.contains("~"))

    assertEquals(2, strike.spans.size)
  }

  @Test
  fun testH1LineStyleDoesNotAddHash() {
    val initial = RichNoteContent("My Project Notes\nTasks to complete", emptyList(), emptyMap())

    // Cursor on line 0 (cursor at 5)
    val h1Styled = RichTextEditorManager.toggleLineStyle(initial, 5, LineType.H1)

    // Text must remain completely clean, no #
    assertEquals("My Project Notes\nTasks to complete", h1Styled.plainText)
    assertFalse(h1Styled.plainText.contains("#"))
    assertEquals(LineType.H1, h1Styled.lineStyles[0])

    val html = h1Styled.toHtml()
    assertTrue(html.startsWith("<h1>My Project Notes</h1>"))
  }

  @Test
  fun testChecklistAndBulletToggle() {
    val initial = RichNoteContent("Buy groceries", emptyList(), emptyMap())
    val tfv = TextFieldValue(text = initial.plainText, selection = TextRange(5))

    // Toggle checklist -> inserts "☐ "
    val (withBox, tfvBox) = RichTextEditorManager.toggleChecklist(initial, tfv)
    assertEquals("☐ Buy groceries", withBox.plainText)

    // Toggle checklist again -> checks it "☑ "
    val (checked, _) = RichTextEditorManager.toggleChecklist(withBox, tfvBox)
    assertEquals("☑ Buy groceries", checked.plainText)

    // Toggle bullet on clean text
    val (withBullet, _) = RichTextEditorManager.toggleBullet(initial, tfv)
    assertEquals("• Buy groceries", withBullet.plainText)
  }

  @Test
  fun testLegacyMarkdownUpgradedCleanly() {
    val legacyMarkdown = "# Welcome\nThis is **bold** text and *italic*."
    val parsed = RichNoteContent.fromString(legacyMarkdown)

    // Hash and asterisks should be stripped from plainText!
    assertEquals("Welcome\nThis is bold text and italic.", parsed.plainText)
    assertFalse(parsed.plainText.contains("**"))
    assertFalse(parsed.plainText.contains("#"))

    // Line 0 is H1
    assertEquals(LineType.H1, parsed.lineStyles[0])

    // "bold" is bold span
    val boldSpan = parsed.spans.find { it.type == StyleType.BOLD }
    assertTrue(boldSpan != null)
    val boldWord = parsed.plainText.substring(boldSpan!!.start, boldSpan.end)
    assertEquals("bold", boldWord)
  }
}
