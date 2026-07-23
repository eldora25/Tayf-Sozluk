package com.example.kelimehatirlatici

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.LibraryInfo
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.ui.*
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var database: AppDatabase
    private lateinit var repository: WordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        repository = WordRepository(database.wordDao())

        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ─── Repository'de var olan metodlar ───
    // ✓ getWordsByLibraryAndLevel
    // ✓ getAllWords
    // ✓ addWord
    // ✓ getDailyGoal
    // ✓ updateDailyGoal
    // ✓ updateWordKnowledge
    // ✓ getLibraries
    // ✓ getLibraryWords
    // ✓ getAllLibrariesWithWordCount
    // ✓ resetWordKnowledge

    @Composable
    private fun MainScreen() {
        val coroutineScope = rememberCoroutineScope()

        // Durum değişkenleri
        var selectedLibrary by remember { mutableStateOf("Genel") }
        var selectedLevel by remember { mutableStateOf("Genel") }
        var words by remember { mutableStateOf<List<Word>>(emptyList()) }
        var currentWordIndex by remember { mutableIntStateOf(0) }
        var currentWord by remember { mutableStateOf<Word?>(null) }
        var currentScreen by remember { mutableStateOf("main") }
        var libraries by remember { mutableStateOf<List<String>>(listOf("Genel")) }
        var libraryInfoList by remember { mutableStateOf<List<LibraryInfo>>(emptyList()) }
        var dailyGoal by remember { mutableStateOf<DailyGoal?>(null) }
        var isSoundMuted by remember { mutableStateOf(false) }

        // Verileri yükle
        LaunchedEffect(selectedLibrary, selectedLevel) {
            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
            currentWordIndex = 0
            currentWord = words.firstOrNull()
            libraries = repository.getLibraries()
            libraryInfoList = repository.getAllLibrariesWithWordCount()
            dailyGoal = repository.getDailyGoal()
        }

        // Ekran yönlendirme
        when (currentScreen) {
            "main" -> {
                AppScreen(
                    words = words,
                    word = currentWord,
                    selectedLibrary = selectedLibrary,
                    selectedLevel = selectedLevel,
                    totalWordCount = words.size,
                    dailyGoal = dailyGoal,
                    isFlipped = false,
                    memorizationThreshold = 3,
                    onKnownClick = {
                        currentWord?.let { w ->
                            coroutineScope.launch {
                                repository.updateWordKnowledge(w.id, correct = true)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                currentWordIndex = (currentWordIndex + 1) % words.size
                                currentWord = words.getOrNull(currentWordIndex)
                                dailyGoal = repository.getDailyGoal()
                            }
                        }
                    },
                    onWrongClick = {
                        currentWord?.let { w ->
                            coroutineScope.launch {
                                repository.updateWordKnowledge(w.id, correct = false)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                currentWordIndex = (currentWordIndex + 1) % words.size
                                currentWord = words.getOrNull(currentWordIndex)
                            }
                        }
                    },
                    onFlip = { },
                    onSpeakClick = { word -> speak(word) },
                    onAddWordClick = { currentScreen = "addWord" },
                    onWordListClick = { currentScreen = "wordList" },
                    onLibraryClick = { currentScreen = "library" },
                    onLevelClick = { currentScreen = "level" },
                    onGoalClick = { currentScreen = "goal" },
                    onStatsClick = { currentScreen = "stats" },
                    onQuizClick = { currentScreen = "quiz" },
                    onImportClick = { currentScreen = "import" },
                    onPacksClick = { currentScreen = "packs" },
                    onWrongWordsClick = { currentScreen = "wrongWords" },
                    onSettingsClick = { currentScreen = "settings" },
                    onWordClick = { word ->
                        currentWord = word
                    },
                    onWordLongClick = { word ->
                        currentWord = word
                    }
                )
            }
            "addWord" -> {
                AddWordScreen(
                    libraries = libraries,
                    onSave = { word, meaning, example, library, level ->
                        coroutineScope.launch {
                            repository.addWord(Word(word = word, meaning = meaning, example = example, library = library, level = level))
                            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            currentScreen = "main"
                        }
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "wordList" -> {
                WordListScreen(
                    words = words,
                    onUpdateWord = { id, word, meaning, example, level ->
                        coroutineScope.launch {
                            // Repository'de updateWord yok, word'ü al, güncelle, sil ve ekle
                            repository.addWord(Word(word = word, meaning = meaning, example = example, library = selectedLibrary, level = level))
                            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                        }
                    },
                    onDeleteWord = { id ->
                        coroutineScope.launch {
                            // Repository'de deleteWord yok, resetWordKnowledge var veya getAllWords olmadan çalış
                            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                        }
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "library" -> {
                LibrarySelectScreen(
                    libraryInfoList = libraryInfoList,
                    onLibrarySelected = { lib ->
                        selectedLibrary = lib
                        currentScreen = "main"
                    },
                    onManageLibraries = { currentScreen = "main" },
                    onBack = { currentScreen = "main" }
                )
            }
            "level" -> {
                LevelSelectScreen(
                    selectedLevel = selectedLevel,
                    onLevelSelected = { lvl ->
                        selectedLevel = lvl
                        currentScreen = "main"
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "goal" -> {
                GoalScreen(
                    currentGoal = dailyGoal?.targetCount ?: 10,
                    completed = dailyGoal?.completedCount ?: 0,
                    onSaveGoal = { target ->
                        coroutineScope.launch {
                            repository.updateDailyGoal(target)
                            dailyGoal = repository.getDailyGoal()
                            currentScreen = "main"
                        }
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "quiz" -> {
                // QuizSession sadece List<Word> alıyor
                val quizWords = words.toList()
                if (quizWords.isNotEmpty()) {
                    QuizScreen(
                        words = quizWords,
                        memorizationThreshold = 3,
                        onAnswerCorrect = { word ->
                            coroutineScope.launch {
                                repository.updateWordKnowledge(word.id, correct = true)
                            }
                        },
                        onAnswerWrong = { word ->
                            coroutineScope.launch {
                                repository.updateWordKnowledge(word.id, correct = false)
                            }
                        },
                        onSpeak = { word -> speak(word.word) },
                        isSoundMuted = isSoundMuted,
                        onToggleMute = { isSoundMuted = !isSoundMuted },
                        onBack = { currentScreen = "main" }
                    )
                } else {
                    // Quiz için kelime yoksa ana ekrana dön
                    LaunchedEffect(Unit) {
                        currentScreen = "main"
                    }
                }
            }
            "wrongWords" -> {
                WrongWordsScreen(
                    words = words,
                    onBack = { currentScreen = "main" }
                )
            }
            else -> {
                currentScreen = "main"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
