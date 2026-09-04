package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String) {
  SYSTEM("System Default"),
  LIGHT("Light Theme"),
  DARK("Dark Theme")
}

class ThemePreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("mind_notepad_prefs", Context.MODE_PRIVATE)

  private val _themeMode = MutableStateFlow(loadThemeMode())
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  private fun loadThemeMode(): ThemeMode {
    val saved = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
    return try {
      ThemeMode.valueOf(saved)
    } catch (_: Exception) {
      ThemeMode.SYSTEM
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    _themeMode.value = mode
  }

  companion object {
    private const val KEY_THEME_MODE = "key_theme_mode"
  }
}
