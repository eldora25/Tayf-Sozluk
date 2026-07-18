package com.example.kelimehatirlatici

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.tts.TtsManager

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF7B1FA2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1BEE7),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1B1F),
    error = Color(0xFFF44336),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFFCE93D8),
    onSecondary = Color(0xFF4A148C),
    secondaryContainer = Color(0xFF7B1FA2),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005)
)

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
            val darkMode by remember { mutableStateOf(settings.darkMode) }

            MaterialTheme(
                colorScheme = if (darkMode) DarkColors else LightColors
            ) {
                AppScreen(
                    repository = repository,
                    settings = settings,
                    context = this,
                    onSpeak = { text -> ttsManager.speak(text) }
                )
            }
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}
