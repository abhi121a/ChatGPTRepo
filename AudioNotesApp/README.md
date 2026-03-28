# Audio Notes Android App

A simple Android application that:

1. Records voice input through Android speech recognition.
2. Converts spoken audio to text.
3. Saves each transcript as a bullet-point note in the app.
4. Persists notes locally between app launches.

## Build APK

From this folder:

```bash
cd AudioNotesApp
JAVA_HOME=$HOME/.local/share/mise/installs/java/21.0.2 PATH=$JAVA_HOME/bin:$PATH gradle assembleDebug
```

APK output path:

`app/build/outputs/apk/debug/app-debug.apk`

## Notes for this container

- Java 25 is the default in this environment and is not compatible with Android Gradle Plugin used here.
- Use Java 21 for build commands as shown above.
