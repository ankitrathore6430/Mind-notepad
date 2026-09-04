package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class MindDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao

  companion object {
    @Volatile
    private var INSTANCE: MindDatabase? = null

    fun getDatabase(context: Context): MindDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MindDatabase::class.java,
          "mind_notepad.db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
