package com.example.kelimehatirlatici.quiz

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ★ Yeni: Çoklu anlamdan sadece ilkini al
private fun getFirstMeaning(meaning: String): String {
    return meaning.split("|||").firstOrNull()?.trim()?.ifBlank { meaning } ?: meaning
}

// ★ Yeni: Çoklu örnek cümleden sadece ilkini al
private fun getFirstExample(example: String): String {
    return example.split("|||").firstOrNull()?.trim()?.ifBlank { example } ?: example
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onRestartQuiz: () -> Unit,
    onBack: () -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showAnswer by remember { mutableStateOf(false) }
    var showMarkAsLearnedDialog by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(session.questions) {
        if (session.questions.isNotEmpty()) {
            currentQuestion = session.questions.getOrNull(currentIndex)
        }
        currentIndex = 0
        correctCount = 0
        wrongCount = 0
        quizFinished = false
        elapsedSeconds = 0
    }

    LaunchedEffect(showAnswer) {
        if (!showAnswer && !quizFinished) {
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    val formatTime = { seconds: Int ->
        val min = seconds / 60
        val sec = seconds % 60
        "%02d:%02d".format(min, sec)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                actions = {
                    if (currentQuestion != null) {
                        Text(
                            "${currentIndex + 1}/${session.questions.size}",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        formatTime(elapsedSeconds),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    // ★ Düzeltme: R.drawable.sound_off/sound_on yerine Material Icon kullan
                    IconButton(onClick = onToggleMute) {
                        if (isSoundMuted) {
                            Icon(Icons.Default.VolumeOff, contentDescription = "Sesi Aç")
                        } else {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Sesi Kapa")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (quizFinished) {
                // ═══ QUIZ BİTTİ EKRANI ═══
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🎉 Quiz Tamamlandı!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Sonuçlar", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$correctCount",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                    Text("Doğru", color = Color(0xFF4CAF50))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$wrongCount",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                    Text("Yanlış", color = Color(0xFFD32F2F))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        formatTime(elapsedSeconds),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("Süre")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val totalQuestions = correctCount + wrongCount
                            val pct = if (totalQuestions > 0) (correctCount * 100 / totalQuestions) else 0
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = if (pct >= 70) Color(0xFF4CAF50) else if (pct >= 40) Color(0xFFFFC107) else Color(0xFFD32F2F),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("%$pct Başarı", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onRestartQuiz()
                            currentIndex = 0
                            correctCount = 0
                            wrongCount = 0
                            quizFinished = false
                            elapsedSeconds = 0
                            showAnswer = false
                            selectedAnswer = null
                            isCorrect = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🔄 Tekrar Dene") }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Ana Sayfaya Dön") }
                }
            } else if (currentQuestion != null) {
                // ═══ SORU EKRANI ═══
                val question = currentQuestion!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / session.questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("✅ $correctCount", fontWeight = FontWeight.Bold)
                            Text("❌ $wrongCount", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Kelime kartı - anlamı göster, kelimeyi sor
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ★ Değişiklik: Çoklu anlamdan sadece ilkini göster
                            Text(
                                getFirstMeaning(question.word.meaning),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // ★ Değişiklik: Çoklu örnek cümleden sadece ilkini göster
                            if (question.word.example.isNotBlank()) {
                                Text(
                                    "\"${getFirstExample(question.word.example)}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }

                            Text(
                                "?",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Bu kelimenin anlamı nedir?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (question.word.word.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onSpeak(question.word.word) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("🔊 ${question.word.word}") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Şıklar
                    question.options.forEachIndexed { index, option ->
                        val buttonColor = when {
                            showAnswer && option == question.correctAnswer -> Color(0xFF4CAF50)
                            showAnswer && option == selectedAnswer && isCorrect == false -> Color(0xFFD32F2F)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            showAnswer && option == question.correctAnswer -> Color.White
                            showAnswer && option == selectedAnswer && isCorrect == false -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Button(
                            onClick = {
                                if (!showAnswer) {
                                    selectedAnswer = option
                                    isCorrect = option == question.correctAnswer
                                    showAnswer = true

                                    if (isCorrect == true) {
                                        onPlayCorrectSound()
                                        onAnswerCorrect(question)
                                        if (question.word.quizCorrectCount + 1 >= memorizationThreshold) {
                                            showMarkAsLearnedDialog = true
                                        }
                                    } else {
                                        onPlayWrongSound()
                                        onAnswerWrong(question)
                                    }

                                    coroutineScope.launch {
                                        delay(1500)
                                        if (isCorrect == true) correctCount++ else wrongCount++

                                        val nextIndex = currentIndex + 1
                                        if (nextIndex < session.questions.size) {
                                            currentIndex = nextIndex
                                            currentQuestion = session.questions[nextIndex]
                                            showAnswer = false
                                            selectedAnswer = null
                                            isCorrect = null
                                            session.correctCount = correctCount
                                            session.wrongCount = wrongCount
                                        } else {
                                            quizFinished = true
                                            session.correctCount = correctCount
                                            session.wrongCount = wrongCount
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                contentColor = textColor
                            ),
                            enabled = !showAnswer
                        ) { Text(option, fontSize = 16.sp) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!showAnswer) {
                        OutlinedButton(
                            onClick = {
                                showAnswer = true
                                onPlayWrongSound()
                                onAnswerWrong(question)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Cevabı Göster") }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Quiz için yeterli kelime bulunamadı.") }
            }
        }
    }

    // Öğrenildi olarak işaretle dialogu
    if (showMarkAsLearnedDialog && currentQuestion != null) {
        AlertDialog(
            onDismissRequest = { showMarkAsLearnedDialog = false },
            title = { Text("Tebrikler! 🎉") },
            text = {
                Column {
                    Text(
                        "\"${getFirstMeaning(currentQuestion!!.word.meaning)}\" kelimesini öğrendiniz.",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Öğrenildi olarak işaretlemek ister misiniz?")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onMarkLearned(currentQuestion!!)
                    showMarkAsLearnedDialog = false
                }) { Text("Evet, Öğrendim") }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAsLearnedDialog = false }) { Text("Hayır") }
            }
        )
    }
}
