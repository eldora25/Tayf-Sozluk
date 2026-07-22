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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(this)
        repository = WordRepository(database.wordDao())
        quizGenerator = QuizGenerator(repository)

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
        var dailyGoal by remember { mutableStateOf<DailyGoal?>(null) }
        var quizSession by remember { mutableStateOf<QuizSession?>(null) }
        var isSoundMuted by remember { mutableStateOf(false) }

        // Verileri yükle
        LaunchedEffect(selectedLibrary, selectedLevel) {
            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
            currentWordIndex = 0
            currentWord = words.firstOrNull()
            libraries = repository.getAllLibraries()
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
                                repository.markAsKnown(w.id)
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
                                repository.markAsWrong(w.id)
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
                    onQuizClick = {
                        coroutineScope.launch {
                            val wordsForQuiz = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            if (wordsForQuiz.isNotEmpty()) {
                                val questions = quizGenerator.generateQuestions(wordsForQuiz)
                                if (questions.isNotEmpty()) {
                                    quizSession = QuizSession(questions.map { Question(it) })
                                    currentScreen = "quiz"
                                }
                            }
                        }
                    },
                    onImportClick = { currentScreen = "import" },
                    onPacksClick = { currentScreen = "packs" },
                    onWrongWordsClick = { currentScreen = "wrongWords" },
                    onSettingsClick = { currentScreen = "settings" },
                    onWordClick = { word ->
                        // Kelimeye tıklanınca detay sayfasına git
                        currentWord = word
                    },
                    onWordLongClick = { word ->
                        // Uzun tıklamada düzenleme dialogu
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
                            repository.updateWord(id, word, meaning, example, level)
                            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                        }
                    },
                    onDeleteWord = { id ->
                        coroutineScope.launch {
                            repository.deleteWord(id)
                            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                        }
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "library" -> {
                LibrarySelectScreen(
                    libraries = libraries,
                    selectedLibrary = selectedLibrary,
                    onSelect = { lib ->
                        selectedLibrary = lib
                        currentScreen = "main"
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "level" -> {
                LevelSelectScreen(
                    selectedLevel = selectedLevel,
                    onSelect = { lvl ->
                        selectedLevel = lvl
                        currentScreen = "main"
                    },
                    onBack = { currentScreen = "main" }
                )
            }
            "goal" -> {
                GoalScreen(
                    dailyGoal = dailyGoal,
                    onSave = { target ->
                        coroutineScope.launch {
                            repository.setDailyGoal(target)
                            dailyGoal = repository.getDailyGoal()
                            currentScreen = "main"
                        }
                    },
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
                                repository.incrementQuizCorrect(question.word.id)
                            }
                        },
                        onAnswerWrong = { question ->
                            coroutineScope.launch {
                                repository.incrementQuizWrong(question.word.id)
                            }
                        },
                        onMarkLearned = { question ->
                            coroutineScope.launch {
                                repository.markAsKnown(question.word.id)
                            }
                        },
                        onSpeak = { word -> speak(word) },
                        onPlayCorrectSound = { /* TODO */ },
                        onPlayWrongSound = { /* TODO */ },
                        isSoundMuted = isSoundMuted,
                        onToggleMute = { isSoundMuted = !isSoundMuted },
                        onRestartQuiz = {
                            coroutineScope.launch {
                                val wordsForQuiz = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                if (wordsForQuiz.isNotEmpty()) {
                                    val questions = quizGenerator.generateQuestions(wordsForQuiz)
                                    if (questions.isNotEmpty()) {
                                        quizSession = QuizSession(questions.map { Question(it) })
                                    }
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
                // Bilinmeyen ekran, ana sayfaya dön
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
