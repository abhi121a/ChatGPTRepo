package com.example.audionotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AudioNotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: AudioNotesDatabase? = null

        fun getInstance(context: Context): AudioNotesDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AudioNotesDatabase::class.java,
                    "audio_notes.db"
                ).build().also { instance = it }
            }
        }
    }
}
