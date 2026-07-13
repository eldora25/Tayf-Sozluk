package com.example.kelimehatirlatici.ui

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningCardScreen(
    word: Word?,
    selectedLibrary: String,
    selectedLevel: String,
    dailyGoal: DailyGoal?,
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
    onWrongWordsClick: () -> Unit
) {
    var showMeaning by remember(word?.id) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelime Hatırlatıcı")
                        Text(
                            text = "$selectedLibrary / $selectedLevel",
                            style = MaterialTheme.typography.bodySmall
                        )
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

            Text(
                text = "Günlük hedef: $completed / $target",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (word == null) {
                Text(
                    text = "Bu kütüphane ve seviyede kelime yok.",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Paketler bölümünden hazır kelimeleri yükleyebilir veya yeni kelime ekleyebilirsin.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = word.word,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    onSpeakClick(word.word)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Seslendir"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (showMeaning) {
                            Text(
                                text = word.meaning,
                                style = MaterialTheme.typography.titleLarge
                            )

                            if (word.example.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = word.example,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    showMeaning = true
                                }
                            ) {
                                Text("Anlamı Göster")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onKnownClick
                    ) {
                        Text("Biliyorum")
                    }

                    OutlinedButton(
                        onClick = onWrongClick
                    ) {
                        Text("Tekrar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton("Kelime Ekle", onAddWordClick)
            MenuButton("Kelime Listesi", onWordListClick)
            MenuButton("Kütüphane Seç", onLibraryClick)
            MenuButton("Seviye Seç", onLevelClick)
            MenuButton("Günlük Hedef", onGoalClick)
            MenuButton("İstatistikler", onStatsClick)
            MenuButton("Quiz", onQuizClick)
            MenuButton("İçe Aktar", onImportClick)
            MenuButton("Paketler", onPacksClick)
            MenuButton("Yanlış Kelimeler", onWrongWordsClick)
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}
