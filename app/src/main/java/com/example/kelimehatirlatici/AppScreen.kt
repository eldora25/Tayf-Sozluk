package com.example.kelimehatirlatici

import android.content.Context
import android.net.Uri
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
import com.example.kelimehatirlatici.quiz.QuizGenerator
import com.example.kelimehatirlatici.quiz.QuizQuestion
import com.example.kelimehatirlatici.quiz.QuizScreen
import com.example.kelimehatirlatici.ui.*
import kotlinx.coroutines.launch

@Composable
fun AppScreen(
    repository: WordRepository,
    context: Context,
    onSpeak: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("learning") }
    var selectedLibrary by remember { mutableStateOf("İngilizce A1") }
    var selectedLevel by remember { mutableStateOf("A1") }

    var words by remember { mutableStateOf<List<Word>>(emptyList()) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }
    var libraryInfoList by remember { mutableStateOf<List<LibraryInfo>>(emptyList()) }
    var dailyGoal by remember { mutableStateOf<DailyGoal?>(null) }
    var stats by remember { mutableStateOf(emptyList<com.example.kelimehatirlatici.data.StudyStats>()) }
    var packs by remember { mutableStateOf<List<WordPack>>(emptyList()) }
    var wrongWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var quizQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var totalWordCount by remember { mutableStateOf(0) }

    val quizGenerator = remember { QuizGenerator(repository) }

    // ──────── BEKLENEN İÇE AKTARMA KÜTÜPHANE ADI ────────

    var pendingLibraryName by remember { mutableStateOf("") }

    fun refreshData() {
        scope.launch {
            words = repository.getWordsByLibraryAndLevel(selectedLibrary, selectedLevel)
            currentWord = repository.getNextWord(selectedLibrary, selectedLevel)
            libraries = repository.getLibraries()
            libraryInfoList = repository.getLibraryInfoList()
            dailyGoal = repository.getTodayGoal()
            stats = repository.getStats()
            packs = WordPackReader.readAllPacks(context)
            wrongWords = repository.getWrongWords()
            totalWordCount = repository.getTotalCount(selectedLibrary)
        }
    }

    LaunchedEffect(selectedLibrary, selectedLevel) {
        refreshData()
    }

    // ──────── İÇE AKTARMA LAUNCHER'LARI ────────

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val importedWords = CsvImportHelper.importFromCsv(context, uri)
                // CSV'de library alanı varsa onu kullan, yoksa kullanıcının girdiği adı kullan
                val libName = importedWords.firstOrNull()?.library
                    ?.takeIf { it != "İçe Aktarılan CSV" }
                    ?: pendingLibraryName.ifBlank { "İçe Aktarılan CSV" }

                val finalWords = importedWords.map {
                    it.copy(library = libName)
                }
                repository.addWords(finalWords)
                selectedLibrary = libName
                selectedLevel = finalWords.firstOrNull()?.level ?: "Genel"
                pendingLibraryName = ""
                refreshData()
                currentScreen = "learning"
            }
        }
    }

    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val importedWords = ExcelImportHelper.importFromExcel(context, uri)
                val libName = importedWords.firstOrNull()?.library
                    ?.takeIf { it != "İçe Aktarılan Excel" }
                    ?: pendingLibraryName.ifBlank { "İçe Aktarılan Excel" }

                val finalWords = importedWords.map {
                    it.copy(library = libName)
                }
                repository.addWords(finalWords)
                selectedLibrary = libName
                selectedLevel = finalWords.firstOrNull()?.level ?: "Genel"
                pendingLibraryName = ""
                refreshData()
                currentScreen = "learning"
            }
        }
    }

    val lingoesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val importedWords = LingoesImportHelper.importFromLingoesText(context, uri)
                val libName = pendingLibraryName.ifBlank { "Lingoes TXT" }

                val finalWords = importedWords.map {
                    it.copy(library = libName)
                }
                repository.addWords(finalWords)
                selectedLibrary = libName
                selectedLevel = "Genel"
                pendingLibraryName = ""
                refreshData()
                currentScreen = "learning"
            }
        }
    }

    // ──────── EKRAN YÖNLENDİRME ────────

    when (currentScreen) {
        "learning" -> LearningCardScreen(
            word = currentWord,
            selectedLibrary = selectedLibrary,
            selectedLevel = selectedLevel,
            dailyGoal = dailyGoal,
            totalWordCount = totalWordCount,
            onKnownClick = {
                scope.launch {
                    currentWord?.let {
                        repository.markKnown(it)
                        repository.increaseTodayCompleted()
                    }
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
            onAddWordClick = { currentScreen = "add" },
            onWordListClick = {
                refreshData()
                currentScreen = "list"
            },
            onLibraryClick = {
                refreshData()
                currentScreen = "library"
            },
            onLevelClick = { currentScreen = "level" },
            onGoalClick = {
                refreshData()
                currentScreen = "goal"
            },
            onStatsClick = {
                refreshData()
                currentScreen = "stats"
            },
            onQuizClick = {
                scope.launch {
                    val word = repository.getNextWord(selectedLibrary, selectedLevel)
                    quizQuestion = if (word != null) quizGenerator.generateQuestion(word) else null
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
            }
        )

        "add" -> AddWordScreen(
            onSave = { word, meaning, example, library, level ->
                scope.launch {
                    repository.addWord(
                        Word(word = word, meaning = meaning, example = example, library = library, level = level)
                    )
                    selectedLibrary = library
                    selectedLevel = level
                    refreshData()
                    currentScreen = "learning"
                }
            },
            onBack = { currentScreen = "learning" }
        )

        "list" -> WordListScreen(
            words = words,
            onBack = { currentScreen = "learning" }
        )

        "library" -> LibrarySelectScreen(
            libraryInfoList = libraryInfoList,
            selectedLibrary = selectedLibrary,
            onLibrarySelected = {
                selectedLibrary = it
                currentScreen = "learning"
            },
            onManageLibraries = {
                refreshData()
                currentScreen = "manageLibraries"
            },
            onBack = { currentScreen = "learning" }
        )

        "manageLibraries" -> LibraryManageScreen(
            libraryInfoList = libraryInfoList,
            onDeleteLibrary = { lib ->
                scope.launch {
                    repository.deleteLibrary(lib)
                    if (selectedLibrary == lib) {
                        selectedLibrary = "İngilizce A1"
                    }
                    refreshData()
                }
            },
            onRenameLibrary = { oldName, newName ->
                scope.launch {
                    repository.renameLibrary(oldName, newName)
                    if (selectedLibrary == oldName) {
                        selectedLibrary = newName
                    }
                    refreshData()
                }
            },
            onBack = { currentScreen = "library" }
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
            stats = stats,
            onBack = { currentScreen = "learning" }
        )

        "quiz" -> QuizScreen(
            question = quizQuestion,
            onAnswerSelected = { correct ->
                scope.launch {
                    quizQuestion?.word?.let { repository.recordQuizResult(it, correct) }
                }
            },
            onNextQuestion = {
                scope.launch {
                    val word = repository.getNextWord(selectedLibrary, selectedLevel)
                    quizQuestion = if (word != null) quizGenerator.generateQuestion(word) else null
                }
            },
            onBack = { currentScreen = "learning" }
        )

        "import" -> ImportScreen(
            onCsvImportClick = { libraryName ->
                pendingLibraryName = libraryName
                csvLauncher.launch("text/*")
            },
            onExcelImportClick = { libraryName ->
                pendingLibraryName = libraryName
                excelLauncher.launch("*/*")
            },
            onLingoesImportClick = { libraryName ->
                pendingLibraryName = libraryName
                lingoesLauncher.launch("text/*")
            },
            onBack = { currentScreen = "learning" }
        )

        "packs" -> WordPackScreen(
            packs = packs,
            onInstallPack = { pack ->
                scope.launch {
                    val wordsToInstall = pack.words.map { item ->
                        Word(
                            word = item.word,
                            meaning = item.meaning,
                            example = item.example,
                            library = pack.name,
                            level = pack.level
                        )
                    }
                    repository.addWords(wordsToInstall)
                    selectedLibrary = pack.name
                    selectedLevel = pack.level
                    refreshData()
                    currentScreen = "learning"
                }
            },
            onInstallAll = {
                scope.launch {
                    packs.forEach { pack ->
                        val wordsToInstall = pack.words.map { item ->
                            Word(
                                word = item.word,
                                meaning = item.meaning,
                                example = item.example,
                                library = pack.name,
                                level = pack.level
                            )
                        }
                        repository.addWords(wordsToInstall)
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
    }
}
