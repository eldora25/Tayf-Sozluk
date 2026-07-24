package com.example.kelimehatirlatici

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.Word
import kotlinx.coroutines.launch
import org.json.JSONObject

data class WordPackage(
    val fileName: String,
    val name: String,
    val description: String,
    val languagePair: String,
    val level: String,
    val wordCount: Int
)

fun loadPackagesFromAssets(context: Context): List<WordPackage> {
    val packages = mutableListOf<WordPackage>()
    try {
        val fileNames = context.assets.list("packs") ?: return packages
        for (fileName in fileNames) {
            if (fileName.endsWith(".json")) {
                try {
                    val inputStream = context.assets.open("packs/$fileName")
                    val content = inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(content)
                    val wordsArray = json.getJSONArray("words")
                    packages.add(
                        WordPackage(
                            fileName = fileName,
                            name = json.getString("name"),
                            description = json.optString("description", ""),
                            languagePair = json.optString("languagePair", ""),
                            level = json.optString("level", "Genel"),
                            wordCount = wordsArray.length()
                        )
                    )
                } catch (_: Exception) { }
            }
        }
    } catch (_: Exception) { }
    return packages
}

fun loadWordsFromPackage(context: Context, fileName: String): List<Word> {
    val words = mutableListOf<Word>()
    try {
        val inputStream = context.assets.open("packs/$fileName")
        val content = inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(content)
        val library = json.getString("name")
        val level = json.optString("level", "Genel")
        val wordsArray = json.getJSONArray("words")

        for (i in 0 until wordsArray.length()) {
            val wordJson = wordsArray.getJSONObject(i)
            val word = wordJson.getString("word")

            val meaning: String
            val example: String

            if (wordJson.has("meanings")) {
                // ÇOKLU ANLAM FORMATI: ["anlam1", "anlam2"]
                val meaningsArray = wordJson.getJSONArray("meanings")
                val parts = mutableListOf<String>()
                for (j in 0 until meaningsArray.length()) {
                    parts.add(meaningsArray.getString(j))
                }
                meaning = parts.joinToString("|||")
            } else {
                // ESKİ FORMAT: "meaning": "..."
                meaning = wordJson.optString("meaning", "")
            }

            if (wordJson.has("examples")) {
                // ÇOKLU ÖRNEK FORMATI: ["cümle1", "cümle2"]
                val examplesArray = wordJson.getJSONArray("examples")
                val exParts = mutableListOf<String>()
                for (j in 0 until examplesArray.length()) {
                    exParts.add(examplesArray.getString(j))
                }
                example = exParts.joinToString("|||")
            } else {
                // ESKİ FORMAT: "example": "..."
                example = wordJson.optString("example", "")
            }

            words.add(
                Word(
                    word = word,
                    meaning = meaning,
                    example = example,
                    library = library,
                    level = level
                )
            )
        }
    } catch (_: Exception) { }
    return words
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    repository: WordRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var packages by remember { mutableStateOf<List<WordPackage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var installProgress by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        packages = loadPackagesFromAssets(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hazır Paketler", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        statusMessage = "Tüm paketler yükleniyor..."
                        var totalImported = 0
                        for (pkg in packages) {
                            installProgress = "${pkg.name} yükleniyor..."
                            val words = loadWordsFromPackage(context, pkg.fileName)
                            for (word in words) {
                                repository.addWord(word)
                                totalImported++
                            }
                        }
                        installProgress = ""
                        statusMessage = "✅ Toplam $totalImported kelime yüklendi!"
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && packages.isNotEmpty()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tüm Kütüphaneleri Yükle")
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = installProgress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (statusMessage.isNotBlank() && !isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (packages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz paket bulunamadı",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(packages) { pkg ->
                        PackageCard(pkg = pkg, onInstall = {
                            coroutineScope.launch {
                                isLoading = true
                                installProgress = "${pkg.name} yükleniyor..."
                                val words = loadWordsFromPackage(context, pkg.fileName)
                                var imported = 0
                                for (word in words) {
                                    repository.addWord(word)
                                    imported++
                                }
                                installProgress = ""
                                statusMessage = "✅ $imported kelime yüklendi: ${pkg.name}"
                                isLoading = false
                            }
                        }, isInstalling = isLoading)
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageCard(
    pkg: WordPackage,
    onInstall: () -> Unit,
    isInstalling: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LibraryBooks,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = pkg.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = pkg.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // SORUN DÜZELTİLDİ: Chip → SuggestionChip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(pkg.languagePair, style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(pkg.level, style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("${pkg.wordCount} kelime", style = MaterialTheme.typography.labelSmall) },
                    icon = { Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onInstall,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isInstalling
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bu Paketi Yükle")
            }
        }
    }
}
