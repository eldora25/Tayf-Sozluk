package com.example.kelimehatirlatici

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.ui.ImportScreen
import com.example.kelimehatirlatici.ui.theme.KelimeHatirlaticiTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// ========== DATASTORE ==========
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

// Anahtarlar
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
private val SELECTED_LIBRARY_KEY = stringPreferencesKey("selected_library")
private val SELECTED_LEVEL_KEY = stringPreferencesKey("selected_level")
private val LEARNED_WORDS_KEY = stringSetPreferencesKey("learned_words")
private val WRONG_WORDS_KEY = stringSetPreferencesKey("wrong_words")
private val REPEAT_WORDS_KEY = stringSetPreferencesKey("repeat_words")

// Dark mode
fun Context.getDarkModeFlow(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }
}
suspend fun Context.setDarkModeEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[DARK_MODE_KEY] = enabled
    }
}

// Seçili kütüphane
suspend fun Context.setSelectedLibrary(library: String) {
    dataStore.edit { preferences ->
        preferences[SELECTED_LIBRARY_KEY] = library
    }
}
fun Context.getSelectedLibraryFlow(): Flow<String> {
    return dataStore.data.map { preferences ->
        preferences[SELECTED_LIBRARY_KEY] ?: "Tümü"
    }
}

// Seçili seviye
suspend fun Context.setSelectedLevel(level: String) {
    dataStore.edit { preferences ->
        preferences[SELECTED_LEVEL_KEY] = level
    }
}
fun Context.getSelectedLevelFlow(): Flow<String> {
    return dataStore.data.map { preferences ->
        preferences[SELECTED_LEVEL_KEY] ?: "Tümü"
    }
}

// Öğrenilen kelimeler
suspend fun Context.addLearnedWord(word: String) {
    dataStore.edit { preferences ->
        val current = preferences[LEARNED_WORDS_KEY] ?: emptySet()
        preferences[LEARNED_WORDS_KEY] = current + word
    }
}
fun Context.getLearnedWordsFlow(): Flow<Set<String>> {
    return dataStore.data.map { preferences ->
        preferences[LEARNED_WORDS_KEY] ?: emptySet()
    }
}

// Yanlış kelimeler
suspend fun Context.addWrongWord(word: String) {
    dataStore.edit { preferences ->
        val current = preferences[WRONG_WORDS_KEY] ?: emptySet()
        preferences[WRONG_WORDS_KEY] = current + word
    }
}
suspend fun Context.removeWrongWord(word: String) {
    dataStore.edit { preferences ->
        val current = preferences[WRONG_WORDS_KEY] ?: emptySet()
        preferences[WRONG_WORDS_KEY] = current - word
    }
}
fun Context.getWrongWordsFlow(): Flow<Set<String>> {
    return dataStore.data.map { preferences ->
        preferences[WRONG_WORDS_KEY] ?: emptySet()
    }
}

// Tekrar kelimeleri
suspend fun Context.addRepeatWord(word: String) {
    dataStore.edit { preferences ->
        val current = preferences[REPEAT_WORDS_KEY] ?: emptySet()
        preferences[REPEAT_WORDS_KEY] = current + word
    }
}
fun Context.getRepeatWordsFlow(): Flow<Set<String>> {
    return dataStore.data.map { preferences ->
        preferences[REPEAT_WORDS_KEY] ?: emptySet()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val repository = WordRepository(db.wordDao())

        setContent {
            val darkModeFlow = getDarkModeFlow()
            var isDarkMode by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                darkModeFlow.collect { isDark ->
                    isDarkMode = isDark
                }
            }

            KelimeHatirlaticiTheme(darkTheme = isDarkMode) {
                MainApp(
                    repository = repository,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { newValue ->
                        kotlinx.coroutines.MainScope().launch {
                            setDarkModeEnabled(newValue)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(
    repository: WordRepository,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // ========== KALICI STATE (rotation'a dayanıklı) ==========
    var currentScreen by rememberSaveable { mutableStateOf("main") }
    var selectedLibrary by rememberSaveable { mutableStateOf("Tümü") }
    var selectedLevel by rememberSaveable { mutableStateOf("Tümü") }
    var currentWordIndex by rememberSaveable { mutableIntStateOf(0) }
    var showMeanings by rememberSaveable { mutableStateOf(false) }

    // DataStore'dan kalıcı değerleri oku
    LaunchedEffect(Unit) {
        context.getSelectedLibraryFlow().collect { lib -> selectedLibrary = lib }
    }
    LaunchedEffect(Unit) {
        context.getSelectedLevelFlow().collect { level -> selectedLevel = level }
    }

    // Öğrenilen ve yanlış kelimeler
    var learnedWords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var wrongWords by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        context.getLearnedWordsFlow().collect { words -> learnedWords = words }
    }
    LaunchedEffect(Unit) {
        context.getWrongWordsFlow().collect { words -> wrongWords = words }
    }

    // Veritabanı
    var words by remember { mutableStateOf<List<Word>>(emptyList()) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(currentScreen, selectedLibrary, selectedLevel) {
        if (currentScreen == "main") {
            words = repository.getWordsByLibraryAndLevel(
                if (selectedLibrary == "Tümü") "" else selectedLibrary,
                if (selectedLevel == "Tümü") "" else selectedLevel
            )
            libraries = repository.getAllLibraries()
            if (words.isNotEmpty()) {
                if (currentWordIndex >= words.size) currentWordIndex = 0
                currentWord = words[currentWordIndex]
            } else {
                currentWord = null
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kelime Hatırlatıcı",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Build 1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "384"} - Tayfun Yamak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                val drawerItems = listOf(
                    "Ana Sayfa" to Icons.Default.Home,
                    "Kütüphane Seç" to Icons.Default.LibraryBooks,
                    "İçe Aktar" to Icons.Default.FileUpload,
                    "Paketler" to Icons.Default.Inventory2,
                    "Ayarlar" to Icons.Default.Settings
                )

                drawerItems.forEach { (title, icon) ->
                    NavigationDrawerItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(title) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            when (title) {
                                "Ana Sayfa" -> { currentScreen = "main" }
                                "Kütüphane Seç" -> { currentScreen = "library" }
                                "İçe Aktar" -> { currentScreen = "import" }
                                "Paketler" -> { currentScreen = "packages" }
                                "Ayarlar" -> { currentScreen = "settings" }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "v:1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "384"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "By: Tayfun Yamak ©",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        content = {
            when (currentScreen) {
                "main" -> MainContent(
                    words = words,
                    currentWord = currentWord,
                    currentWordIndex = currentWordIndex,
                    showMeanings = showMeanings,
                    selectedLibrary = selectedLibrary,
                    selectedLevel = selectedLevel,
                    learnedWords = learnedWords,
                    wrongWords = wrongWords,
                    onDrawerOpen = { coroutineScope.launch { drawerState.open() } },
                    onWordChange = { index, word ->
                        currentWordIndex = index
                        currentWord = word
                    },
                    onToggleMeanings = { showMeanings = !showMeanings },
                    onScreenChange = { currentScreen = it },
                    onLearnedWord = { word ->
                        coroutineScope.launch { context.addLearnedWord(word) }
                    },
                    onWrongWord = { word ->
                        coroutineScope.launch { context.addWrongWord(word) }
                    }
                )

                "library" -> LibrariesScreen(
                    libraries = libraries,
                    repository = repository,
                    currentLibrary = selectedLibrary,
                    onLibrarySelect = { lib ->
                        selectedLibrary = lib
                        currentWord = null
                        currentWordIndex = 0
                        coroutineScope.launch { context.setSelectedLibrary(lib) }
                        currentScreen = "main"
                    },
                    onBack = { currentScreen = "main" }
                )

                "import" -> ImportScreen(
                    repository = repository,
                    onBack = { currentScreen = "main" },
                    onImportComplete = {
                        coroutineScope.launch {
                            words = repository.getWordsByLibraryAndLevel(
                                if (selectedLibrary == "Tümü") "" else selectedLibrary,
                                if (selectedLevel == "Tümü") "" else selectedLevel
                            )
                            libraries = repository.getAllLibraries()
                            if (words.isNotEmpty()) {
                                currentWord = words.first()
                                currentWordIndex = 0
                            }
                        }
                    }
                )

                "packages" -> PackagesScreen(
                    repository = repository,
                    onBack = { currentScreen = "main" }
                )

                "settings" -> SettingsScreen(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { onToggleDarkMode(it) },
                    onBack = { currentScreen = "main" },
                    onSave = { currentScreen = "main" }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    words: List<Word>,
    currentWord: Word?,
    currentWordIndex: Int,
    showMeanings: Boolean,
    selectedLibrary: String,
    selectedLevel: String,
    learnedWords: Set<String>,
    wrongWords: Set<String>,
    onDrawerOpen: () -> Unit,
    onWordChange: (Int, Word?) -> Unit,
    onToggleMeanings: () -> Unit,
    onScreenChange: (String) -> Unit,
    onLearnedWord: (String) -> Unit,
    onWrongWord: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Kelime Hatırlatıcı", style = MaterialTheme.typography.titleLarge)
                        if (words.isNotEmpty()) {
                            Text(
                                text = "$selectedLibrary / $selectedLevel | ${words.size} kelime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDrawerOpen) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (words.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz kelime eklenmemiş",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Menü > Paketler ile hazır paket yükleyin",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onScreenChange("packages") }) {
                        Icon(Icons.Default.Inventory2, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hazır Paketleri Gör")
                    }
                }
            } else {
                Text(
                    text = "Toplam: ${words.size} kelime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                currentWord?.let { word ->
                    // Kelime durumu rozeti
                    val isLearned = learnedWords.contains(word.word)
                    val isWrong = wrongWords.contains(word.word)
                    val statusText = when {
                        isLearned -> "✅ Öğrenildi"
                        isWrong -> "❌ Yanlış"
                        else -> "🔄 Öğreniliyor"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            isLearned -> MaterialTheme.colorScheme.secondary
                            isWrong -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (showMeanings) {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(16.dp))

                                // ========== ÇOKLU ANLAMLAR ==========
                                val meaningsList = if (word.meaning.contains("|||")) {
                                    word.meaning.split("|||")
                                } else {
                                    listOf(word.meaning)
                                }

                                Text(
                                    text = "Anlamları:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                meaningsList.forEachIndexed { index, meaning ->
                                    val cleanMeaning = meaning.trim().removePrefix("[").removeSuffix("]")
                                    if (cleanMeaning.isNotBlank()) {
                                        Text(
                                            text = "${index + 1}. $cleanMeaning",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }

                                // ========== ÇOKLU ÖRNEK CÜMLELER ==========
                                if (word.example.isNotBlank()) {
                                    val examplesList = if (word.example.contains("|||")) {
                                        word.example.split("|||")
                                    } else {
                                        listOf(word.example)
                                    }

                                    if (examplesList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Örnek Cümleler:",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        examplesList.forEachIndexed { index, ex ->
                                            val cleanEx = ex.trim().removePrefix("[").removeSuffix("]")
                                            if (cleanEx.isNotBlank()) {
                                                Text(
                                                    text = "${index + 1}. $cleanEx",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    textAlign = TextAlign.Center,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Anlamı görmek için tıklayın",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(text = word.library, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = word.level, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // BUTONLAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentWordIndex > 0) {
                                    onWordChange(currentWordIndex - 1, words[currentWordIndex - 1])
                                }
                            },
                            enabled = currentWordIndex > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Önceki", maxLines = 1)
                        }

                        Button(
                            onClick = onToggleMeanings,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (showMeanings) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showMeanings) "Gizle" else "Anlamı Göster", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                if (currentWordIndex < words.size - 1) {
                                    onWordChange(currentWordIndex + 1, words[currentWordIndex + 1])
                                }
                            },
                            enabled = currentWordIndex < words.size - 1,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sonraki", maxLines = 1)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Biliyorum / Bilmiyorum
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onLearnedWord(word.word) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isLearned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Biliyorum", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { onWrongWord(word.word) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isWrong) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bilmiyorum", maxLines = 1)
                        }

                        IconButton(onClick = { onScreenChange("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // İlerleme
                    Text(
                        text = "${currentWordIndex + 1} / ${words.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Arama
                    var searchQuery by rememberSaveable { mutableStateOf("") }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            if (query.isNotBlank()) {
                                val index = words.indexOfFirst {
                                    it.word.contains(query, ignoreCase = true)
                                }
                                if (index >= 0) {
                                    onWordChange(index, words[index])
                                }
                            }
                        },
                        label = { Text("Kelime Ara...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Temizle")
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Ara")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrariesScreen(
    libraries: List<String>,
    repository: WordRepository,
    currentLibrary: String,
    onLibrarySelect: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kütüphaneler") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            // Tümü seçeneği
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { onLibrarySelect("Tümü") },
                colors = CardDefaults.cardColors(
                    containerColor = if (currentLibrary == "Tümü") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = if (currentLibrary == "Tümü") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tümü",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (currentLibrary == "Tümü") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    if (currentLibrary == "Tümü") {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            items(libraries) { library ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onLibrarySelect(library) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentLibrary == library) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = if (currentLibrary == library) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = library,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (currentLibrary == library) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "${repository.getWordCountByLibrary(library)} kelime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (currentLibrary == library) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        } else {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
