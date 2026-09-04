package com.example.data.repository

import com.example.data.local.Note
import com.example.data.local.NoteDao
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

  fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

  fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

  fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id)

  suspend fun getNoteByIdOnce(id: Long): Note? = noteDao.getNoteByIdOnce(id)

  suspend fun insert(note: Note): Long = noteDao.insertNote(note)

  suspend fun update(note: Note) = noteDao.updateNote(note)

  suspend fun delete(note: Note) = noteDao.deleteNote(note)

  suspend fun deleteById(id: Long) = noteDao.deleteNoteById(id)

  suspend fun togglePin(id: Long, isPinned: Boolean) {
    noteDao.updatePinStatus(id, !isPinned, System.currentTimeMillis())
  }
}
