package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String = "",
  val content: String = "",
  val category: String = "General",
  val colorIndex: Int = 0,
  val isPinned: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) {
  val wordCount: Int
    get() {
      val text = "$title $content".trim()
      return if (text.isEmpty()) 0 else text.split("\\s+".toRegex()).size
    }

  val charCount: Int
    get() = (title.length + content.length)
}
