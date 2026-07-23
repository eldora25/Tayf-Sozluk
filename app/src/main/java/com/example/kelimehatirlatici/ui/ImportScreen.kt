package com.example.kelimehatirlatici.ui

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
import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.importer.ExcelImportHelper

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

    // Dosya seçme launcher - tüm metin dosyaları
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importStatus = "Dosya okunuyor..."
            importedCount = 0
            skippedCount = 0

            try {
                val result = ExcelImportHelper.importFile(context, repository, uri)
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
                        format = "CSV",
                        details = "word,meaning1|||meaning2|||...,example,level,library"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.TextSnippet,
                        format = "TXT (Lingoes)",
                        details = "word: meaning1; meaning2"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.Code,
                        format = "TXT (FreeDict)",
                        details = "word=meaning1|||meaning2|||..."
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.DataObject,
                        format = "JSON (her satırda bir nesne)",
                        details = "{\"word\":\"...\",\"meanings\":[\"...\"]}"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.TableChart,
                        format = "Excel",
                        details = "İlk sayfa, ilk 5 sütun (word,meaning,example,level,library)"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // İçe Aktar Butonu
            Button(
                onClick = { fileLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isImporting
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dosya Seç ve İçe Aktar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CSV, TXT, JSON dosyaları desteklenir",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // İlerleme durumu
            if (isImporting) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kelimeler işleniyor...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (importStatus.isNotBlank() && !isImporting) {
                Spacer(modifier = Modifier.height(8.dp))
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
