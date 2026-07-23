package com.example.kelimehatirlatici

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.kelimehatirlatici.importer.ExcelImportHelper
import com.example.kelimehatirlatici.ui.ImportScreen
import com.example.kelimehatirlatici.ui.theme.KelimeHatirlaticiTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
            var isDrawerOpen by remember { mutableStateOf(false) }

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
                    },
                    isDrawerOpen = isDrawerOpen,
                    onDrawerStateChange = { isDrawerOpen = it }
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
    onToggleDarkMode: (Boolean) -> Unit,
    isDrawerOpen: Boolean,
    onDrawerStateChange: (Boolean) -> Unit
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

    val levels = remember {
        listOf(
            "A1 - Başlangıç",
            "A2 - Temel",
            "B1 - Orta",
            "B2 - Orta-Üst",
            "C1 - İleri",
            "C2 - Uzman",
            "Genel"
        )
    }

    // Verileri yükle
    LaunchedEffect(currentScreen) {
        if (currentScreen == "main" || currentScreen == "library") {
            words = repository.getWordsByLibraryAndLevel(
                if (selectedLibrary == "Tümü") "" else selectedLibrary,
                if (selectedLevel == "Tümü") "" else selectedLevel
            )
            libraries = repository.getAllLibraries()
        }
    }

    // Drawer state değişimini dışarı bildir
    LaunchedEffect(drawerState.isOpen) {
        onDrawerStateChange(drawerState.isOpen)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            when (title) {
                                "Ana Sayfa" -> { currentScreen = "main" }
                                "Kelime Ekle" -> { /* Gelecek özellik */ }
                                "Kelime Listesi" -> { /* Gelecek özellik */ }
                                "Kütüphane Seç" -> { currentScreen = "library" }
                                "Seviye Seç" -> { currentScreen = "main" }
                                "İçe Aktar" -> { currentScreen = "import" }
                                "Ayarlar" -> {
                                    // Ayarlar dialogunu aç
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Dark Mode Switch (Drawer altında)
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Karanlık Mod",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode(it) }
                    )
                }
            }
        },
        content = {
            when (currentScreen) {
                "main" -> {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = "Kelime Hatırlatıcı",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        if (selectedLibrary != "Tümü" || selectedLevel != "Tümü") {
                                            Text(
                                                text = "${words.size} kelime | ${if (selectedLibrary == "Tümü") "Tümü" else selectedLibrary}${if (selectedLevel != "Tümü") " / $selectedLevel" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            drawerState.open()
                                        }
                                    }) {
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (words.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
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
                                        text = "Menü > İçe Aktar ile kelime dosyası yükleyin",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                if (currentWord == null && words.isNotEmpty()) {
                                    LaunchedEffect(words) {
                                        currentWordIndex = 0
                                        currentWord = words.first()
                                    }
                                }

                                currentWord?.let { word ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = word.word,
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            if (showMeanings) {
                                                HorizontalDivider()
                                                Spacer(modifier = Modifier.height(16.dp))

                                                val meaningsList = try {
                                                    org.json.JSONArray(word.meanings).let { arr ->
                                                        (0 until arr.length()).map { arr.getString(it) }
                                                    }
                                                } catch (e: Exception) {
                                                    listOf(word.meaning)
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
                                                        modifier = Modifier.padding(vertical = 4.dp)
                                                    )
                                                }

                                                if (word.example.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    HorizontalDivider()
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Örnek:",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.tertiary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = word.example,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Center,
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                    )
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
                                                Text(
                                                    text = word.library,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = word.level,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                if (currentWordIndex > 0) {
                                                    currentWordIndex--
                                                    currentWord = words[currentWordIndex]
                                                    showMeanings = false
                                                }
                                            },
                                            enabled = currentWordIndex > 0
                                        ) {
                                            Icon(Icons.Default.SkipPrevious, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Önceki")
                                        }

                                        Button(
                                            onClick = { showMeanings = !showMeanings },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = if (showMeanings) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (showMeanings) "Gizle" else "Anlamı Göster")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (currentWordIndex < words.size - 1) {
                                                    currentWordIndex++
                                                    currentWord = words[currentWordIndex]
                                                    showMeanings = false
                                                }
                                            },
                                            enabled = currentWordIndex < words.size - 1
                                        ) {
                                            Text("Sonraki")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.SkipNext, contentDescription = null)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "${currentWordIndex + 1} / ${words.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = { query ->
                                            if (query.isNotBlank()) {
                                                val index = words.indexOfFirst {
                                                    it.word.contains(query, ignoreCase = true)
                                                }
                                                if (index >= 0) {
                                                    currentWordIndex = index
                                                    currentWord = words[index]
                                                    showMeanings = false
                                                }
                                            }
                                        },
                                        label = { Text("Kelime Ara...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        trailingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = "Ara")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                "library" -> {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Kütüphaneler") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        currentScreen = "main"
                                    }) {
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp)
                        ) {
                            items(libraries) { library ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        selectedLibrary = library
                                        currentWord = null
                                        currentWordIndex = 0
                                        currentScreen = "main"
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LibraryBooks,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = library,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "import" -> {
                    ImportScreen(
                        repository = repository,
                        onBack = {
                            currentScreen = "main"
                        },
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
                }
            }
        }
    )
}
