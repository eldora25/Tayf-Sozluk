package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word

// Çoklu değerleri listeye çevirir
private fun parseMultiValue(text: String): List<String> {
    return text.split("|||").map { it.trim() }.filter { it.isNotBlank() }
}

// Çoklu anlamları alt alta görüntüleme
private fun formatMultiMeanings(meaning: String): String {
    val parts = parseMultiValue(meaning)
    return if (parts.size > 1) {
        parts.joinToString("\n") { "• $it" }
    } else {
        meaning
    }
}

// Çoklu örnekleri alt alta görüntüleme
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
fun AppScreen(
    words: List<Word>,
    word: Word?,
    selectedLibrary: String,
    selectedLevel: String,
    totalWordCount: Int,
    dailyGoal: DailyGoal?,
    isFlipped: Boolean,
    memorizationThreshold: Int,
    onKnownClick: () -> Unit,
    onWrongClick: () -> Unit,
    onFlip: () -> Unit,
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
    onSettingsClick: () -> Unit,
    onWordClick: (Word) -> Unit,
    onWordLongClick: (Word) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelime Hatırlatıcı", fontSize = 18.sp)
                        // Build versiyonu + Tayfun Yamak
                        Text(
                            "Build 1.0 - Tayfun Yamak",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                actions = {
                    // Çark ikonu - ayarlar menüsü
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ═══════════════════════════════════════════
            // ÜST BİLGİ: Kütüphane, Seviye, Kelime Sayısı
            // ═══════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kütüphane
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = onLibraryClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        Text(selectedLibrary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    // Seviye
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = onLevelClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        Text(selectedLevel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    // Kelime sayısı
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalWordCount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    // Günlük hedef
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = onGoalClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            "${dailyGoal?.completedCount ?: 0}/${dailyGoal?.targetCount ?: 10}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══════════════════════════════════════════
            // KELİME LİSTESİ - Çark ikonlu düzenleme
            // ═══════════════════════════════════════════
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(words) { w ->
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onWordClick(w) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(w.word, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        text = formatMultiMeanings(w.meaning),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        maxLines = if (expanded) Int.MAX_VALUE else 2
                                    )
                                }

                                // Çark ikonu - Düzenle/Sil
                                IconButton(onClick = { onWordLongClick(w) }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Düzenle/Sil",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Genişletme oku
                            if (w.meaning.split("|||").size > 1 || w.example.isNotBlank()) {
                                TextButton(
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Text(
                                        if (expanded) "Daha az göster" else "Daha fazla göster",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (expanded && w.example.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatMultiExamples(w.example),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ═══════════════════════════════════════════
            // HIZLI İŞLEM BUTONLARI
            // ═══════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onQuizClick,
                    modifier = Modifier.weight(1f)
                ) { Text("❓ Quiz") }

                OutlinedButton(
                    onClick = onAddWordClick,
                    modifier = Modifier.weight(1f)
                ) { Text("➕ Ekle") }

                OutlinedButton(
                    onClick = onWordListClick,
                    modifier = Modifier.weight(1f)
                ) { Text("📋 Liste") }
            }
        }
    }
}
