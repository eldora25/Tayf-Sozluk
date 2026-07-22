package com.example.kelimehatirlatici

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.importer.CsvImportHelper
import com.example.kelimehatirlatici.importer.ExcelImportHelper
import com.example.kelimehatirlatici.importer.LingoesImportHelper
import com.example.kelimehatirlatici.packs.WordPack
import com.example.kelimehatirlatici.packs.WordPackReader
import com.example.kelimehatirlatici.quiz.Question
import com.example.kelimehatirlatici.quiz.QuizGenerator
import com.example.kelimehatirlatici.quiz.QuizScreen
import com.example.kelimehatirlatici.quiz.QuizSession
import com.example.kelimehatirlatici.ui.*
import kotlinx.coroutines.launch

@Composable
fun AppScreen(
    repository: WordRepository,
    settings: AppSettings,
    context: Context,
    onSpeak: (String) -> Unit,
    soundManager: SoundManager,
    onDarkModeChange: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("learning") }

    var selectedLibrary by remember { mutableStateOf(settings.selectedLibrary) }
    var selectedLevel by remember { mutableStateOf(settings.selectedLevel) }

    var words by remember { mutableStateOf<List<Word>>(emptyList()) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }
    var libraryInfoList by remember { mutableStateOf<List<LibraryInfo>>(emptyList()) }
    var dailyGoal by remember { mutableStateOf<DailyGoal?>(null) }
    var packs by remember { mutableStateOf<List<WordPack>>(emptyList()) }
    var wrongWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var totalWordCount by remember { mutableIntStateOf(0) }
    var totalLibraries by remember { mutableIntStateOf(0) }
    var totalWordsOverall by remember { mutableIntStateOf(0) }
    var totalLearnedOverall by remember { mutableIntStateOf(0) }

    val quizGenerator = remember { QuizGenerator(repository) }
    val quizSession = remember { QuizSession() }

    var quizQuestionCount by remember { mutableIntStateOf(settings.quizQuestionCount) }
    var randomOrder by remember { mutableStateOf(settings.randomOrder) }
    var memorizationThreshold by remember { mutableIntStateOf(settings.memorizationThreshold) }
    var darkMode by remember { mutableStateOf(settings.darkMode) }

    var pendingLibraryName by remember { mutableStateOf("") }
    var exportLibraryName by remember { mutableStateOf("") }

    fun refreshData() {
        scope.launch {
            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
            currentWord = repository.getNextWord(selectedLibrary, selectedLevel)
            libraries = repository.getLibraries()
            libraryInfoList = repository.getLibraryInfoList()
            dailyGoal = repository.getTodayGoal()
            packs = WordPackReader.readAllPacks(context)
            wrongWords = repository.getWrongWords()
            totalWordCount = repository.getTotalCount(selectedLibrary)
            totalLibraries = repository.getTotalLibraryCount()
            totalWordsOverall = repository.getTotalWordCount()
            totalLearnedOverall = repository.getTotalLearnedCount()
        }
    }

    LaunchedEffect(selectedLibrary, selectedLevel) { refreshData() }
    LaunchedEffect(selectedLibrary) { settings.selectedLibrary = selectedLibrary }
    LaunchedEffect(selectedLevel) { settings.selectedLevel = selectedLevel }

    // Dosya seçiciler
    val csvUri = remember { mutableStateOf<Uri?>(null) }
    val excelUri = remember { mutableStateOf<Uri?>(null) }
    val lingoesUri = remember { mutableStateOf<Uri?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        csvUri.value = uri
        if (uri != null) scope.launch {
            val imported = CsvImportHelper.importFromCsv(context, uri)
            val libName = pendingLibraryName.ifBlank { "İçe Aktarılan CSV" }
            repository.addWords(imported.map { it.copy(library = libName) })
            selectedLibrary = libName
            pendingLibraryName = ""
            refreshData()
            currentScreen = "learning"
        }
    }

    val excelLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        excelUri.value = uri
        if (uri != null) scope.launch {
            val imported = ExcelImportHelper.importFromExcel(context, uri)
            val libName = pendingLibraryName.ifBlank { "İçe Aktarılan Excel" }
            repository.addWords(imported.map { it.copy(library = libName) })
            selectedLibrary = libName
            pendingLibraryName = ""
            refreshData()
            currentScreen = "learning"
        }
    }

    val lingoesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        lingoesUri.value = uri
        if (uri != null) scope.launch {
            val imported = LingoesImportHelper.importFromLingoesText(context, uri)
            val libName = pendingLibraryName.ifBlank { "Lingoes TXT" }
            repository.addWords(imported.map { it.copy(library = libName) })
            selectedLibrary = libName
            pendingLibraryName = ""
            refreshData()
            currentScreen = "learning"
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            try {
                val csv = repository.exportLibraryAsCsv(exportLibraryName)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "✅ \"$exportLibraryName\" dışa aktarıldı", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "❌ Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            refreshData()
        }
    }

    when (currentScreen) {
        "learning" -> LearningCardScreen(
            word = currentWord,
            selectedLibrary = selectedLibrary,
            selectedLevel = selectedLevel,
            dailyGoal = dailyGoal,
            totalWordCount = totalWordCount,
            onKnownClick = {
                scope.launch {
                    currentWord?.let { repository.markKnown(it); repository.increaseTodayCompleted() }
                    refreshData()
                }
            },
            onWrongClick = {
                scope.launch {
                    currentWord?.let { repository.markWrong(it) }
                    refreshData()
                }
            },
            onSpeakClick = onSpeak,
            onAddWordClick = { refreshData(); currentScreen = "add" },
            onWordListClick = { refreshData(); currentScreen = "list" },
            onLibraryClick = { refreshData(); currentScreen = "library" },
            onLevelClick = { currentScreen = "level" },
            onGoalClick = { refreshData(); currentScreen = "goal" },
            onStatsClick = { refreshData(); currentScreen = "stats" },
            onQuizClick = {
                scope.launch {
                    val unlearned = repository.getUnlearnedWords(selectedLibrary, randomOrder)
                    val quizQuestions = quizGenerator.generateQuestions(unlearned)
                    val questions = quizQuestions.map { q ->
                        Question(
                            word = q.word,
                            options = q.options,
                            correctAnswer = q.correctAnswer
                        )
                    }
                    quizSession.start(questions, quizQuestionCount)
                    currentScreen = "quiz"
                }
            },
            onImportClick = { currentScreen = "import" },
            onPacksClick = {
                packs = WordPackReader.readAllPacks(context)
                currentScreen = "packs"
            },
            onWrongWordsClick = {
                scope.launch {
                    wrongWords = repository.getWrongWords()
                    currentScreen = "wrong"
                }
            },
            onSettingsClick = { currentScreen = "settings" }
        )

        // ★ ★ ★ GÜNCELLENDİ: WordListScreen çağrısı ★ ★ ★
        "list" -> WordListScreen(
            words = words,
            onUpdateWord = { id, newWord, newMeaning, newExample, newLevel ->
                scope.launch {
                    // Kelimenin mevcut kütüphanesini koru
                    val existing = words.find { it.id == id }
                    val library = existing?.library ?: selectedLibrary
                    val finalLevel = if (newLevel.isBlank()) "Genel" else newLevel
                    repository.updateWordFull(id, newWord, newMeaning, newExample, finalLevel, library)
                    refreshData()
                }
            },
            onBack = { currentScreen = "learning" }
        )

        "add" -> AddWordScreen(
            libraries = libraries,
            onSave = { w, m, e, lib, lvl ->
                scope.launch {
                    val isDup = repository.isWordDuplicate(lib, w)
                    if (isDup) {
                        Toast.makeText(context, "⚠️ \"$w\" zaten \"$lib\" kütüphanesinde var!", Toast.LENGTH_LONG).show()
                    } else {
                        repository.addWord(Word(word = w, meaning = m, example = e, library = lib, level = lvl))
                        selectedLibrary = lib
                        selectedLevel = lvl
                        refreshData()
                        currentScreen = "learning"
                    }
                }
            },
            onBack = { currentScreen = "learning" }
        )

        "library" -> LibrarySelectScreen(
            libraryInfoList = libraryInfoList,
            selectedLibrary = selectedLibrary,
            onLibrarySelected = {
                selectedLibrary = it
                currentScreen = "learning"
            },
            onManageLibraries = { refreshData(); currentScreen = "manageLibraries" },
            onBack = { currentScreen = "learning" }
        )

        "manageLibraries" -> LibraryManageScreen(
            libraryInfoList = libraryInfoList,
            onDeleteLibrary = { lib ->
                scope.launch {
                    repository.deleteLibrary(lib)
                    if (selectedLibrary == lib) selectedLibrary = "İngilizce A1"
                    refreshData()
                }
            },
            onRenameLibrary = { old, new ->
                scope.launch {
                    repository.renameLibrary(old, new)
                    if (selectedLibrary == old) selectedLibrary = new
                    refreshData()
                }
            },
            onExportLibrary = { lib ->
                exportLibraryName = lib
                exportLauncher.launch("${lib}.csv")
            },
            onBack = { refreshData(); currentScreen = "library" }
        )

        "level" -> LevelSelectScreen(
            selectedLevel = selectedLevel,
            onLevelSelected = {
                selectedLevel = it
                currentScreen = "learning"
            },
            onBack = { currentScreen = "learning" }
        )

        "goal" -> GoalScreen(
            currentGoal = dailyGoal?.targetCount ?: 10,
            completed = dailyGoal?.completedCount ?: 0,
            onSaveGoal = {
                scope.launch {
                    repository.setTodayGoal(it)
                    refreshData()
                    currentScreen = "learning"
                }
            },
            onBack = { currentScreen = "learning" }
        )

        "stats" -> StatsScreen(
            libraryInfoList = libraryInfoList,
            totalLibraries = totalLibraries,
            totalWords = totalWordsOverall,
            totalLearned = totalLearnedOverall,
            onBack = { currentScreen = "learning" }
        )

        "quiz" -> QuizScreen(
            session = quizSession,
            memorizationThreshold = memorizationThreshold,
            onAnswerCorrect = { question: Question ->
                scope.launch {
                    repository.incrementQuizCorrect(question.word)
                    repository.recordQuizStat(true)
                }
            },
            onAnswerWrong = { question: Question ->
                scope.launch {
                    repository.recordQuizWrong(question.word)
                    repository.recordQuizStat(false)
                }
            },
            onMarkLearned = { question: Question ->
                scope.launch {
                    repository.markAsLearned(question.word)
                }
            },
            onSpeak = onSpeak,
            onPlayCorrectSound = { soundManager.playCorrect() },
            onPlayWrongSound = { soundManager.playWrong() },
            isSoundMuted = soundManager.isMuted,
            onToggleMute = {
                soundManager.isMuted = !soundManager.isMuted
            },
            onRestartQuiz = {
                // Aynı ayarlarla yeni quiz başlat
                scope.launch {
                    val unlearned = repository.getUnlearnedWords(selectedLibrary, randomOrder)
                    val quizQuestions = quizGenerator.generateQuestions(unlearned)
                    val questions = quizQuestions.map { q ->
                        Question(
                            word = q.word,
                            options = q.options,
                            correctAnswer = q.correctAnswer
                        )
                    }
                    quizSession.start(questions, quizQuestionCount)
                }
            },
            onBack = {
                quizSession.isRunning = false
                currentScreen = "learning"
                refreshData()
            }
        )

        "import" -> ImportScreen(
            onCsvImportClick = { name -> pendingLibraryName = name; csvLauncher.launch("text/*") },
            onExcelImportClick = { name -> pendingLibraryName = name; excelLauncher.launch("*/*") },
            onLingoesImportClick = { name -> pendingLibraryName = name; lingoesLauncher.launch("text/*") },
            onBack = { currentScreen = "learning" }
        )

        "packs" -> WordPackScreen(
            packs = packs,
            onInstallPack = { pack ->
                scope.launch {
                    repository.addWords(
                        pack.words.map {
                            Word(
                                word = it.word,
                                meaning = it.meaning,
                                example = it.example,
                                library = pack.name,
                                level = pack.level
                            )
                        }
                    )
                    selectedLibrary = pack.name
                    selectedLevel = pack.level
                    refreshData()
                    currentScreen = "learning"
                }
            },
            onInstallAll = {
                scope.launch {
                    packs.forEach { pack ->
                        repository.addWords(
                            pack.words.map {
                                Word(
                                    word = it.word,
                                    meaning = it.meaning,
                                    example = it.example,
                                    library = pack.name,
                                    level = pack.level
                                )
                            }
                        )
                    }
                    refreshData()
                    currentScreen = "learning"
                }
            },
            onBack = { currentScreen = "learning" }
        )

        "wrong" -> WrongWordsScreen(
            words = wrongWords,
            onBack = { currentScreen = "learning" }
        )

        "settings" -> SettingsScreen(
            quizQuestionCount = quizQuestionCount,
            randomOrder = randomOrder,
            memorizationThreshold = memorizationThreshold,
            darkMode = darkMode,
            onQuizQuestionCountChange = {
                quizQuestionCount = it
                settings.quizQuestionCount = it
            },
            onRandomOrderChange = {
                randomOrder = it
                settings.randomOrder = it
            },
            onMemorizationThresholdChange = {
                memorizationThreshold = it
                settings.memorizationThreshold = it
            },
            onDarkModeChange = {
                darkMode = it
                settings.darkMode = it
                onDarkModeChange(it)
            },
            onBack = { currentScreen = "learning" }
        )
    }
}
