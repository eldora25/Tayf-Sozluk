package com.example.kelimehatirlatici.ui

import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.example.kelimehatirlatici.R
import com.example.kelimehatirlatici.data.DailyGoal
import com.example.kelimehatirlatici.data.Word

@Composable
fun GifImage(
    @DrawableRes gifRes: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // Android 9+ native AnimatedImageDrawable
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    val drawable = context.getDrawable(gifRes)
                    if (drawable is AnimatedImageDrawable) {
                        setImageDrawable(drawable)
                        drawable.start()
                    } else {
                        setImageResource(gifRes)
                    }
                }
            },
            modifier = modifier
        )
    } else {
        // Eski cihazlar için Coil kullan
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(gifRes)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
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
    var isFlipped by remember(word?.id) { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }

    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    val showFront = flipRotation < 90f

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
                // Çevrilebilir kelime kartı
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
                        // Ön yüz
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
                        // Arka yüz
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

                // Biliyorum / Tekrar butonları
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onKnownClick(); isFlipped = false }) { Text("Biliyorum") }
                    OutlinedButton(onClick = { onWrongClick(); isFlipped = false }) { Text("Tekrar") }
                }

                // ── GIF ANİMASYONU (native AnimatedImageDrawable + Coil yedek) ──
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

            // ══════════ VERSİYON NUMARASI ══════════
            Text(
                text = "v:01.32        By: Tayfun Yamak ©",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp)
            )
        }
    }
}
