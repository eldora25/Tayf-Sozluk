package com.example.kelimehatirlatici.ui

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.*
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LearningCardScreen(
    word: Word?,
    selectedLibrary: String,
    selectedLevel: String,
    dailyGoal: DailyGoal?,
    totalWordCount: Int,
    onKnownClick: () -> Unit,
    onWrongClick: () -> Unit,
    onSpeakClick: () -> Unit,
    onAddWordClick: () -> Unit,
    onWordListClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onLevelClick: () -> Unit,
    onGoalClick: () -> Unit,
    onStatsClick: () -> Unit,
    onQuizClick: () -> Unit,
    onImportClick: () -> Unit,
    onPacksClick: () -> Unit,
    onWrongWordsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var flipped by remember { mutableStateOf(false) }
    val rotation = animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardRotation"
    )

    // Düzenleme dialog state'leri
    var editWord by remember(word?.word) { mutableStateOf(word?.word ?: "") }
    var editMeaning by remember(word?.meaning) { mutableStateOf(word?.meaning ?: "") }
    var editExample by remember(word?.example) { mutableStateOf(word?.example ?: "") }
    var editLevel by remember(word?.level) { mutableStateOf(word?.level ?: "A1") }
    var editLibrary by remember(word?.library) { mutableStateOf(word?.library ?: selectedLibrary) }
    var editMode by remember { mutableStateOf("update") } // "update", "copy", "move"
    var editMessage by remember { mutableStateOf("") }
    var showEditSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val wordRepository = remember { WordRepository(AppDatabase.getInstance(context).wordDao()) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }

    // Kütüphane listesini al
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            libraries = wordRepository.getLibraries()
        }
    }

    // Seçilen kelime değişince dialog state'lerini güncelle
    LaunchedEffect(word) {
        editWord = word?.word ?: ""
        editMeaning = word?.meaning ?: ""
        editExample = word?.example ?: ""
        editLevel = word?.level ?: "A1"
        editLibrary = word?.library ?: selectedLibrary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Tayf Sözlük",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "$selectedLibrary • $selectedLevel • $totalWordCount kelime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("📚 Kütüphane Yönetimi") }, onClick = { showMenu = false; onLibraryClick() })
                        DropdownMenuItem(text = { Text("🎯 Seviye Seç") }, onClick = { showMenu = false; onLevelClick() })
                        DropdownMenuItem(text = { Text("📝 Kelime Ekle") }, onClick = { showMenu = false; onAddWordClick() })
                        DropdownMenuItem(text = { Text("📖 Kelime Listesi") }, onClick = { showMenu = false; onWordListClick() })
                        DropdownMenuItem(text = { Text("🎯 Günlük Hedef") }, onClick = { showMenu = false; onGoalClick() })
                        DropdownMenuItem(text = { Text("📊 İstatistikler") }, onClick = { showMenu = false; onStatsClick() })
                        DropdownMenuItem(text = { Text("❓ Quiz") }, onClick = { showMenu = false; onQuizClick() })
                        DropdownMenuItem(text = { Text("📥 İçe Aktar") }, onClick = { showMenu = false; onImportClick() })
                        DropdownMenuItem(text = { Text("📦 Paketler") }, onClick = { showMenu = false; onPacksClick() })
                        DropdownMenuItem(text = { Text("⚠️ Yanlış Kelimeler") }, onClick = { showMenu = false; onWrongWordsClick() })
                        DropdownMenuItem(text = { Text("⚙️ Ayarlar") }, onClick = { showMenu = false; onSettingsClick() })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Günlük hedef çubuğu
            if (dailyGoal != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🎯 Günlük Hedef", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${dailyGoal.completedCount}/${dailyGoal.targetCount}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (dailyGoal.targetCount > 0)
                                    (dailyGoal.completedCount.toFloat() / dailyGoal.targetCount).coerceIn(0f, 1f)
                                else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (dailyGoal.completedCount >= dailyGoal.targetCount)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.primary,
                            trackColor = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (word == null) {
                // Kelime yok
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Bu kütüphanede/seviyede kelime bulunamadı.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddWordClick) { Text("Kelime Ekle") }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onPacksClick) { Text("Paketlerden Yükle") }
                    }
                }
            } else {
                // KART
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .heightIn(min = 250.dp)
                            .graphicsLayer {
                                rotationY = rotation.value
                                cameraDistance = 12f * density
                            }
                            .clickable { flipped = !flipped },
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation.value <= 90f) {
                                // Ön yüz: Kelime
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = word.word,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (word.detail.isNotEmpty()) {
                                        Text(
                                            text = word.detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "Dokun → Anlamı Gör",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                // Arka yüz: Anlam
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .rotate(180f)
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = word.meaning,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (word.example.isNotEmpty()) {
                                        Text(
                                            text = word.example,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "[${word.level}] • ${word.library}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Alt butonlar
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ===== YENİ: Düzenleme (çark) butonu =====
                    FilledTonalIconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Kelimeyi Düzenle",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Tekrar (Yanlış) butonu
                    Button(
                        onClick = onWrongClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336).copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tekrar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Biliyorum (Doğru) butonu
                    Button(
                        onClick = onKnownClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Biliyorum", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seslendirme butonu
                Button(
                    onClick = onSpeakClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Seslendir", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ===== YENİ: Kelime Düzenleme Dialogu =====
    if (showEditDialog && word != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Column {
                    Text("Kelimeyi Düzenle", fontWeight = FontWeight.Bold)
                    if (showEditSuccess) {
                        Text(
                            text = editMessage,
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Kelime
                    OutlinedTextField(
                        value = editWord,
                        onValueChange = { editWord = it },
                        label = { Text("Kelime") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Anlam
                    OutlinedTextField(
                        value = editMeaning,
                        onValueChange = { editMeaning = it },
                        label = { Text("Anlamı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Örnek Cümle
                    OutlinedTextField(
                        value = editExample,
                        onValueChange = { editExample = it },
                        label = { Text("Örnek Cümle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Seviye
                    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
                    Text("Seviye", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        levels.forEach { level ->
                            FilterChip(
                                selected = editLevel == level,
                                onClick = { editLevel = level },
                                label = { Text(level, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kütüphane seçimi
                    Text("Kütüphane", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    var libraryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = libraryExpanded,
                        onExpandedChange = { libraryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = editLibrary,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = libraryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Kütüphane") }
                        )
                        ExposedDropdownMenu(
                            expanded = libraryExpanded,
                            onDismissRequest = { libraryExpanded = false }
                        ) {
                            libraries.forEach { lib ->
                                DropdownMenuItem(
                                    text = { Text(lib) },
                                    onClick = {
                                        editLibrary = lib
                                        libraryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // İşlem türü seçimi
                    Text("İşlem Türü", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Güncelle (mevcut kelimeyi değiştir)
                        FilterChip(
                            selected = editMode == "update",
                            onClick = { editMode = "update" },
                            label = { Text("Güncelle", fontSize = 12.sp) },
                            leadingIcon = {
                                if (editMode == "update") Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Kopyala
                        FilterChip(
                            selected = editMode == "copy",
                            onClick = { editMode = "copy" },
                            label = { Text("Kopyala", fontSize = 12.sp) },
                            leadingIcon = {
                                if (editMode == "copy") Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Taşı
                        FilterChip(
                            selected = editMode == "move",
                            onClick = { editMode = "move" },
                            label = { Text("Taşı", fontSize = 12.sp) },
                            leadingIcon = {
                                if (editMode == "move") Icon(
                                    Icons.Default.DriveFileMove,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (editMode == "copy") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Kelime seçilen kütüphaneye kopyalanacak, orijinali kalacak.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    if (editMode == "move") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Kelime seçilen kütüphaneye taşınacak, orijinali silinecek.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val trimmedWord = editWord.trim()
                                val trimmedMeaning = editMeaning.trim()
                                val trimmedExample = editExample.trim().ifEmpty { null }

                                when (editMode) {
                                    "update" -> {
                                        wordRepository.updateWordFull(
                                            id = word.id,
                                            newWord = trimmedWord,
                                            newMeaning = trimmedMeaning,
                                            newExample = trimmedExample,
                                            newLevel = editLevel,
                                            newLibrary = editLibrary
                                        )
                                        editMessage = "✅ Kelime güncellendi!"
                                    }
                                    "copy" -> {
                                        val success = wordRepository.copyWordToLibrary(
                                            word = word.copy(
                                                word = trimmedWord,
                                                meaning = trimmedMeaning,
                                                example = trimmedExample,
                                                level = editLevel
                                            ),
                                            targetLibrary = editLibrary
                                        )
                                        editMessage = if (success) {
                                            "✅ Kelime \"$editLibrary\" kütüphanesine kopyalandı!"
                                        } else {
                                            "⚠️ Bu kelime zaten \"$editLibrary\" kütüphanesinde var!"
                                        }
                                    }
                                    "move" -> {
                                        // Önce kelimeyi güncelle, sonra kütüphanesini değiştir
                                        wordRepository.updateWordFull(
                                            id = word.id,
                                            newWord = trimmedWord,
                                            newMeaning = trimmedMeaning,
                                            newExample = trimmedExample,
                                            newLevel = editLevel,
                                            newLibrary = editLibrary
                                        )
                                        editMessage = "✅ Kelime \"$editLibrary\" kütüphanesine taşındı!"
                                    }
                                }

                                showEditSuccess = true

                                // 1.5 saniye sonra dialogu kapat
                                kotlinx.coroutines.delay(1500)
                                showEditDialog = false
                                showEditSuccess = false
                                editMessage = ""

                            } catch (e: Exception) {
                                editMessage = "❌ Hata: ${e.message}"
                                showEditSuccess = true
                            }
                        }
                    },
                    enabled = editWord.isNotBlank() && editMeaning.isNotBlank()
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showEditDialog = false
                    showEditSuccess = false
                    editMessage = ""
                }) {
                    Text("İptal")
                }
            }
        )
    }
}
