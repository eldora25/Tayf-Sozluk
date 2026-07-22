package com.example.kelimehatirlatici.quiz

import android.content.ContentValues
import android.content.Context
import android.media.ToneGenerator
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.AppSettings
import com.example.kelimehatirlatici.R
import com.example.kelimehatirlatici.Word
import com.example.kelimehatirlatici.data.AppDatabase
import com.example.kelimehatirlatici.data.WordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun QuizScreen(
    kelimeListesi: List<Word>,
    maxQuestions: Int = 20,
    appSettings: AppSettings,
    wordDao: WordDao? = null,
    onFinish: () -> Unit = {}
) {
    val context = LocalContext.current
    val quizGenerator = remember {
        QuizGenerator(wordDao ?: AppDatabase.getInstance(context).wordDao())
    }
    val session = remember { QuizSession() }
    var soundEnabled by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }

    // Quiz'i başlat
    LaunchedEffect(Unit) {
        if (!hasStarted) {
            val questions = quizGenerator.generateQuestions(kelimeListesi, maxQuestions)
            session.start(questions)
            hasStarted = true
        }
    }

    // Ses çalma
    val toneGenerator = remember { ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 50) }

    fun playCorrectSound() {
        if (soundEnabled) {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            } catch (e: Exception) {
                Log.e("QuizScreen", "Ses çalma hatası", e)
            }
        }
    }

    fun playWrongSound() {
        if (soundEnabled) {
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 300)
            } catch (e: Exception) {
                Log.e("QuizScreen", "Ses çalma hatası", e)
            }
        }
    }

    fun answerSelected(answer: String) {
        if (showResult) return

        selectedAnswer = answer
        isCorrect = answer == session.currentQuestion?.correctAnswer
        showResult = true

        if (isCorrect) {
            playCorrectSound()
        } else {
            playWrongSound()
        }

        session.answerCurrentQuestion(isCorrect)
    }

    fun nextQuestion() {
        showResult = false
        selectedAnswer = null

        if (!session.nextQuestion()) {
            // Quiz bitti
        }
    }

    // ========== YENİ QUIZ BAŞLATMA FONKSİYONU ==========
    // Önceden seçilmiş ayarlarla (kelimeListesi, maxQuestions) yeni quiz başlatır
    fun startNewQuiz() {
        coroutineScope.launch {
            val questions = quizGenerator.generateQuestions(
                kelimeListesi = kelimeListesi,
                maxQuestions = maxQuestions
            )
            session.start(questions)
            showResult = false
            selectedAnswer = null
        }
    }

    if (!session.isFinished) {
        // ---------- QUIZ EKRANI ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst bilgi çubuğu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Soru ${session.currentQuestionNumber}/${session.totalQuestions}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ses butonu
                    IconButton(onClick = { soundEnabled = !soundEnabled }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Ses"
                        )
                    }

                    // Süre
                    Text(
                        text = session.formattedElapsedTime,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İlerleme çubuğu
            LinearProgressIndicator(
                progress = {
                    if (session.totalQuestions > 0)
                        (session.currentQuestionNumber - 1).toFloat() / session.totalQuestions
                    else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Soru kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Kelime
                    Text(
                        text = session.currentQuestion?.word?.word ?: "",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ek bilgi
                    Text(
                        text = session.currentQuestion?.word?.detail ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Anlamını seçin:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Şıklar
            val options = session.currentQuestion?.options ?: emptyList()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val buttonColor = when {
                        showResult && option == session.currentQuestion?.correctAnswer -> Color(0xFF4CAF50)
                        showResult && option == selectedAnswer && !isCorrect -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val textColor = when {
                        showResult && option == session.currentQuestion?.correctAnswer -> Color.White
                        showResult && option == selectedAnswer && !isCorrect -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Button(
                        onClick = { answerSelected(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        ),
                        enabled = !showResult
                    ) {
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sonraki butonu
            if (showResult) {
                Button(
                    onClick = { nextQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (session.currentQuestionNumber < session.totalQuestions) "Sonraki Soru" else "Sonuçları Göster",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        // ---------- SONUÇ EKRANI (QUIZ BİTTİ) ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Görsel
            Image(
                painter = painterResource(id = R.drawable.study_gif),
                contentDescription = "Study GIF",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Quiz Tamamlandı! 🎉",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // İstatistik kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "İstatistikler",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Doğru
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "${session.correctCount}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text("Doğru", color = Color.Gray)
                        }

                        // Toplam
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "${session.totalQuestions}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Toplam", color = Color.Gray)
                        }

                        // Yanlış
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "${session.wrongCount}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                            Text("Yanlış", color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Başarı yüzdesi
                    val yuzde = if (session.totalQuestions > 0) {
                        (session.correctCount * 100) / session.totalQuestions
                    } else 0

                    LinearProgressIndicator(
                        progress = { yuzde / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (yuzde >= 70) Color(0xFF4CAF50)
                        else if (yuzde >= 40) Color(0xFFFFC107)
                        else Color(0xFFF44336),
                        trackColor = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Başarı: %$yuzde",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Süre: ${session.formattedElapsedTime}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========== YENİ QUIZ BUTONU ==========
            // ÖNCEKİ AYARLARLA (kelimeListesi, maxQuestions) YENİ QUIZ BAŞLATIR
            Button(
                onClick = { startNewQuiz() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Quiz",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ana Menü Butonu
            OutlinedButton(
                onClick = { onFinish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ana Menü",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
