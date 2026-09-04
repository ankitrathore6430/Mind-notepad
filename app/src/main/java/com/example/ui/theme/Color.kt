package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary & Brand Colors
val MindPrimaryLight = Color(0xFF4338CA)
val MindOnPrimaryLight = Color(0xFFFFFFFF)
val MindPrimaryContainerLight = Color(0xFFE0E7FF)
val MindOnPrimaryContainerLight = Color(0xFF1E1B4B)

val MindSecondaryLight = Color(0xFF0F766E)
val MindOnSecondaryLight = Color(0xFFFFFFFF)
val MindSecondaryContainerLight = Color(0xFFCCFBF1)
val MindOnSecondaryContainerLight = Color(0xFF134E4A)

val MindBackgroundLight = Color(0xFFF8FAFC)
val MindOnBackgroundLight = Color(0xFF0F172A)
val MindSurfaceLight = Color(0xFFFFFFFF)
val MindOnSurfaceLight = Color(0xFF0F172A)
val MindSurfaceVariantLight = Color(0xFFF1F5F9)
val MindOnSurfaceVariantLight = Color(0xFF475569)
val MindOutlineLight = Color(0xFFCBD5E1)

// Dark Palette
val MindPrimaryDark = Color(0xFF818CF8)
val MindOnPrimaryDark = Color(0xFF1E1B4B)
val MindPrimaryContainerDark = Color(0xFF312E81)
val MindOnPrimaryContainerDark = Color(0xFFE0E7FF)

val MindSecondaryDark = Color(0xFF2DD4BF)
val MindOnSecondaryDark = Color(0xFF042F2C)
val MindSecondaryContainerDark = Color(0xFF115E59)
val MindOnSecondaryContainerDark = Color(0xFFCCFBF1)

val MindBackgroundDark = Color(0xFF090D16)
val MindOnBackgroundDark = Color(0xFFF1F5F9)
val MindSurfaceDark = Color(0xFF131C2E)
val MindOnSurfaceDark = Color(0xFFF1F5F9)
val MindSurfaceVariantDark = Color(0xFF1E293B)
val MindOnSurfaceVariantDark = Color(0xFF94A3B8)
val MindOutlineDark = Color(0xFF334155)

// Note Card Color Palettes (Index 0..6)
object NoteColors {
  data class NoteColorDef(
    val id: Int,
    val name: String,
    val lightBg: Color,
    val lightBorder: Color,
    val darkBg: Color,
    val darkBorder: Color,
    val dotColor: Color
  )

  val palettes = listOf(
    NoteColorDef(
      id = 0,
      name = "Classic",
      lightBg = Color(0xFFFFFFFF),
      lightBorder = Color(0xFFE2E8F0),
      darkBg = Color(0xFF182234),
      darkBorder = Color(0xFF334155),
      dotColor = Color(0xFF94A3B8)
    ),
    NoteColorDef(
      id = 1,
      name = "Warm Amber",
      lightBg = Color(0xFFFEF3C7),
      lightBorder = Color(0xFFFDE68A),
      darkBg = Color(0xFF32230D),
      darkBorder = Color(0xFF78350F),
      dotColor = Color(0xFFF59E0B)
    ),
    NoteColorDef(
      id = 2,
      name = "Mint Sage",
      lightBg = Color(0xFFDCFCE7),
      lightBorder = Color(0xFFBBF7D0),
      darkBg = Color(0xFF122C20),
      darkBorder = Color(0xFF065F46),
      dotColor = Color(0xFF10B981)
    ),
    NoteColorDef(
      id = 3,
      name = "Sky Blue",
      lightBg = Color(0xFFE0F2FE),
      lightBorder = Color(0xFFBAE6FD),
      darkBg = Color(0xFF0E2C44),
      darkBorder = Color(0xFF0369A1),
      dotColor = Color(0xFF0EA5E9)
    ),
    NoteColorDef(
      id = 4,
      name = "Lavender",
      lightBg = Color(0xFFEDE9FE),
      lightBorder = Color(0xFFDDD6FE),
      darkBg = Color(0xFF281C48),
      darkBorder = Color(0xFF5B21B6),
      dotColor = Color(0xFF8B5CF6)
    ),
    NoteColorDef(
      id = 5,
      name = "Rose Coral",
      lightBg = Color(0xFFFFE4E6),
      lightBorder = Color(0xFFFECDD3),
      darkBg = Color(0xFF3A1220),
      darkBorder = Color(0xFF881337),
      dotColor = Color(0xFFF43F5E)
    ),
    NoteColorDef(
      id = 6,
      name = "Lemon Cream",
      lightBg = Color(0xFFFEF9C3),
      lightBorder = Color(0xFFFEF08A),
      darkBg = Color(0xFF302B08),
      darkBorder = Color(0xFF713F12),
      dotColor = Color(0xFFEAB308)
    )
  )

  fun get(id: Int): NoteColorDef {
    return palettes.find { it.id == id } ?: palettes.first()
  }
}
