package com.example.audionotes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun observeNotes(): Flow<List<Note>> = noteDao.observeNotes()

    suspend fun save(content: String) {
        noteDao.insert(Note(content = content.trim(), createdAt = System.currentTimeMillis()))
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
}
