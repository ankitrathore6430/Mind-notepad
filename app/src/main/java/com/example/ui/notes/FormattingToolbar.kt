package com.example.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormattingToolbar(
  currentLineType: LineType = LineType.NORMAL,
  activeStyles: Set<StyleType> = emptySet(),
  onH1Click: () -> Unit,
  onH2Click: () -> Unit,
  onBoldClick: () -> Unit,
  onItalicClick: () -> Unit,
  onStrikeClick: () -> Unit,
  onBulletClick: () -> Unit,
  onChecklistClick: () -> Unit,
  onQuoteClick: () -> Unit,
  onCodeClick: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
    shape = RoundedCornerShape(16.dp),
    tonalElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp, horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      LazyRow(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // H1 Heading
        item {
          FormatTextButton(
            label = "H1",
            isActive = currentLineType == LineType.H1,
            fontWeight = FontWeight.Black,
            testTag = "format_h1_button",
            onClick = onH1Click
          )
        }

        // H2 Heading
        item {
          FormatTextButton(
            label = "H2",
            isActive = currentLineType == LineType.H2,
            fontWeight = FontWeight.Bold,
            testTag = "format_h2_button",
            onClick = onH2Click
          )
        }

        // Bold
        item {
          FormatIconButton(
            icon = Icons.Default.FormatBold,
            isActive = activeStyles.contains(StyleType.BOLD),
            contentDescription = "Bold",
            testTag = "format_bold_button",
            onClick = onBoldClick
          )
        }

        // Italic
        item {
          FormatIconButton(
            icon = Icons.Default.FormatItalic,
            isActive = activeStyles.contains(StyleType.ITALIC),
            contentDescription = "Italic",
            testTag = "format_italic_button",
            onClick = onItalicClick
          )
        }

        // Strikethrough
        item {
          FormatIconButton(
            icon = Icons.Default.FormatStrikethrough,
            isActive = activeStyles.contains(StyleType.STRIKE),
            contentDescription = "Strikethrough",
            testTag = "format_strike_button",
            onClick = onStrikeClick
          )
        }

        // Bullet List
        item {
          FormatIconButton(
            icon = Icons.Default.FormatListBulleted,
            isActive = false,
            contentDescription = "Bullet List",
            testTag = "format_bullet_button",
            onClick = onBulletClick
          )
        }

        // Checklist Task
        item {
          FormatIconButton(
            icon = Icons.Default.TaskAlt,
            isActive = false,
            contentDescription = "Checklist",
            testTag = "format_task_button",
            onClick = onChecklistClick
          )
        }

        // Quote
        item {
          FormatIconButton(
            icon = Icons.Default.FormatQuote,
            isActive = currentLineType == LineType.QUOTE,
            contentDescription = "Quote",
            testTag = "format_quote_button",
            onClick = onQuoteClick
          )
        }

        // Monospace Code
        item {
          FormatTextButton(
            label = "< />",
            isActive = activeStyles.contains(StyleType.CODE),
            fontFamily = FontFamily.Monospace,
            testTag = "format_code_button",
            onClick = onCodeClick
          )
        }
      }

      // Close / Hide toolbar button
      IconButton(
        onClick = onClose,
        modifier = Modifier
          .size(32.dp)
          .testTag("hide_format_toolbar_button")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Hide formatting tools",
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun FormatIconButton(
  icon: ImageVector,
  isActive: Boolean,
  contentDescription: String,
  testTag: String,
  onClick: () -> Unit
) {
  val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
  val iconColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconColor,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable
private fun FormatTextButton(
  label: String,
  isActive: Boolean,
  fontWeight: FontWeight = FontWeight.Bold,
  fontFamily: FontFamily? = null,
  testTag: String,
  onClick: () -> Unit
) {
  val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
  val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

  Box(
    modifier = Modifier
      .height(36.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(bgColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      fontWeight = fontWeight,
      fontFamily = fontFamily,
      color = textColor
    )
  }
}
