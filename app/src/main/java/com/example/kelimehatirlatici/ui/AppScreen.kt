package com.example.kelimehatirlatici.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.kelimehatirlatici.data.Word
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    words: List<Word>,
    word: Word?,
    selectedLibrary: String,
    selectedLevel: String,
    totalWordCount: Int,
    dailyGoal: Int?,
    learnedToday: Int,
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
    onWordLongClick: (Word) -> Unit,
    onWordEdit: (Word) -> Unit
) {
    var drawerOpen by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flip"
    )

    // Anlamları JSON'dan parse et
    val meanings = remember(word) {
        word?.let { w ->
            try {
                val arr = JSONArray(w.meanings)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (e: Exception) {
                listOf(w.meaning)
            }
        } ?: emptyList()
    }

    // Örnek cümleleri JSON'dan parse et
    val examples = remember(word) {
        word?.let { w ->
            try {
                val arr = JSONArray(w.examples)
                (0 until arr.length()).map { arr.getString(it) }
            } catch (e: Exception) {
                if (w.example.isNotBlank()) listOf(w.example) else emptyList()
            }
        } ?: emptyList()
    }

    // Lottie animasyonu
    val compositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(com.example.kelimehatirlatici.R.raw.study_gif)
    )
    val lottieComposition = compositionResult.value
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = !isFlipped
    )

    ModalNavigationDrawer(
        drawerState = rememberDrawerState(DrawerValue.Closed).also {
            LaunchedEffect(drawerOpen) {
                if (drawerOpen) it.open() else it.close()
            }
        },
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Kelime Hatırlatıcı",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Build 1.0 - Tayfun Yamak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Drawer Menü Öğeleri
                DrawerMenuItem(
                    icon = Icons.Default.Add,
                    label = "Kelime Ekle",
                    onClick = { drawerOpen = false; onAddWordClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.List,
                    label = "Kelime Listesi",
                    onClick = { drawerOpen = false; onWordListClick() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                DrawerMenuItem(
                    icon = Icons.Default.LibraryBooks,
                    label = "Kütüphane Seç",
                    onClick = { drawerOpen = false; onLibraryClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.School,
                    label = "Seviye Seç",
                    onClick = { drawerOpen = false; onLevelClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Günlük Hedef",
                    onClick = { drawerOpen = false; onGoalClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.BarChart,
                    label = "İstatistikler",
                    onClick = { drawerOpen = false; onStatsClick() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                DrawerMenuItem(
                    icon = Icons.Default.Quiz,
                    label = "Quiz",
                    onClick = { drawerOpen = false; onQuizClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.FileUpload,
                    label = "İçe Aktar",
                    onClick = { drawerOpen = false; onImportClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.Inventory2,
                    label = "Paketler",
                    onClick = { drawerOpen = false; onPacksClick() }
                )
                DrawerMenuItem(
                    icon = Icons.Default.ErrorOutline,
                    label = "Yanlış Kelimeler",
                    onClick = { drawerOpen = false; onWrongWordsClick() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                DrawerMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Ayarlar",
                    onClick = { drawerOpen = false; onSettingsClick() }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Kelime Hatırlatıcı",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$selectedLibrary / $selectedLevel | Toplam: $totalWordCount kelime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = !drawerOpen }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Günlük Hedef Progress
                val goal = dailyGoal ?: 10
                val progress = if (goal > 0) learnedToday.toFloat() / goal else 0f
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Günlük hedef: $learnedToday / $goal",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kelime Kartı (Flip Animasyonlu)
                if (word != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable { onFlip() },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .graphicsLayer {
                                    rotationY = rotationAngle
                                    cameraDistance = 12f * density
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            if (rotationAngle <= 90f) {
                                // Ön Yüz - Kelime
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = word.word,
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = { onSpeakClick(word.word) }) {
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = "Seslendir",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Anlamı görmek için dokun",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Arka Yüz - Anlamlar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                        .graphicsLayer { rotationY = 180f },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Çoklu anlamları göster
                                    meanings.forEachIndexed { index, meaning ->
                                        Text(
                                            text = "${index + 1}. $meaning",
                                            style = MaterialTheme.typography.titleLarge,
                                            textAlign = TextAlign.Center
                                        )
                                        if (index < meanings.size - 1) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }

                                    // Örnek cümleleri göster
                                    if (examples.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        examples.forEachIndexed { index, example ->
                                            Text(
                                                text = "Örn: $example",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            if (index < examples.size - 1) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Geri çevirmek için dokun",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Butonlar: Dişli + Biliyorum + Tekrar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Dişli/Çark butonu - Kelime Düzenle
                        FilledTonalIconButton(
                            onClick = { onWordEdit(word) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Kelimeyi Düzenle"
                            )
                        }

                        // Biliyorum
                        Button(
                            onClick = onKnownClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Biliyorum")
                        }

                        // Tekrar
                        Button(
                            onClick = onWrongClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tekrar")
                        }
                    }
                } else {
                    // Kelime yok
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz kelime bulunmuyor.\nLütfen kelime ekleyin.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lottie Animasyonu
                if (lottieComposition != null) {
                    LottieAnimation(
                        composition = lottieComposition,
                        progress = { lottieProgress },
                        modifier = Modifier
                            .size(80.dp)
                            .padding(bottom = 8.dp)
                    )
                }

                // Alt bilgi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "v:1.0.264",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "By: Tayfun YAMAK©",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
