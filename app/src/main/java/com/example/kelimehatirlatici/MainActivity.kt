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

        LaunchedEffect(selectedLibrary, selectedLevel) {
            try {
                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                currentWordIndex = 0
                currentWord = words.firstOrNull()
                libraries = repository.getAllLibraries()
                dailyGoal = repository.getDailyGoal()
            } catch (e: Exception) {
                e.printStackTrace()
                // Hata durumunda boş değerlerle devam et
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
                                    dailyGoal = repository.getDailyGoal()
                                } catch (e: Exception) {
                                    e.printStackTrace()
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
                                    e.printStackTrace()
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
                                e.printStackTrace()
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
                                e.printStackTrace()
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
                                e.printStackTrace()
                            }
                        }
                    },
                    onDeleteWord = { id ->
                        coroutineScope.launch {
                            try {
                                repository.deleteWord(id)
                                words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
                            } catch (e: Exception) {
                                e.printStackTrace()
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
                    currentGoal = dailyGoal?.targetCount ?: 10,
                    completed = dailyGoal?.completedCount ?: 0,
                    onSaveGoal = { target ->
                        coroutineScope.launch {
                            try {
                                repository.updateDailyGoal(target)
                                dailyGoal = repository.getDailyGoal()
                                currentScreen = "main"
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
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
                                    e.printStackTrace()
                                }
                            }
                        },
                        onAnswerWrong = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.updateQuizWrongCount(question.word.id)
                                    repository.updateIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        onMarkLearned = { question ->
                            coroutineScope.launch {
                                try {
                                    repository.resetIncorrectCount(question.word.id)
                                } catch (e: Exception) {
                                    e.printStackTrace()
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
                                    e.printStackTrace()
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
        tts.stop()
        tts.shutdown()
    }
}
