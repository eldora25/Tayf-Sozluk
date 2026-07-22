package com.example.kelimehatirlatici.ui
import com.example.kelimehatirlatici.ui.GifImage

import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kelimehatirlatici.BuildConfig
import com.example.kelimehatirlatici.R
import com.example.kelimehatirlatici.WordRepository
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningCardScreen(
    word: Word?,
    selectedLibrary: String,
    selectedLevel: String,
    dailyGoal: DailyGoal?,
    totalWordCount: Int,
    onKnownClick: () -> Unit,
    onWrongClick: () -> Unit,
    onSpeakClick: (String) -> Unit,
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
    var isFlipped by remember(word?.id) { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }

    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    val showFront = flipRotation < 90f

    var menuExpanded by remember { mutableStateOf(false) }

    // ═══════════════════════════════════════════════════════
    // KELİME DÜZENLEME STATE'LERİ
    // ═══════════════════════════════════════════════════════
    var showEditDialog by remember { mutableStateOf(false) }
    var editWord by remember(word?.word) { mutableStateOf(word?.word ?: "") }
    var editMeaning by remember(word?.meaning) { mutableStateOf(word?.meaning ?: "") }
    var editExample by remember(word?.example) { mutableStateOf(word?.example ?: "") }
    var editLevel by remember(word?.level) { mutableStateOf(word?.level ?: "A1") }
    var editLibrary by remember(word?.library) { mutableStateOf(word?.library ?: selectedLibrary) }
    var editMode by remember { mutableStateOf("update") }
    var editMessage by remember { mutableStateOf("") }
    var showEditSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val wordRepository = remember { WordRepository(AppDatabase.getInstance(context).wordDao()) }
    var libraries by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            libraries = wordRepository.getLibraries()
        }
    }

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
                        Text("Kelime Hatırlatıcı", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "$selectedLibrary / $selectedLevel  |  Toplam: $totalWordCount kelime",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menü",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("➕ Kelime Ekle") }, onClick = { menuExpanded = false; onAddWordClick() })
                            DropdownMenuItem(text = { Text("📋 Kelime Listesi") }, onClick = { menuExpanded = false; onWordListClick() })
                            DropdownMenuItem(text = { Text("📚 Kütüphane Seç") }, onClick = { menuExpanded = false; onLibraryClick() })
                            DropdownMenuItem(text = { Text("📊 Seviye Seç") }, onClick = { menuExpanded = false; onLevelClick() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("🎯 Günlük Hedef") }, onClick = { menuExpanded = false; onGoalClick() })
                            DropdownMenuItem(text = { Text("📈 İstatistikler") }, onClick = { menuExpanded = false; onStatsClick() })
                            DropdownMenuItem(text = { Text("🧠 Quiz") }, onClick = { menuExpanded = false; onQuizClick() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("📥 İçe Aktar") }, onClick = { menuExpanded = false; onImportClick() })
                            DropdownMenuItem(text = { Text("📦 Paketler") }, onClick = { menuExpanded = false; onPacksClick() })
                            DropdownMenuItem(text = { Text("❌ Yanlış Kelimeler") }, onClick = { menuExpanded = false; onWrongWordsClick() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("⚙️ Ayarlar") }, onClick = { menuExpanded = false; onSettingsClick() })
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val completed = dailyGoal?.completedCount ?: 0
            val target = dailyGoal?.targetCount ?: 10
            val progress = if (target > 0) completed.toFloat() / target.toFloat() else 0f

            Text(text = "Günlük hedef: $completed / $target", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))

            if (word == null) {
                Text("Bu kütüphane ve seviyede kelime yok.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Menüden Paketler bölümünden hazır kelimeleri yükleyebilir veya yeni kelime ekleyebilirsin.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationY = flipRotation
                            cameraDistance = 12f * density
                        }
                        .defaultMinSize(minHeight = 220.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    onClick = {
                        if (!isAnimating) {
                            if (!isFlipped) {
                                onSpeakClick(word.word)
                            }
                            isFlipped = !isFlipped
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    if (showFront) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Text(word.word, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Seslendir", tint = Color(0xFF1976D2), modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Anlamı görmek için dokun", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1976D2).copy(alpha = 0.6f))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 220.dp)
                                .graphicsLayer { rotationY = 180f }
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(Color(0xFFF3E5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(24.dp)) {
                                Text(word.meaning, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4A148C), textAlign = TextAlign.Center)
                                if (word.example.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFF4A148C).copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(word.example, style = MaterialTheme.typography.titleMedium, color = Color(0xFF6A1B9A), textAlign = TextAlign.Center, fontStyle = FontStyle.Italic)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Geri çevirmek için dokun", style = MaterialTheme.typography.bodySmall, color = Color(0xFF7B1FA2).copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════════
                // ALT BUTONLAR (Çark + Biliyorum + Tekrar)
                // ═══════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Button(
                        onClick = { onKnownClick(); isFlipped = false },
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

                    OutlinedButton(
                        onClick = { onWrongClick(); isFlipped = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tekrar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GifImage(
                        gifRes = R.raw.study_gif,
                        modifier = Modifier.size(120.dp),
                        contentDescription = "Çalışma animasyonu"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "v:${BuildConfig.VERSION_NAME}        By: Tayfun Yamak ©",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp)
            )
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // KELİME DÜZENLEME DİALOGU
    // ══════════════════════════════════════════════════════════════════════
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

                    Text("İşlem Türü", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                        Text("Kelime seçilen kütüphaneye kopyalanacak, orijinali kalacak.", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (editMode == "move") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Kelime seçilen kütüphaneye taşınacak.", fontSize = 12.sp, color = Color.Gray)
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
                                val trimmedExample = editExample.trim()

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
