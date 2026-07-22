package com.example.kelimehatirlatici.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word
import kotlinx.coroutines.launch

// ★ Yeni: ||| ile ayrılmış çoklu değerleri listeye çevirir
private fun parseMultiValue(text: String): List<String> {
    return text.split("|||").map { it.trim() }.filter { it.isNotBlank() }
}

// ★ Yeni: Çoklu anlamları görüntüleme - her anlamı alt alta • ile göster
private fun formatMultiMeanings(meaning: String): String {
    val parts = parseMultiValue(meaning)
    return if (parts.size > 1) {
        parts.joinToString("\n") { "• $it" }
    } else {
        meaning
    }
}

// ★ Yeni: Çoklu örnekleri görüntüleme - her örneği alt alta • ile göster
private fun formatMultiExamples(example: String): String {
    val parts = parseMultiValue(example)
    return if (parts.size > 1) {
        parts.joinToString("\n") { "• \"$it\"" }
    } else if (example.isNotBlank()) {
        "\"$example\""
    } else {
        ""
    }
}

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
    var isFlipped by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val flipRotation = animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Öğrenme Kartı") },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(text = { Text("📝 Kelime Ekle") }, onClick = { showMenu = false; onAddWordClick() })
                        DropdownMenuItem(text = { Text("📋 Kelime Listesi") }, onClick = { showMenu = false; onWordListClick() })
                        DropdownMenuItem(text = { Text("📚 Kütüphane Seç") }, onClick = { showMenu = false; onLibraryClick() })
                        DropdownMenuItem(text = { Text("🎯 Seviye Seç") }, onClick = { showMenu = false; onLevelClick() })
                        DropdownMenuItem(text = { Text("🎯 Günlük Hedef") }, onClick = { showMenu = false; onGoalClick() })
                        DropdownMenuItem(text = { Text("📊 İstatistikler") }, onClick = { showMenu = false; onStatsClick() })
                        DropdownMenuItem(text = { Text("❓ Quiz") }, onClick = { showMenu = false; onQuizClick() })
                        DropdownMenuItem(text = { Text("📥 İçe Aktar") }, onClick = { showMenu = false; onImportClick() })
                        DropdownMenuItem(text = { Text("📦 Paketler") }, onClick = { showMenu = false; onPacksClick() })
                        DropdownMenuItem(text = { Text("⚠️ Yanlış Kelimeler") }, onClick = { showMenu = false; onWrongWordsClick() })
                        DropdownMenuItem(text = { Text("⚙️ Ayarlar") }, onClick = { showMenu = false; onSettingsClick() })
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bilgi satırı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kütüphane: $selectedLibrary", style = MaterialTheme.typography.labelMedium)
                Text("Seviye: $selectedLevel", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Günlük hedef
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Günlük Hedef", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${dailyGoal?.completedCount ?: 0}/${dailyGoal?.targetCount ?: 10}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Kart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .graphicsLayer(
                        rotationY = flipRotation.value,
                        cameraDistance = 8f * density
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                onClick = { isFlipped = !isFlipped }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (flipRotation.value < 90f) {
                        // ═══════════════════════════════════════════
                        // ÖN YÜZ: Kelime
                        // ═══════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (word != null) {
                                Text(
                                    text = word.word,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    "Kelimeler bitti!\nYeni kelime ekleyin veya\nfarklı kütüphane/seviye seçin",
                                    textAlign = TextAlign.Center,
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // ═══════════════════════════════════════════
                        // ARKA YÜZ: Anlam ve Örnek
                        // ═══════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .graphicsLayer(scaleX = -1f) // Yazıyı düzeltmek için
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (word != null) {
                                // ★ Değişiklik: Çoklu anlamları alt alta • ile göster
                                Text(
                                    text = formatMultiMeanings(word.meaning),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // ★ Değişiklik: Çoklu örnekleri alt alta • ile göster
                                if (word.example.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Örnek Cümle(ler):",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatMultiExamples(word.example),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    "Kütüphane: ${word.library} | Seviye: ${word.level}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Butonlar
            if (word != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Konuşma butonu
                    OutlinedButton(
                        onClick = { onSpeakClick(word.word) },
                        modifier = Modifier.weight(1f)
                    ) { Text("🔊") }

                    // Bilmiyorum
                    OutlinedButton(
                        onClick = { isFlipped = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("❓ Bilmiyorum") }

                    // Yanlış
                    Button(
                        onClick = {
                            if (isFlipped) {
                                onWrongClick()
                                isFlipped = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) { Text("❌ Yanlış") }

                    // Biliyorum
                    Button(
                        onClick = {
                            if (isFlipped) {
                                onKnownClick()
                                isFlipped = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("✅ Biliyorum") }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // İpucu
            if (word != null && !isFlipped) {
                Text(
                    "Karta dokunarak çevirin",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            } else if (word != null) {
                Text(
                    "Butonlardan birine basın",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
