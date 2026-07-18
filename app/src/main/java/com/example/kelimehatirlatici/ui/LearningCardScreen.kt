package com.example.kelimehatirlatici.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word
import kotlinx.coroutines.delay

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
    // Kart çevirme animasyonu
    var isFlipped by remember(word?.id) { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }

    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    // Kartın ön yüzü 0-90, arka yüzü 90-180 derece arasında görünür
    val showFront = rotationY < 90f

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
                    // ══════════ ÇEVRİLEBİLİR KELİME KARTI ══════════
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = this@LearningCardScreen.rotationY
                                cameraDistance = 12f * density
                            }
                            .then(
                                Modifier.defaultMinSize(minHeight = 220.dp)
                            ),
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        onClick = {
                            if (!isAnimating) {
                                if (!isFlipped) {
                                    // Ön yüzdeyken tıklandı → seslendir + çevir
                                    onSpeakClick(word.word)
                                }
                                isFlipped = !isFlipped
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)  // Quiz kartıyla aynı açık mavi
                        )
                    ) {
                        if (showFront) {
                            // ══════════ ÖN YÜZ: KELİME ══════════
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = word.word,
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0D47A1),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Seslendir",
                                            tint = Color(0xFF1976D2),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Anlamı görmek için dokun",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1976D2).copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            // ══════════ ARKA YÜZ: ANLAM ══════════
                            // Y ekseninde çevrildiği için içerik de ters görünmesin diye tekrar çevir
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 220.dp)
                                    .graphicsLayer {
                                        rotationY = 180f
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = word.meaning,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0D47A1),
                                        textAlign = TextAlign.Center
                                    )
                                    if (word.example.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = Color(0xFF0D47A1).copy(alpha = 0.2f))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = word.example,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF1565C0),
                                            textAlign = TextAlign.Center,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Geri çevirmek için dokun",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1976D2).copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bilmiyorum / Tekrar butonları
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(onClick = {
                            onKnownClick()
                            isFlipped = false
                        }) { Text("Biliyorum") }
                        OutlinedButton(onClick = {
                            onWrongClick()
                            isFlipped = false
                        }) { Text("Tekrar") }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // ══════════ VERSİYON NUMARASI ══════════
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "v:00.77",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = " By: Tayfun Yamak ©",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}
