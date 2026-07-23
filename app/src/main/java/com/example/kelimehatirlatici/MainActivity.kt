package com.example.kelimehatirlatici

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.ui.ImportScreen
import com.example.kelimehatirlatici.ui.theme.KelimeHatirlaticiTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)
        val repository = WordRepository(db.wordDao())
        val coroutineScope = rememberCoroutineScope()

        // DataStore'dan dark mode tercihini oku
        val darkModeFlow = getDarkModeFlow()
        var isDarkMode by mutableStateOf(false)

        // Dark mode değişimlerini dinle
        LaunchedEffect(Unit) {
            darkModeFlow.collect { isDark ->
                isDarkMode = isDark
            }
        }

        setContent {
            KelimeHatirlaticiTheme(darkTheme = isDarkMode) {
                MainScreen(
                    repository = repository,
                    coroutineScope = coroutineScope,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { newValue ->
                        coroutineScope.launch {
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
private fun MainScreen(
    repository: WordRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    var currentScreen by remember { mutableStateOf("main") }
    var words by remember { mutableStateOf<List<Word>>(emptyList()) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedLibrary by remember { mutableStateOf("Tümü") }
    var selectedLevel by remember { mutableStateOf("Tümü") }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var showMeanings by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Levels
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
                        actions = {
                            // Ayarlar menüsü
                            Box {
                                IconButton(onClick = { showSettingsMenu = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                                }
                                DropdownMenu(
                                    expanded = showSettingsMenu,
                                    onDismissRequest = { showSettingsMenu = false }
                                ) {
                                    // Karanlık Mod toggle
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Karanlık Mod")
                                                }
                                                Switch(
                                                    checked = isDarkMode,
                                                    onCheckedChange = { onToggleDarkMode(it) }
                                                )
                                            }
                                        },
                                        onClick = { } // Switch zaten tıklanabiliyor
                                    )

                                    HorizontalDivider()

                                    // Kütüphane seçimi
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Kütüphaneler", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = { currentScreen = "library"; showSettingsMenu = false }
                                    )

                                    libraries.forEach { lib ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(
                                                        selected = (selectedLibrary == lib),
                                                        onClick = {
                                                            selectedLibrary = lib
                                                            currentWord = null
                                                            currentWordIndex = 0
                                                            showSettingsMenu = false
                                                            coroutineScope.launch {
                                                                words = repository.getWordsByLibraryAndLevel(
                                                                    if (selectedLibrary == "Tümü") "" else selectedLibrary,
                                                                    if (selectedLevel == "Tümü") "" else selectedLevel
                                                                )
                                                            }
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(lib)
                                                }
                                            },
                                            onClick = { }
                                        )
                                    }

                                    HorizontalDivider()

                                    // Seviye seçimi
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Seviyeler", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = { }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = (selectedLevel == "Tümü"),
                                                    onClick = {
                                                        selectedLevel = "Tümü"
                                                        currentWord = null
                                                        currentWordIndex = 0
                                                        showSettingsMenu = false
                                                        coroutineScope.launch {
                                                            words = repository.getWordsByLibraryAndLevel(
                                                                if (selectedLibrary == "Tümü") "" else selectedLibrary,
                                                                ""
                                                            )
                                                        }
                                                    }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Tümü")
                                            }
                                        },
                                        onClick = { }
                                    )

                                    levels.forEach { level ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(
                                                        selected = (selectedLevel == level),
                                                        onClick = {
                                                            selectedLevel = level
                                                            currentWord = null
                                                            currentWordIndex = 0
                                                            showSettingsMenu = false
                                                            coroutineScope.launch {
                                                                words = repository.getWordsByLibraryAndLevel(
                                                                    if (selectedLibrary == "Tümü") "" else selectedLibrary,
                                                                    if (selectedLevel == "Tümü") "" else selectedLevel
                                                                )
                                                            }
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(level)
                                                }
                                            },
                                            onClick = { }
                                        )
                                    }

                                    HorizontalDivider()

                                    // İçe Aktar
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("İçe Aktar")
                                            }
                                        },
                                        onClick = { currentScreen = "import"; showSettingsMenu = false }
                                    )

                                    // Dışa Aktar
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Dışa Aktar")
                                            }
                                        },
                                        onClick = {
                                            showSettingsMenu = false
                                            coroutineScope.launch {
                                                exportWords(repository)
                                            }
                                        }
                                    )
                                }
                            }
                        }
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
                        // Boş durum
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
                                text = "Ayarlar > İçe Aktar ile kelime dosyası yükleyin",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Kelime kartı
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

                                        // Anlamları göster
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

                                        // Örnek cümle
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

                            // Kontrol butonları
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Önceki
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

                                // Anlamı Göster/Gizle
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

                                // Sonraki
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

                            // İlerleme
                            Text(
                                text = "${currentWordIndex + 1} / ${words.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Kelime arama
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
                            IconButton(onClick = { currentScreen = "main" }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                            }
                        }
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
        }
    }
}

// Dışa aktarma fonksiyonu
private suspend fun exportWords(repository: WordRepository) {
    // Bu fonksiyon mevcut haliyle kalabilir
}

@Composable
private fun rememberCoroutineScope() = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
