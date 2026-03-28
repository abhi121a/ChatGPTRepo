# Audio Notes (Android)

A modern **Speech-to-Text Notes App** built with:

- **Kotlin**
- **Jetpack Compose + Material 3**
- **MVVM architecture**
- **Room Database** for local storage
- **Android `SpeechRecognizer`** for near real-time transcription

## Features

- Press-and-hold microphone to start transcription.
- Live partial text updates while speaking.
- Final transcription on release.
- Save notes locally with timestamps.
- Share current note with Android Sharesheet (WhatsApp/Email/SMS/etc).
- Copy to clipboard.
- Delete notes from history.
- Graceful error handling for permission denial and recognition issues.

## Project Structure

```text
AudioNotesApp/
  app/src/main/java/com/example/audionotes/
    MainActivity.kt
    data/
      Note.kt
      NoteDao.kt
      AudioNotesDatabase.kt
      NoteRepository.kt
    ui/
      AudioNotesViewModel.kt
```

## Build APK (Windows)

### Option A: Android Studio (Recommended)
1. Open project folder: `C:\Users\LENOVO\Documents\Repo\ChatGPTRepo\AudioNotesApp`
2. Wait for Gradle sync.
3. Click **Build > Build Bundle(s)/APK(s) > Build APK(s)**.
4. Find output at:
   `app/build/outputs/apk/debug/app-debug.apk`

### Option B: Command Line

```powershell
cd C:\Users\LENOVO\Documents\Repo\ChatGPTRepo\AudioNotesApp
# Ensure JDK 17 or 21 is active
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
gradle assembleDebug
```

## Icon Design

The launcher icon uses a custom adaptive icon:
- **Background**: premium blue
- **Foreground**: document card + microphone glyph
- Concept: "voice + notes" in a clean minimal visual style.

## Permissions

- `RECORD_AUDIO`
- `INTERNET`
