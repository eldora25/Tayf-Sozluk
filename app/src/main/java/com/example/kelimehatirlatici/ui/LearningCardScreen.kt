package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word

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
    var showMeaning by remember(word?.id) { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
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
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = word.word, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { onSpeakClick(word.word) }) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Seslendir")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (showMeaning) {
                                Text(text = word.meaning, style = MaterialTheme.typography.titleLarge)
                                if (word.example.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = word.example, style = MaterialTheme.typography.bodyLarge)
                                }
                            } else {
                                Button(onClick = { showMeaning = true }) { Text("Anlamı Göster") }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = onKnownClick) { Text("Biliyorum") }
                        OutlinedButton(onClick = onWrongClick) { Text("Tekrar") }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // ══════════ VERSİYON NUMARASI ══════════
            Text(
                text = "v:00.76",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}
