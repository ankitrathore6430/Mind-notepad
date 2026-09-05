package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.preferences.ThemeMode

private val DarkColorScheme =
  darkColorScheme(
    primary = MindPrimaryDark,
    onPrimary = MindOnPrimaryDark,
    primaryContainer = MindPrimaryContainerDark,
    onPrimaryContainer = MindOnPrimaryContainerDark,
    secondary = MindSecondaryDark,
    onSecondary = MindOnSecondaryDark,
    secondaryContainer = MindSecondaryContainerDark,
    onSecondaryContainer = MindOnSecondaryContainerDark,
    background = MindBackgroundDark,
    onBackground = MindOnBackgroundDark,
    surface = MindSurfaceDark,
    onSurface = MindOnSurfaceDark,
    surfaceVariant = MindSurfaceVariantDark,
    onSurfaceVariant = MindOnSurfaceVariantDark,
    outline = MindOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MindPrimaryLight,
    onPrimary = MindOnPrimaryLight,
    primaryContainer = MindPrimaryContainerLight,
    onPrimaryContainer = MindOnPrimaryContainerLight,
    secondary = MindSecondaryLight,
    onSecondary = MindOnSecondaryLight,
    secondaryContainer = MindSecondaryContainerLight,
    onSecondaryContainer = MindOnSecondaryContainerLight,
    background = MindBackgroundLight,
    onBackground = MindOnBackgroundLight,
    surface = MindSurfaceLight,
    onSurface = MindOnSurfaceLight,
    surfaceVariant = MindSurfaceVariantLight,
    onSurfaceVariant = MindOnSurfaceVariantLight,
    outline = MindOutlineLight
  )

@Composable
fun MindNotepadTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        // In Light Mode (!isDark == true), status bar icons (time, battery, wifi) are dark so they are clearly visible on light backgrounds.
        // In Dark Mode (!isDark == false), status bar icons are light/white.
        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
