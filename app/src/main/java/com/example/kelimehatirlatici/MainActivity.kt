package com.example.kelimehatirlatici

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.quiz.*
import com.example.kelimehatirlatici.ui.*
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var database: AppDatabase
    private lateinit var repository: WordRepository
    private lateinit var quizGenerator: QuizGenerator

    companion object {
        private const val TAG = "TayfSozluk"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crash handler'ı kaydet (Compose dışında, güvenli)
        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (currentHandler !is CrashHandler) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(currentHandler))
        }

        Log.d(TAG, "Uygulama başlatılıyor...")

        try {
            database = AppDatabase.getDatabase(this)
            Log.d(TAG, "Database başlatıldı")

            repository = WordRepository(database.wordDao())
            Log.d(TAG, "Repository başlatıldı")

            quizGenerator = QuizGenerator(repository)
            Log.d(TAG, "QuizGenerator başlatıldı")

            tts = TextToSpeech(this) { status ->
                if (status != TextToSpeech.ERROR) {
                    tts.language = Locale.US
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Başlatma hatası: ${e.message}", e)
            // Hata olursa yine de UI'ı göster, hata ekranda görünsün
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

        Log.d(TAG, "UI başlatıldı")
    }

    private fun speak(text: String) {
        try {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "TTS hatası: ${e.message}")
        }
    }

    @Composable
    private fun MainScreen() {
        val coroutineScope = rememberCoroutineScope()

        var selectedLibrary by remember { mutableStateOf("Genel") }
        var selectedLevel by remember { mutableStateOf("Genel") }
        var words by remember { mutableStateOf<List<Word>>(emptyList()) }
        var currentWordIndex by remember { mutableIntStateOf(0) }
        var currentWord by remember { mutableStateOf<Word?>(null) }
        var currentScreen by remember { mutableStateOf("main") }
        var libraries by remember { mutableStateOf<List<String>>(listOf("Genel")) }
        var quizSession by remember { mutableStateOf<QuizSession?>(null) }
        var isSoundMuted by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Hata varsa ErrorScreen göster (Composable içinde güvenli)
        if (errorMessage != null) {
            ErrorScreen(
                errorMessage = errorMessage!!,
                onRetry = {
                    errorMessage = null
                    currentScreen = "main"
                }
            )
            return
        }

        // Verileri yükle (Composable dışında değil, LaunchedEffect ile güvenli)
        LaunchedEffect(selectedLibrary, selectedLevel) {
            try {
                Log.d(TAG, "Veriler yükleniyor: library=$selectedLibrary, level=$selectedLevel")
                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                currentWordIndex = 0
                currentWord = words.firstOrNull()
                libraries = repository.getAllLibraries()
                Log.d(TAG, "Veriler yüklendi: ${words.size} kelime, ${libraries.size} kütüphane")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Veri yükleme hatası: ${e.message}", e)
                errorMessage = "Veri yüklenirken hata: ${e.message}"
            }
        }

        // when bloğu - artık try-catch YOK, hata state ile yönetiliyor
        when (currentScreen) {
            "main" -> {
                AppScreen(
                    words = words,
                    word = currentWord,
                    selectedLibrary = selectedLibrary,
                    selectedLevel = selectedLevel,
                    totalWordCount = words.size,
                    dailyGoal = null,
                    isFlipped = false,
                    memorizationThreshold = 3,
                    onKnownClick = {
                        currentWord?.let { w ->
                            coroutineScope.launch {
                                try {
                                    repository.resetIncorrectCount(w.id)
                                    words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                    currentWordIndex = (currentWordIndex + 1) % words.size
                                    currentWord = words.getOrNull(currentWordIndex)
                                } catch (e: Exception) {
                                    Log.e(TAG, "KnownClick hatası", e)
                                    errorMessage = "İşlem sırasında hata: ${e.message}"
                                }
                            }
                        }
                    },
                    onWrongClick = {
                        currentWord?.let { w ->
                            coroutineScope.launch {
                                try {
                                    repository.updateIncorrectCount(w.id)
                                    words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                    currentWordIndex = (currentWordIndex + 1) % words.size
                                    currentWord = words.getOrNull(currentWordIndex)
                                } catch (e: Exception) {
                                    Log.e(TAG, "WrongClick hatası", e)
                                    errorMessage = "İşlem sırasında hata: ${e.message}"
                                }
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
                    onQuizClick = {
                        coroutineScope.launch {
                            try {
                                val wordsForQuiz = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                if (wordsForQuiz.isNotEmpty()) {
                                    val questions = quizGenerator.generateQuestions(wordsForQuiz)
                                    if (questions.isNotEmpty()) {
                                        val session = QuizSession()
                                        session.questions = questions.toMutableList()
                                        quizSession = session
                                        currentScreen = "quiz"
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Quiz başlatma hatası", e)
                                errorMessage = "Quiz başlatılamadı: ${e.message}"
                            }
                        }
                    },
                    onImportClick = { currentScreen = "import" },
                    onPacksClick = { currentScreen = "packs" },
                    onWrongWordsClick = { currentScreen = "wrongWords" },
                    onSettingsClick = { currentScreen = "settings" },
                    onWordClick = { word -> currentWord = word },
                    onWordLongClick = { word -> currentWord = word }
                )
            }
            "addWord" -> {
                AddWordScreen(
                    libraries = libraries,
                    onSave = { word, meaning, example, library, level ->
                        coroutineScope.launch {
                            try {
                                repository.addWord(Word(word = word, meaning = meaning, example = example, library = library, level = level))
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                currentScreen = "main"
                            } catch (e: Exception) {
                                Log.e(TAG, "Kelime ekleme hatası", e)
                                errorMessage = "Kelime eklenemedi: ${e.message}"
                            }
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
                            try {
                                repository.updateWord(id, word, meaning, example, level)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            } catch (e: Exception) {
                                Log.e(TAG, "Kelime güncelleme hatası", e)
                                errorMessage = "Kelime güncellenemedi: ${e.message}"
                            }
                        }
                    },
                    onDeleteWord = { id ->
                        coroutineScope.launch {
                            try {
                                repository.deleteWord(id)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            } catch (e: Exception) {
                                Log.e(TAG, "Kelime silme hatası", e)
                                errorMessage = "Kelime silinemedi: ${e.message}"
                            }
                        }
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "library" -> {
                LibrarySelectScreen(
                    selectedLibrary = selectedLibrary,
                    libraries = libraries,
                    onLibrarySelected = { lib ->
                        selectedLibrary = lib
                        currentScreen = "main"
                    },
                    onManageLibraries = { },
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
                    currentGoal = 10,
                    completed = 0,
                    onSaveGoal = { target -> currentScreen = "main" },
                    onBack = { currentScreen = "main" }
                )
            }
            "stats" -> {
                StatsScreen(
                    words = words,
                    onBack = { currentScreen = "main" }
                )
            }
            "quiz" -> {
                quizSession?.let { session ->
                    QuizScreen(
                        session = session,
                        memorizationThreshold = 3,
                        onAnswerCorrect = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.updateQuizCorrectCount(question.word.id)
                                    repository.resetIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Quiz doğru işleme hatası", e)
                                }
                            }
                        },
                        onAnswerWrong = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.updateQuizWrongCount(question.word.id)
                                    repository.updateIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Quiz yanlış işleme hatası", e)
                                }
                            }
                        },
                        onMarkLearned = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.resetIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Öğrenildi işaretleme hatası", e)
                                }
                            }
                        },
                        onSpeak = { wordText -> speak(wordText) },
                        onPlayCorrectSound = { },
                        onPlayWrongSound = { },
                        isSoundMuted = isSoundMuted,
                        onToggleMute = { isSoundMuted = !isSoundMuted },
                        onRestartQuiz = {
                            coroutineScope.launch {
                                try {
                                    val wordsForQuiz = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                    if (wordsForQuiz.isNotEmpty()) {
                                        val questions = quizGenerator.generateQuestions(wordsForQuiz)
                                        if (questions.isNotEmpty()) {
                                            val session = QuizSession()
                                            session.questions = questions.toMutableList()
                                            quizSession = session
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Quiz yeniden başlatma hatası", e)
                                }
                            }
                        },
                        onBack = { currentScreen = "main" }
                    )
                }
            }
            "wrongWords" -> {
                WrongWordsScreen(
                    words = words.filter { it.wrongCount > 0 },
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
        try {
            tts.stop()
            tts.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "TTS kapatma hatası: ${e.message}")
        }
    }
}
