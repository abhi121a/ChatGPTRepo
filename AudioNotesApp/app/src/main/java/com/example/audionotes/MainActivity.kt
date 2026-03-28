package com.example.audionotes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audionotes.data.AudioNotesDatabase
import com.example.audionotes.data.Note
import com.example.audionotes.data.NoteRepository
import com.example.audionotes.ui.AudioNotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val speechIntent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    private val viewModel: AudioNotesViewModel by viewModels {
        AudioNotesViewModel.Factory(
            NoteRepository(AudioNotesDatabase.getInstance(applicationContext).noteDao())
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startListening()
        } else {
            viewModel.showError("Microphone permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initSpeechRecognizer()

        setContent {
            MaterialTheme {
                AudioNotesRoute(
                    viewModel = viewModel,
                    onMicPress = { onMicPressed() },
                    onMicRelease = { onMicReleased() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    private fun onMicPressed() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        startListening()
    }

    private fun onMicReleased() {
        stopListening()
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            viewModel.showError("Speech recognition is not available on this device.")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    viewModel.onListeningStarted()
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    viewModel.onListeningStopped()
                }

                override fun onError(error: Int) {
                    viewModel.onListeningStopped()
                    if (error != SpeechRecognizer.ERROR_CLIENT) {
                        viewModel.showError("No speech detected or recognition failed. Please try again.")
                    }
                }

                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty()
                    viewModel.onFinalTranscription(text)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val text = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    if (text.isNotBlank()) {
                        viewModel.onPartialTranscription(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
    }

    private fun startListening() {
        speechRecognizer?.cancel()
        speechRecognizer?.startListening(speechIntent)
    }

    private fun stopListening() {
        speechRecognizer?.stopListening()
    }
}

@Composable
private fun AudioNotesRoute(
    viewModel: AudioNotesViewModel = viewModel(),
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    AudioNotesScreen(
        uiState = uiState,
        notes = notes,
        snackbarHostState = snackbarHostState,
        onMicPress = onMicPress,
        onMicRelease = onMicRelease,
        onSave = viewModel::saveCurrentNote,
        onShare = {
            val text = uiState.transcription.trim()
            if (text.isBlank()) {
                viewModel.showError("Nothing to share yet.")
                return@AudioNotesScreen
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share note"))
        },
        onCopy = {
            val text = uiState.transcription.trim()
            if (text.isBlank()) {
                viewModel.showError("Nothing to copy yet.")
                return@AudioNotesScreen
            }
            clipboardManager.setText(AnnotatedString(text))
            viewModel.showError("Copied to clipboard.")
        },
        onDelete = viewModel::deleteNote,
        onClearDraft = viewModel::clearTranscription
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioNotesScreen(
    uiState: com.example.audionotes.ui.AudioNotesUiState,
    notes: List<Note>,
    snackbarHostState: SnackbarHostState,
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onDelete: (Note) -> Unit,
    onClearDraft: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Notes") },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TranscriptionCard(
                text = uiState.transcription,
                isListening = uiState.isListening,
                onClearDraft = onClearDraft
            )

            DraftActions(
                onSave = onSave,
                onShare = onShare,
                onCopy = onCopy
            )

            Text(
                text = "Saved Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            NotesHistory(
                notes = notes,
                onDelete = onDelete,
                modifier = Modifier.weight(1f)
            )

            HoldToTalkMicButton(
                isListening = uiState.isListening,
                onPress = onMicPress,
                onRelease = onMicRelease,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun TranscriptionCard(
    text: String,
    isListening: Boolean,
    onClearDraft: () -> Unit
) {
    val displayText = text.ifBlank { "Hold the button and start speaking..." }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = if (isListening) "Listening…" else "Transcription",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (text.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
            if (text.isNotBlank()) {
                TextButton(onClick = onClearDraft) {
                    Text("Clear Draft")
                }
            }
        }
    }
}

@Composable
private fun DraftActions(
    onSave: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledIconButton(onClick = onSave, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Save, contentDescription = "Save")
        }
        FilledIconButton(onClick = onShare, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Send, contentDescription = "Share")
        }
        FilledIconButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotesHistory(
    notes: List<Note>,
    onDelete: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) {
        Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No saved notes yet.")
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = { onDelete(note) }),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatTimestamp(note.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.content,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onDelete(note) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete note")
                    }
                }
            }
        }
    }
}

@Composable
private fun HoldToTalkMicButton(
    isListening: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val color by animateColorAsState(
        targetValue = if (isListening || isPressed) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        label = "micColor"
    )

    Surface(
        modifier = modifier
            .size(96.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val down = event.changes.any { it.pressed }
                        if (down && !isPressed) {
                            isPressed = true
                            onPress()
                        } else if (!down && isPressed) {
                            isPressed = false
                            onRelease()
                        }
                    }
                }
            },
        shape = CircleShape,
        color = color,
        shadowElevation = 10.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Press and hold to talk",
                modifier = Modifier.size(42.dp),
                tint = Color.White
            )
        }
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(epochMillis))
}
