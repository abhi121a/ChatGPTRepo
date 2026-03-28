package com.example.audionotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audionotes.data.Note
import com.example.audionotes.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioNotesUiState(
    val transcription: String = "",
    val isListening: Boolean = false,
    val errorMessage: String? = null
)

class AudioNotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioNotesUiState())
    val uiState: StateFlow<AudioNotesUiState> = _uiState.asStateFlow()

    val notes: StateFlow<List<Note>> = repository.observeNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onListeningStarted() {
        _uiState.update { it.copy(isListening = true, errorMessage = null) }
    }

    fun onPartialTranscription(text: String) {
        _uiState.update { it.copy(transcription = text) }
    }

    fun onFinalTranscription(text: String) {
        _uiState.update { it.copy(transcription = text, isListening = false) }
    }

    fun onListeningStopped() {
        _uiState.update { it.copy(isListening = false) }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isListening = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveCurrentNote() {
        val content = uiState.value.transcription.trim()
        if (content.isBlank()) {
            showError("Nothing to save yet.")
            return
        }

        viewModelScope.launch {
            repository.save(content)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun clearTranscription() {
        _uiState.update { it.copy(transcription = "") }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioNotesViewModel(repository) as T
        }
    }
}
