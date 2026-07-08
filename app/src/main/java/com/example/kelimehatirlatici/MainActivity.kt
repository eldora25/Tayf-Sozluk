package com.example.kelimehatirlatici

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.tts.TtsManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = WordRepository(database.wordDao())

        ttsManager = TtsManager(this)

        setContent {
            AppScreen(
                repository = repository,
                context = this,
                onSpeak = { text ->
                    ttsManager.speak(text)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
