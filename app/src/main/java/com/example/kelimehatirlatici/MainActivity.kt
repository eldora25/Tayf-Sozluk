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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.ui.ImportScreen
import com.example.kelimehatirlatici.ui.theme.KelimeHatirlaticiTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray

// DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("main") }
    var words by remember { mutableStateOf<List<Word>>(emptyList()) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedLibrary by remember { mutableStateOf("Tümü") }
    var selectedLevel by remember { mutableStateOf("Tümü") }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var showMeanings by remember { mutableStateOf(false) }

    // Verileri yükle
    LaunchedEffect(currentScreen) {
        if (currentScreen == "main" || currentScreen == "library") {
            words = repository.getWordsByLibraryAndLevel(
                if (selectedLibrary == "Tümü") "" else selectedLibrary,
                if (selectedLevel == "Tümü") "" else selectedLevel
            )
            libraries = repository.getAllLibraries()
            if (words.isNotEmpty() && currentWord == null) {
                currentWordIndex = 0
                currentWord = words.first()
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
                // Drawer Header
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
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

                // Drawer Menü Öğeleri
                val drawerItems = listOf(
                    "Ana Sayfa" to Icons.Default.Home,
                    "Kelime Ekle" to Icons.Default.Add,
                    "Kelime Listesi" to Icons.Default.List,
                    "Kütüphane Seç" to Icons.Default.LibraryBooks,
                    "Seviye Seç" to Icons.Default.Speed,
                    "Günlük Hedef" to Icons.Default.TrackChanges,
                    "İstatistikler" to Icons.Default.BarChart,
                    "Quiz" to Icons.Default.Quiz,
                    "İçe Aktar" to Icons.Default.FileUpload,
                    "Paketler" to Icons.Default.Inventory2,
                    "Yanlış Kelimeler" to Icons.Default.Error,
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

                // Alt bilgi
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
                    onDrawerOpen = { coroutineScope.launch { drawerState.open() } },
                    onWordChange = { index, word ->
                        currentWordIndex = index
                        currentWord = word
                    },
                    onToggleMeanings = { showMeanings = !showMeanings },
                    onScreenChange = { currentScreen = it }
                )

                "library" -> LibrariesScreen(
                    libraries = libraries,
                    repository = repository,
                    onLibrarySelect = { lib ->
                        selectedLibrary = lib
                        currentWord = null
                        currentWordIndex = 0
                        currentScreen = "main"
                    },
                    onBack = { currentScreen = "main" }
                )

                "import" -> ImportScreen(
                    repository = repository,
                    onBack = { currentScreen = "main" },
                    onImportComplete = {
                        scope.launch {
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
    onDrawerOpen: () -> Unit,
    onWordChange: (Int, Word?) -> Unit,
    onToggleMeanings: () -> Unit,
    onScreenChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Kelime Hatırlatıcı", style = MaterialTheme.typography.titleLarge)
                        if (words.isNotEmpty()) {
                            Text(
                                text = "$selectedLibrary / $selectedLevel | Toplam: ${words.size} kelime",
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
                // Boş durum
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
                        text = "Menü > Paketler veya İçe Aktar ile kelime yükleyin",
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

                                // Çoklu anlamları göster (||| ile ayrılmış)
                                val meaningsList = if (word.meanings.contains("|||")) {
                                    word.meanings.split("|||")
                                } else {
                                    try {
                                        JSONArray(word.meanings).let { arr ->
                                            (0 until arr.length()).map { arr.getString(it) }
                                        }
                                    } catch (_: Exception) {
                                        listOf(word.meaning)
                                    }
                                }

                                Text(
                                    text = "Anlamları:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                meaningsList.forEachIndexed { index, meaning ->
                                    Text(
                                        text = "${index + 1}. $meaning",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }

                                // Çoklu örnek cümleleri göster
                                if (word.example.isNotBlank()) {
                                    val examplesList = if (word.example.contains("|||")) {
                                        word.example.split("|||")
                                    } else {
                                        try {
                                            JSONArray(word.examples).let { arr ->
                                                (0 until arr.length()).map { arr.getString(it) }
                                            }
                                        } catch (_: Exception) {
                                            listOf(word.example)
                                        }
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

                    // Butonlar
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

                    // İlerleme ve ayarlar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentWordIndex + 1} / ${words.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { onScreenChange("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Kelime arama
                    var searchQuery by remember { mutableStateOf("") }
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
            items(libraries) { library ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onLibrarySelect(library) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LibraryBooks, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = library, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${repository.getWordCountByLibrary(library)} kelime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
