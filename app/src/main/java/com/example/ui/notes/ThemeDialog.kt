package com.example.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.preferences.ThemeMode

@Composable
fun ThemeSelectionDialog(
  currentMode: ThemeMode,
  onModeSelected: (ThemeMode) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Choose Theme",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        ThemeOptionRow(
          title = "Light Theme",
          subtitle = "Bright, crisp paper style",
          icon = Icons.Default.LightMode,
          selected = currentMode == ThemeMode.LIGHT,
          testTag = "theme_option_light",
          onClick = {
            onModeSelected(ThemeMode.LIGHT)
            onDismiss()
          }
        )

        ThemeOptionRow(
          title = "Dark Theme",
          subtitle = "OLED dark, easy on the eyes",
          icon = Icons.Default.DarkMode,
          selected = currentMode == ThemeMode.DARK,
          testTag = "theme_option_dark",
          onClick = {
            onModeSelected(ThemeMode.DARK)
            onDismiss()
          }
        )

        ThemeOptionRow(
          title = "System Default",
          subtitle = "Follows Android system appearance",
          icon = Icons.Default.BrightnessAuto,
          selected = currentMode == ThemeMode.SYSTEM,
          testTag = "theme_option_system",
          onClick = {
            onModeSelected(ThemeMode.SYSTEM)
            onDismiss()
          }
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("close_theme_dialog_button")
      ) {
        Text("Done")
      }
    },
    shape = RoundedCornerShape(24.dp)
  )
}

@Composable
private fun ThemeOptionRow(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      RadioButton(
        selected = selected,
        onClick = onClick,
        colors = RadioButtonDefaults.colors(
          selectedColor = MaterialTheme.colorScheme.primary
        )
      )
    }
  }
}
