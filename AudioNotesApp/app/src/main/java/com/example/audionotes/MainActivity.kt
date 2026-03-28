package com.example.audionotes

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var notesText: TextView
    private lateinit var prefs: SharedPreferences
    private val noteItems = mutableListOf<String>()

    private val speechToTextLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()

        if (!spokenText.isNullOrBlank()) {
            noteItems.add(withTimestamp(spokenText.trim()))
            persistNotes()
            renderNotes()
        } else {
            Toast.makeText(this, getString(R.string.no_speech_detected), Toast.LENGTH_SHORT).show()
        }
    }

    private val microphonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechToText()
        } else {
            Toast.makeText(this, getString(R.string.microphone_permission_required), Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        notesText = findViewById(R.id.notesText)
        val recordButton: Button = findViewById(R.id.recordButton)
        val clearButton: Button = findViewById(R.id.clearButton)

        loadNotes()
        renderNotes()

        recordButton.setOnClickListener { ensurePermissionAndRecord() }

        clearButton.setOnClickListener {
            noteItems.clear()
            persistNotes()
            renderNotes()
            Toast.makeText(this, getString(R.string.notes_cleared), Toast.LENGTH_SHORT).show()
        }
    }

    private fun ensurePermissionAndRecord() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSpeechToText()
        } else {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun launchSpeechToText() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_prompt))
        }

        if (speechIntent.resolveActivity(packageManager) != null) {
            speechToTextLauncher.launch(speechIntent)
        } else {
            Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_LONG).show()
        }
    }

    private fun renderNotes() {
        if (noteItems.isEmpty()) {
            notesText.text = getString(R.string.empty_notes)
            return
        }

        notesText.text = noteItems.joinToString(separator = "\n") { "• $it" }
    }

    private fun loadNotes() {
        val savedNotes = prefs.getString(KEY_NOTES, "").orEmpty()
        noteItems.clear()
        if (savedNotes.isNotBlank()) {
            noteItems.addAll(savedNotes.split(NOTE_SEPARATOR))
        }
    }

    private fun persistNotes() {
        val payload = noteItems.joinToString(separator = NOTE_SEPARATOR)
        prefs.edit().putString(KEY_NOTES, payload).apply()
    }

    private fun withTimestamp(note: String): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return "$stamp - $note"
    }

    companion object {
        private const val PREFS_NAME = "audio_notes_prefs"
        private const val KEY_NOTES = "notes"
        private const val NOTE_SEPARATOR = "\u0001"
    }
}
