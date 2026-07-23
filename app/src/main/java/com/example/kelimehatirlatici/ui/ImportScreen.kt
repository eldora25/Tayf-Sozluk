package com.example.kelimehatirlatici.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.importer.ExcelImportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    repository: WordRepository,
    onBack: () -> Unit,
    onImportComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    var importStatus by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importedCount by remember { mutableIntStateOf(0) }
    var skippedCount by remember { mutableIntStateOf(0) }

    // Kütüphane adı dialog durumu
    var showLibraryNameDialog by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var suggestedLibraryName by remember { mutableStateOf("") }
    var userLibraryName by remember { mutableStateOf("") }

    // ✨ YENİ: Karakter kodlaması seçimi
    var selectedCharset by remember { mutableStateOf("UTF-8") }
    val charsetOptions = listOf("UTF-8", "ISO-8859-9", "Windows-1254")

    // Dosya URI'sinden dosya adını çıkaran fonksiyon
    fun getFileName(uri: Uri): String {
        var name = "Yeni_Kutuphane"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        name = it.getString(nameIndex) ?: "Yeni_Kutuphane"
                        // Uzantıyı kaldır
                        if (name.contains(".")) {
                            name = name.substringBeforeLast(".")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Import", "Dosya adı alınamadı", e)
        }
        return name
    }

    // Dosya seçme launcher
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            suggestedLibraryName = getFileName(uri)
            userLibraryName = suggestedLibraryName
            selectedCharset = "UTF-8" // Varsayılan UTF-8
            showLibraryNameDialog = true // Kütüphane adı dialogunu aç
        }
    }

    // Kütüphane adı dialogu
    if (showLibraryNameDialog) {
        AlertDialog(
            onDismissRequest = { showLibraryNameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LibraryBooks, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kütüphane Adı ve Kodlama")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Seçtiğiniz dosyadan alınan kütüphane adı:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dosya: ${suggestedLibraryName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Kütüphane adı
                    OutlinedTextField(
                        value = userLibraryName,
                        onValueChange = { userLibraryName = it },
                        label = { Text("Kütüphane Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Bu isim altında yeni bir kütüphane oluşturulacak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // ✨ YENİ: Kodlama seçimi
                    Text(
                        text = "Dosya Karakter Kodlaması:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.selectableGroup()) {
                        charsetOptions.forEach { charset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (selectedCharset == charset),
                                        onClick = { selectedCharset = charset },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedCharset == charset),
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = charset,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Türkçe karakterler bozuk görünüyorsa ISO-8859-9 veya Windows-1254'ü deneyin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = userLibraryName.ifBlank { suggestedLibraryName.ifBlank { "Yeni_Kutuphane" } }
                        showLibraryNameDialog = false
                        isImporting = true
                        importStatus = "Dosya okunuyor (${selectedCharset})..."
                        importedCount = 0
                        skippedCount = 0

                        selectedFileUri?.let { uri ->
                            try {
                                val result = ExcelImportHelper.importFile(
                                    context = context,
                                    repository = repository,
                                    uri = uri,
                                    libraryName = finalName,
                                    charsetName = selectedCharset // ✨ Kodlama parametresi
                                )
                                importedCount = result.first
                                skippedCount = result.second
                                importStatus = "✅ İşlem tamamlandı: $importedCount kelime eklendi, $skippedCount atlandı (Kütüphane: $finalName)"
                            } catch (e: Exception) {
                                Log.e("Import", "Hata: ${e.message}", e)
                                importStatus = "❌ Hata: ${e.message}"
                            }
                        }
                        isImporting = false
                    },
                    enabled = userLibraryName.isNotBlank()
                ) {
                    Text("İçe Aktar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLibraryNameDialog = false }) {
                    Text("İptal")
                }
            }
        )
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
                        details = "word,meaning1|||meaning2...,example,level,library"
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
                        details = "word=meaning1|||meaning2..."
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FormatInfoRow(
                        icon = Icons.Default.DataObject,
                        format = "JSON (Tek satırda bir nesne)",
                        details = "{\"word\":\"...\",\"meanings\":[\"...\"]}"
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
                    text = "Kelimeler işleniyor (${selectedCharset})...",
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
                    onClick = {
                        onImportComplete()
                        onBack()
                    },
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
