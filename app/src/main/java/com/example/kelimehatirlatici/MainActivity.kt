package com.example.kelimehatirlatici

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.tts.TtsManager

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager
    private lateinit var repository: WordRepository
    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TtsManager(this)
        val dao = AppDatabase.getDatabase(this).wordDao()
        repository = WordRepository(dao)
        settings = AppSettings(this)

        setContent {
            AppScreen(
                repository = repository,
                settings = settings,
                context = this,
                onSpeak = { text -> ttsManager.speak(text) }
            )
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}
