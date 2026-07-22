package com.example.kelimehatirlatici

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.WordDao
import com.example.kelimehatirlatici.quiz.QuizScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    appSettings: AppSettings,
    wordRepository: WordRepository,
    ttsManager: TtsManager,
    soundManager: SoundManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val wordDao = remember { AppDatabase.getInstance(context).wordDao() }

    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedLibrary by remember { mutableStateOf(appSettings.selectedLibrary) }
    var words by remember { mutableStateOf<List<Word>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Word?>(null) }
    var showQuiz by remember { mutableStateOf(false) }
    var showLibrarySelect by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf(appSettings.selectedLevel) }
    var showLevelFilter by remember { mutableStateOf(false) }

    // Yeni kelime ekleme dialog state
    var newWord by remember { mutableStateOf("") }
    var newMeaning by remember { mutableStateOf("") }
    var newExample by remember { mutableStateOf("") }
    var newDetail by remember { mutableStateOf("") }

    // İstatistikler
    var totalWords by remember { mutableStateOf(0) }
    var learnedWords by remember { mutableStateOf(0) }
    var todayLearned by remember { mutableStateOf(0) }
    var dailyGoal by remember { mutableStateOf(20) }

    // Dosya içe aktarma
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val lines = reader.readLines()
                    reader.close()

                    var importedCount = 0
                    for (line in lines) {
                        val parts = line.split("\t")
                        if (parts.size >= 2) {
                            val word = parts[0].trim()
                            val meaning = parts[1].trim()
                            val example = if (parts.size >= 3) parts[2].trim() else ""
                            val detail = if (parts.size >= 4) parts[3].trim() else ""

                            if (word.isNotEmpty() && meaning.isNotEmpty()) {
                                val existingWords = wordRepository.getWordsByLibrary(selectedLibrary)
                                val isDuplicate = existingWords.any { it.word.lowercase() == word.lowercase() }

                                if (!isDuplicate) {
                                    wordRepository.addWord(
                                        Word(
                                            word = word,
                                            meaning = meaning,
                                            example = example,
                                            detail = detail,
                                            library = selectedLibrary,
                                            level = selectedLevel
                                        )
                                    )
                                    importedCount++
                                }
                            }
                        }
                    }

                    Toast.makeText(context, "$importedCount kelime içe aktarıldı", Toast.LENGTH_LONG).show()
                    loadData()
                } catch (e: Exception) {
                    Toast.makeText(context, "İçe aktarma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun loadData() {
        coroutineScope.launch {
            libraries = wordRepository.getAllLibraries()
            if (selectedLibrary !in libraries && libraries.isNotEmpty()) {
                selectedLibrary = libraries[0]
                appSettings.selectedLibrary = selectedLibrary
            }

            val allWords = if (searchQuery.isNotEmpty()) {
                wordRepository.searchWords(searchQuery, selectedLibrary)
            } else {
                wordRepository.getWordsByLibrary(selectedLibrary)
            }

            words = if (selectedLevel == "Tümü") {
                allWords
            } else {
                allWords.filter { it.level == selectedLevel }
            }

            totalWords = allWords.size
            learnedWords = allWords.count { it.isLearned }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Günlük hedef
            coroutineScope.launch {
                try {
                    val stats = wordDao.getStudyStatsByDate(today)
                    if (stats != null) {
                        todayLearned = stats.learnedCount
                    }
                } catch (e: Exception) {
                    todayLearned = 0
                }
            }
        }
    }

    LaunchedEffect(selectedLibrary, searchQuery, selectedLevel) {
        loadData()
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    if (showQuiz) {
        QuizScreen(
            kelimeListesi = words,
            maxQuestions = appSettings.quizQuestionCount,
            appSettings = appSettings,
            wordDao = wordDao,
            onFinish = { showQuiz = false }
        )
    } else if (showLibrarySelect) {
        LibrarySelectScreen(
            libraries = libraries,
            selectedLibrary = selectedLibrary,
            onLibrarySelected = { library ->
                selectedLibrary = library
                appSettings.selectedLibrary = library
                showLibrarySelect = false
            },
            onBack = { showLibrarySelect = false },
            wordRepository = wordRepository,
            onLibrariesChanged = { loadData() }
        )
    } else if (showSettings) {
        // Ayarlar ekranı
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Ayarlar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Quiz soru sayısı
            Text("Quiz Soru Sayısı: ${appSettings.quizQuestionCount}", fontSize = 16.sp)
            Slider(
                value = appSettings.quizQuestionCount.toFloat(),
                onValueChange = {
                    appSettings.quizQuestionCount = it.toInt()
                },
                valueRange = 5f..50f,
                steps = 8
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Karıştırma
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Soruları Karıştır", fontSize = 16.sp)
                Switch(
                    checked = appSettings.shuffleQuestions,
                    onCheckedChange = { appSettings.shuffleQuestions = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ezber eşiği
            Text("Ezber Eşiği: ${appSettings.memorizationThreshold}", fontSize = 16.sp)
            Slider(
                value = appSettings.memorizationThreshold.toFloat(),
                onValueChange = {
                    appSettings.memorizationThreshold = it.toInt()
                },
                valueRange = 1f..10f,
                steps = 8
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dark mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Karanlık Tema", fontSize = 16.sp)
                Switch(
                    checked = appSettings.darkMode,
                    onCheckedChange = { appSettings.darkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showSettings = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kapat")
            }
        }
    } else {
        // ---------- ANA EKRAN ----------
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tayf Sözlük",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showLibrarySelect = true }) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = "Kütüphaneler")
                        }
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Kelime Ekle")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Kütüphane seçimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kütüphane: $selectedLibrary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Seviye filtresi
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Seviye: $selectedLevel",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.clickable { showLevelFilter = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // İstatistik kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalWords", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Toplam", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$learnedWords", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Text("Öğrenilen", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$todayLearned", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                            Text("Bugün", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Arama çubuğu
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Kelime ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quiz ve İçe Aktarma butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showQuiz = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quiz", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            importLauncher.launch("text/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("İçe Aktar")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val file = java.io.File(
                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                        "tayf_sozluk_${selectedLibrary}_${System.currentTimeMillis()}.csv"
                                    )
                                    val csvContent = withContext(Dispatchers.IO) {
                                        wordRepository.exportLibraryToCsv(selectedLibrary)
                                    }
                                    file.writeText(csvContent)
                                    Toast.makeText(context, "Dışa aktarıldı: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Dışa aktarma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dışa Aktar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seviye filtresi dropdown
                if (showLevelFilter) {
                    val levels = listOf("Tümü", "A1", "A2", "B1", "B2", "C1", "C2")
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            levels.forEach { level ->
                                Text(
                                    text = level,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLevel = level
                                            appSettings.selectedLevel = level
                                            showLevelFilter = false
                                        }
                                        .padding(12.dp),
                                    fontWeight = if (level == selectedLevel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (level == selectedLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Kelime listesi
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(words) { word ->
                        WordCard(
                            word = word,
                            onEdit = { showEditDialog = word },
                            onDelete = {
                                coroutineScope.launch {
                                    wordRepository.deleteWord(word)
                                    loadData()
                                }
                            },
                            onToggleLearned = {
                                coroutineScope.launch {
                                    wordRepository.markAsLearned(word.id, !word.isLearned)
                                    loadData()
                                }
                            },
                            onSpeak = {
                                ttsManager.speak(word.word)
                            }
                        )
                    }
                }
            }
        }
    }

    // Kelime ekleme dialogu
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Kelime Ekle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newWord,
                        onValueChange = { newWord = it },
                        label = { Text("Kelime") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newMeaning,
                        onValueChange = { newMeaning = it },
                        label = { Text("Anlamı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newExample,
                        onValueChange = { newExample = it },
                        label = { Text("Örnek Cümle (isteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDetail,
                        onValueChange = { newDetail = it },
                        label = { Text("Detay (isteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWord.isNotBlank() && newMeaning.isNotBlank()) {
                            coroutineScope.launch {
                                wordRepository.addWord(
                                    Word(
                                        word = newWord.trim(),
                                        meaning = newMeaning.trim(),
                                        example = newExample.trim().ifEmpty { null },
                                        detail = newDetail.trim(),
                                        library = selectedLibrary,
                                        level = selectedLevel
                                    )
                                )
                                newWord = ""
                                newMeaning = ""
                                newExample = ""
                                newDetail = ""
                                showAddDialog = false
                                loadData()
                            }
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddDialog = false
                    newWord = ""
                    newMeaning = ""
                    newExample = ""
                    newDetail = ""
                }) {
                    Text("İptal")
                }
            }
        )
    }

    // Kelime düzenleme dialogu
    showEditDialog?.let { word ->
        var editWord by remember { mutableStateOf(word.word) }
        var editMeaning by remember { mutableStateOf(word.meaning) }
        var editExample by remember { mutableStateOf(word.example ?: "") }
        var editDetail by remember { mutableStateOf(word.detail) }

        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Kelimeyi Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editWord,
                        onValueChange = { editWord = it },
                        label = { Text("Kelime") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editMeaning,
                        onValueChange = { editMeaning = it },
                        label = { Text("Anlamı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editExample,
                        onValueChange = { editExample = it },
                        label = { Text("Örnek Cümle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDetail,
                        onValueChange = { editDetail = it },
                        label = { Text("Detay") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            wordRepository.updateWord(
                                word.copy(
                                    word = editWord.trim(),
                                    meaning = editMeaning.trim(),
                                    example = editExample.trim().ifEmpty { null },
                                    detail = editDetail.trim()
                                )
                            )
                            showEditDialog = null
                            loadData()
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun WordCard(
    word: Word,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleLearned: () -> Unit,
    onSpeak: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Öğrenme durumu
            Checkbox(
                checked = word.isLearned,
                onCheckedChange = { onToggleLearned() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50)
                )
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = word.word,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (word.isLearned) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = word.meaning,
                    fontSize = 14.sp,
                    color = if (word.isLearned) Color.LightGray else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (word.detail.isNotEmpty()) {
                    Text(
                        text = word.detail,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    Text(
                        text = "[${word.level}]",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✓${word.quizCorrectCount}",
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✗${word.wrongCount}",
                        fontSize = 11.sp,
                        color = Color(0xFFF44336)
                    )
                }
            }

            // Seslendirme butonu
            IconButton(onClick = onSpeak) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Seslendir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Düzenle butonu
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Düzenle",
                    tint = Color(0xFFFF9800)
                )
            }

            // Sil butonu
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color(0xFFF44336)
                )
            }
        }
    }
}
