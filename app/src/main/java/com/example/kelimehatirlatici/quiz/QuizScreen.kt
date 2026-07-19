package com.example.kelimehatirlatici.quiz

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.R
import com.example.kelimehatirlatici.data.Question
import com.example.kelimehatirlatici.ui.GifImage  // GifImage composable'ını import et
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(
    session: QuizSession,
    memorizationThreshold: Int,
    onAnswerCorrect: (Question) -> Unit,
    onAnswerWrong: (Question) -> Unit,
    onMarkLearned: (Question) -> Unit,
    onSpeak: (String) -> Unit,
    onPlayCorrectSound: () -> Unit,
    onPlayWrongSound: () -> Unit,
    isSoundMuted: Boolean,
    onToggleMute: () -> Unit,
    onBack: () -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showExplanation by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var feedbackColor by remember { mutableStateOf(Color.Transparent) }
    val scope = rememberCoroutineScope()

    // ── Animasyon için state'ler ──
    var showSuccessGif by remember { mutableStateOf(false) }
    var showErrorGif by remember { mutableStateOf(false) }
    var showCongratsGif by remember { mutableStateOf(false) }

    val successAlpha by animateFloatAsState(
        targetValue = if (showSuccessGif) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "successAlpha"
    )
    val errorAlpha by animateFloatAsState(
        targetValue = if (showErrorGif) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "errorAlpha"
    )

    // ── Quiz bittiğinde congrats göster ──
    if (!session.isRunning && session.totalCorrect + session.totalWrong == session.totalQuestions) {
        showCongratsGif = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                navigationIcon = {
                    TextButton(onClick = {
                        session.isRunning = false
                        onBack()
                    }) { Text("Geri", color = Color.White) }
                },
                actions = {
                    IconButton(onClick = { onToggleMute() }) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSoundMuted) "Sesi aç" else "Sesi kapa"
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
            if (!session.isRunning) {
                // ── QUIZ BİTTİ ──
                val total = session.totalCorrect + session.totalWrong
                val correct = session.totalCorrect
                val wrong = session.totalWrong
                val time = session.elapsedSeconds
                val dakika = time / 60
                val saniye = time % 60

                Text(
                    text = "Quiz Tamamlandı!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Doğru: $correct / $total", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32))
                        Text("Yanlış: $wrong / $total", style = MaterialTheme.typography.titleLarge, color = Color(0xFFC62828))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Süre: ${dakika}d ${saniye}s",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1565C0)
                        )

                        // ── CONGRATS GIF ──
                        if (showCongratsGif) {
                            Spacer(modifier = Modifier.height(16.dp))
                            GifImage(
                                gifRes = R.raw.congrats,
                                modifier = Modifier.size(150.dp),
                                contentDescription = "Tebrikler"
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onBack) {
                            Text("Ana Ekrana Dön")
                        }
                    }
                }
            } else {
                val current = session.currentQuestion

                if (current != null) {
                    // Soru ilerleme
                    Text(
                        text = "Soru ${session.currentQuestionIndex + 1} / ${session.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Doğru/Yanlış sayacı
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        Text("✅ ${session.totalCorrect}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text("❌ ${session.totalWrong}", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Soru kelimesi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(current.word.word, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Anlamını seçin:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1976D2))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Şıklar
                    current.options.forEach { option ->
                        val buttonColor = when {
                            selectedAnswer == option && isCorrect == true -> Color(0xFF4CAF50)
                            selectedAnswer == option && isCorrect == false -> Color(0xFFE53935)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Button(
                            onClick = {
                                if (selectedAnswer == null) {
                                    selectedAnswer = option
                                    if (option == current.word.meaning) {
                                        isCorrect = true
                                        feedbackMessage = "Doğru! 🎉"
                                        feedbackColor = Color(0xFF2E7D32)
                                        onAnswerCorrect(current)
                                        onPlayCorrectSound()
                                        showSuccessGif = true
                                        showErrorGif = false
                                    } else {
                                        isCorrect = false
                                        feedbackMessage = "Yanlış! Doğru: ${current.word.meaning}"
                                        feedbackColor = Color(0xFFC62828)
                                        onAnswerWrong(current)
                                        onPlayWrongSound()
                                        showSuccessGif = false
                                        showErrorGif = true
                                    }
                                    showExplanation = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            enabled = selectedAnswer == null
                        ) {
                            Text(option, color = if (selectedAnswer != null) Color.White else Color.Unspecified)
                        }
                    }

                    // Geri bildirim
                    if (feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(feedbackMessage!!, color = feedbackColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        // ── GIF GÖSTERİMİ (doğru/yanlış) ──
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCorrect == true && showSuccessGif) {
                                GifImage(
                                    gifRes = R.raw.success,
                                    modifier = Modifier.size(100.dp),
                                    contentDescription = "Doğru"
                                )
                            } else if (isCorrect == false && showErrorGif) {
                                GifImage(
                                    gifRes = R.raw.error,
                                    modifier = Modifier.size(100.dp),
                                    contentDescription = "Yanlış"
                                )
                            }
                        }

                        if (showExplanation) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Örnek: ${current.word.example}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6A1B9A)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            // Sonraki soruya geç
                            selectedAnswer = null
                            isCorrect = null
                            showExplanation = false
                            feedbackMessage = null
                            showSuccessGif = false
                            showErrorGif = false
                            session.nextQuestion()
                        }) {
                            Text(if (session.currentQuestionIndex + 1 < session.totalQuestions) "Sonraki Soru" else "Bitir")
                        }
                    }
                }
            }
        }
    }
}
