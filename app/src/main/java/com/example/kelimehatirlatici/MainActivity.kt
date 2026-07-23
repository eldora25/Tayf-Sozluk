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
import org.json.JSONArray

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

        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (currentHandler !is CrashHandler) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(currentHandler))
        }

        try {
            database = AppDatabase.getDatabase(this)
            repository = WordRepository(database.wordDao())
            quizGenerator = QuizGenerator(repository)

            tts = TextToSpeech(this) { status ->
                if (status != TextToSpeech.ERROR) {
                    tts.language = Locale.US
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Başlatma hatası: ${e.message}", e)
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
        var isFlipped by remember { mutableStateOf(false) }
        var dailyGoal by remember { mutableStateOf(10) }
        var learnedToday by remember { mutableStateOf(0) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

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

        LaunchedEffect(selectedLibrary, selectedLevel) {
            try {
                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                currentWordIndex = 0
                currentWord = words.firstOrNull()
                libraries = repository.getAllLibraries()
                // Günlük hedef sayısını hesapla (wrongCount=0 olanlar öğrenilmiş)
                learnedToday = repository.getLearnedCount(selectedLibrary, selectedLevel)
                isFlipped = false
            } catch (e: Exception) {
                Log.e(TAG, "Veri yükleme hatası", e)
                errorMessage = "Veri yüklenirken hata: ${e.message}"
            }
        }

        when (currentScreen) {
            "main" -> {
                AppScreen(
                    words = words,
                    word = currentWord,
                    selectedLibrary = selectedLibrary,
                    selectedLevel = selectedLevel,
                    totalWordCount = words.size,
                    dailyGoal = dailyGoal,
                    learnedToday = learnedToday,
                    isFlipped = isFlipped,
                    memorizationThreshold = 3,
                    onKnownClick = {
                        currentWord?.let { w ->
                            coroutineScope.launch {
                                try {
                                    repository.resetIncorrectCount(w.id)
                                    words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                    learnedToday = repository.getLearnedCount(selectedLibrary, selectedLevel)
                                    currentWordIndex = (currentWordIndex + 1) % words.size
                                    currentWord = words.getOrNull(currentWordIndex)
                                    isFlipped = false
                                } catch (e: Exception) {
                                    Log.e(TAG, "KnownClick hatası", e)
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
                                    isFlipped = false
                                } catch (e: Exception) {
                                    Log.e(TAG, "WrongClick hatası", e)
                                }
                            }
                        }
                    },
                    onFlip = { isFlipped = !isFlipped },
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
                            }
                        }
                    },
                    onImportClick = { currentScreen = "import" },
                    onPacksClick = { currentScreen = "packs" },
                    onWrongWordsClick = { currentScreen = "wrongWords" },
                    onSettingsClick = { currentScreen = "settings" },
                    onWordClick = { word -> currentWord = word },
                    onWordLongClick = { word -> currentWord = word },
                    onWordEdit = { word ->
                        // Edit ekranına git
                        currentScreen = "wordEdit_${word.id}"
                    }
                )
            }
            "addWord" -> {
                AddWordScreen(
                    libraries = libraries,
                    onSave = { word, meaning, meanings, example, examples, library, level ->
                        coroutineScope.launch {
                            try {
                                val meaningsJson = JSONArray(meanings).toString()
                                val examplesJson = JSONArray(examples).toString()
                                repository.addWord(
                                    Word(
                                        word = word,
                                        meaning = meaning,
                                        meanings = meaningsJson,
                                        example = example,
                                        examples = examplesJson,
                                        library = library,
                                        level = level
                                    )
                                )
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
                    onUpdateWord = { id, word, meaning, meanings, example, examples, level, library ->
                        coroutineScope.launch {
                            try {
                                repository.updateWord(id, word, meaning, meanings, example, examples, level, library)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            } catch (e: Exception) {
                                Log.e(TAG, "Kelime güncelleme hatası", e)
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
                            }
                        }
                    },
                    onMoveWord = { id, newLibrary ->
                        coroutineScope.launch {
                            try {
                                repository.moveWord(id, newLibrary)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            } catch (e: Exception) {
                                Log.e(TAG, "Kelime taşıma hatası", e)
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
                    currentGoal = dailyGoal,
                    completed = learnedToday,
                    onSaveGoal = { target ->
                        dailyGoal = target
                        currentScreen = "main"
                    },
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
                                    Log.e(TAG, "Quiz doğru hatası", e)
                                }
                            }
                        },
                        onAnswerWrong = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.updateQuizWrongCount(question.word.id)
                                    repository.updateIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Quiz yanlış hatası", e)
                                }
                            }
                        },
                        onMarkLearned = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.resetIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Öğrenildi hatası", e)
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
                                    Log.e(TAG, "Quiz restart hatası", e)
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
                // WordEdit ekranı kontrolü
                if (currentScreen.startsWith("wordEdit_")) {
                    val wordId = currentScreen.removePrefix("wordEdit_").toIntOrNull()
                    val editWord = words.find { it.id == wordId }
                    if (editWord != null) {
                        WordEditScreen(
                            word = editWord,
                            libraries = libraries,
                            onSave = { word, meaning, meanings, example, examples, level, library ->
                                coroutineScope.launch {
                                    try {
                                        val meaningsJson = JSONArray(meanings).toString()
                                        val examplesJson = JSONArray(examples).toString()
                                        repository.updateWord(
                                            editWord.id, word, meaning, meaningsJson,
                                            example, examplesJson, level, library
                                        )
                                        words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                        currentWord = words.find { it.id == editWord.id }
                                        currentScreen = "main"
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Kelime güncelleme hatası", e)
                                    }
                                }
                            },
                            onAction = { action, newLibrary ->
                                coroutineScope.launch {
                                    try {
                                        when (action) {
                                            WordEditAction.UPDATE -> { /* onSave ile yapılır */ }
                                            WordEditAction.COPY -> {
                                                val newWord = editWord.copy(id = 0, library = newLibrary ?: editWord.library)
                                                repository.addWord(newWord)
                                            }
                                            WordEditAction.MOVE -> {
                                                val targetLib = newLibrary ?: selectedLibrary
                                                repository.moveWord(editWord.id, targetLib)
                                                selectedLibrary = targetLib
                                            }
                                            WordEditAction.DELETE -> {
                                                repository.deleteWord(editWord.id)
                                            }
                                        }
                                        words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                                        currentWord = null
                                        currentWordIndex = 0
                                        if (words.isNotEmpty()) {
                                            currentWord = words.first()
                                        }
                                        currentScreen = "main"
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Kelime işlem hatası", e)
                                    }
                                }
                            },
                            onBack = { currentScreen = "main" }
                        )
                    } else {
                        currentScreen = "main"
                    }
                } else {
                    currentScreen = "main"
                }
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
