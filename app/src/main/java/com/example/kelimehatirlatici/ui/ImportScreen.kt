package com.example.kelimehatirlatici.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.Word
import com.example.kelimehatirlatici.WordRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    repository: WordRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var importStatus by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importedCount by remember { mutableIntStateOf(0) }
    var skippedCount by remember { mutableIntStateOf(0) }

    // Dosya seçme launcher - CSV ve TXT için
    val txtFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importStatus = "Dosya okunuyor..."
            importedCount = 0
            skippedCount = 0

            try {
                val result = importTxtOrCsvFile(context, repository, uri)
                importedCount = result.first
                skippedCount = result.second
                importStatus = "✅ İşlem tamamlandı: $importedCount kelime eklendi, $skippedCount atlandı"
            } catch (e: Exception) {
                Log.e("Import", "Hata: ${e.message}", e)
                importStatus = "❌ Hata: ${e.message}"
            }
            isImporting = false
        }
    }

    // JSON dosyası seçme launcher
    val jsonFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importStatus = "JSON dosyası okunuyor..."
            importedCount = 0
            skippedCount = 0

            try {
                val result = importJsonFile(context, repository, uri)
                importedCount = result.first
                skippedCount = result.second
                importStatus = "✅ İşlem tamamlandı: $importedCount kelime eklendi, $skippedCount atlandı"
            } catch (e: Exception) {
                Log.e("Import", "JSON Hata: ${e.message}", e)
                importStatus = "❌ Hata: ${e.message}"
            }
            isImporting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İçe Aktar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Desteklenen formatlar:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Format bilgileri
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    FormatInfoRow(
                        icon = Icons.Default.Description,
                        format = "CSV / TXT (FreeDict)",
                        details = "word,meaning1|||meaning2|||...,,level,library"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.TextSnippet,
                        format = "TXT (Lingoes)",
                        details = "word:meaning1;meaning2"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.DataObject,
                        format = "JSON",
                        details = "[{\"word\":\"...\",\"meanings\":[\"...\"],\"library\":\"...\"}]"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Butonlar
            Button(
                onClick = { txtFileLauncher.launch("text/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isImporting
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CSV / TXT (FreeDict / Lingoes) İçe Aktar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { jsonFileLauncher.launch("application/json") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isImporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.DataObject, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("JSON İçe Aktar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İlerleme durumu
            if (isImporting) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (importStatus.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (importStatus.startsWith("✅"))
                            MaterialTheme.colorScheme.primaryContainer
                        else if (importStatus.startsWith("❌"))
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = importStatus,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (importedCount > 0 && !isImporting) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ana Ekrana Dön")
                }
            }
        }
    }
}

@Composable
private fun FormatInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    format: String,
    details: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = format,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// DOSYA İÇE AKTARMA FONKSİYONLARI
// ============================================================

/**
 * TXT veya CSV dosyasını içe aktarır.
 * Desteklenen formatlar:
 * 1. FreeDict CSV: word,meaning1|||meaning2|||...,,level,library
 * 2. Lingoes TXT: word:meaning1;meaning2
 */
private fun importTxtOrCsvFile(
    context: Context,
    repository: WordRepository,
    uri: Uri
): Pair<Int, Int> {
    var imported = 0
    var skipped = 0

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("Dosya açılamadı!")

    val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

    reader.useLines { lines ->
        lines.forEach { line ->
            if (line.isBlank()) return@forEach

            try {
                val word = parseLine(line)
                if (word != null) {
                    repository.addWord(word)
                    imported++
                } else {
                    skipped++
                }
            } catch (e: Exception) {
                Log.e("Import", "Satır atlandı: $line -> ${e.message}")
                skipped++
            }
        }
    }

    return Pair(imported, skipped)
}

/**
 * Bir satırı parse ederek Word nesnesi oluşturur.
 * Hem FreeDict CSV hem Lingoes TXT formatını dener.
 */
private fun parseLine(line: String): Word? {
    // Önce CSV formatını dene (virgülle ayrılmış)
    if (line.contains(",")) {
        val parts = parseCsvLine(line)
        if (parts.size >= 2) {
            val word = parts[0].trim()
            val meaningField = parts[1].trim()

            if (word.isNotBlank() && meaningField.isNotBlank()) {
                // Anlamları ayır: ||| ile ayrılmışsa çoklu anlam
                val meanings = meaningField.split("|||")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (meanings.isNotEmpty()) {
                    val firstMeaning = meanings.first()
                    val meaningsJson = JSONArray(meanings).toString()

                    // Örnek cümle (varsa)
                    val example = if (parts.size > 2) parts[2].trim() else ""

                    // Seviye (varsa)
                    val level = if (parts.size > 3) parts[3].trim().ifBlank { "Genel" } else "Genel"

                    // Kütüphane (varsa)
                    val library = if (parts.size > 4) parts[4].trim().ifBlank { "Genel" } else "Genel"

                    return Word(
                        word = word,
                        meaning = firstMeaning,
                        meanings = meaningsJson,
                        example = example,
                        examples = if (example.isNotBlank()) JSONArray(listOf(example)).toString() else "[]",
                        library = library,
                        level = level
                    )
                }
            }
        }
    }

    // Lingoes TXT formatını dene: word:meaning1;meaning2
    if (line.contains(":")) {
        val colonIndex = line.indexOf(":")
        val word = line.substring(0, colonIndex).trim()
        val meaningPart = line.substring(colonIndex + 1).trim()

        if (word.isNotBlank() && meaningPart.isNotBlank()) {
            // Noktalı virgülle ayrılmış anlamlar
            val meanings = meaningPart.split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (meanings.isNotEmpty()) {
                val firstMeaning = meanings.first()
                val meaningsJson = JSONArray(meanings).toString()

                return Word(
                    word = word,
                    meaning = firstMeaning,
                    meanings = meaningsJson,
                    example = "",
                    examples = "[]",
                    library = "Genel",
                    level = "Genel"
                )
            }
        }
    }

    return null
}

/**
 * CSV satırını tırnak işaretlerini dikkate alarak parçalar.
 */
private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false

    for (char in line) {
        when {
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> {
                result.add(current.toString())
                current.clear()
            }
            else -> current.append(char)
        }
    }
    result.add(current.toString())

    return result
}

/**
 * JSON dosyasını içe aktarır.
 * Format: [{"word":"...","meaning":"...","meanings":["..."],"example":"...","examples":["..."],"library":"...","level":"..."}]
 */
private fun importJsonFile(
    context: Context,
    repository: WordRepository,
    uri: Uri
): Pair<Int, Int> {
    var imported = 0
    var skipped = 0

    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw Exception("JSON dosyası açılamadı!")

    val jsonString = inputStream.bufferedReader().use { it.readText() }
    val jsonArray = JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {
        try {
            val obj = jsonArray.getJSONObject(i)
            val word = obj.optString("word", "").trim()
            val meaning = obj.optString("meaning", "").trim()

            if (word.isBlank()) {
                skipped++
                continue
            }

            // Çoklu anlamları kontrol et
            val meanings: List<String>
            val meaningsJson: String

            if (obj.has("meanings")) {
                val meaningsArr = obj.getJSONArray("meanings")
                meanings = (0 until meaningsArr.length())
                    .map { meaningsArr.getString(it).trim() }
                    .filter { it.isNotBlank() }
                meaningsJson = JSONArray(meanings).toString()
            } else {
                val singleMeaning = meaning.ifBlank { word }
                meanings = listOf(singleMeaning)
                meaningsJson = JSONArray(listOf(singleMeaning)).toString()
            }

            // Çoklu örnek cümleleri kontrol et
            val example = obj.optString("example", "")
            val examples: String

            if (obj.has("examples")) {
                val examplesArr = obj.getJSONArray("examples")
                val examplesList = (0 until examplesArr.length())
                    .map { examplesArr.getString(it).trim() }
                    .filter { it.isNotBlank() }
                examples = JSONArray(examplesList).toString()
            } else {
                examples = if (example.isNotBlank())
                    JSONArray(listOf(example)).toString()
                else
                    "[]"
            }

            val library = obj.optString("library", "Genel").ifBlank { "Genel" }
            val level = obj.optString("level", "Genel").ifBlank { "Genel" }

            val newWord = Word(
                word = word,
                meaning = meaning.ifBlank { meanings.firstOrNull() ?: word },
                meanings = meaningsJson,
                example = example,
                examples = examples,
                library = library,
                level = level
            )

            repository.addWord(newWord)
            imported++
        } catch (e: Exception) {
            Log.e("Import", "JSON kayıt $i atlandı: ${e.message}")
            skipped++
        }
    }

    return Pair(imported, skipped)
}
