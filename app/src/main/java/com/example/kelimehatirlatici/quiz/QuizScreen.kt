package com.example.kelimehatirlatici.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    session: QuizSession,
    memorizationThreshold: Int,
    onAnswerCorrect: (QuizQuestion) -> Unit,
    onAnswerWrong: (QuizQuestion) -> Unit,
    onMarkLearned: (QuizQuestion) -> Unit,
    onBack: () -> Unit
) {
    val question = session.currentQuestion

    var selectedAnswer by remember(question?.word?.id) { mutableStateOf<String?>(null) }
    var answerState by remember(question?.word?.id) { mutableStateOf("") } // "" | "correct" | "wrong"

    // Timer
    LaunchedEffect(session.isRunning) {
        while (session.isRunning && !session.isFinished) {
            delay(1000L)
            session.elapsedSeconds++
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Quiz") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        ) {
            // ────── ÜST BİLGİ SATIRI ──────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Doğru sayısı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✅", style = MaterialTheme.typography.titleMedium)
                    Text(" ${session.correctCount}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF4CAF50))
                }
                // Zaman
                Text(session.formattedTime, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                // Soru numarası
                Text("${session.currentQuestionNumber}/${session.totalQuestions}", style = MaterialTheme.typography.titleMedium)
                // Yanlış sayısı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❌", style = MaterialTheme.typography.titleMedium)
                    Text(" ${session.wrongCount}", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF44336))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (session.isFinished) {
                // ────── BİTİŞ EKRANI ──────
                Spacer(modifier = Modifier.height(32.dp))
                Text("Quiz Tamamlandı! 🎉", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Doğru: ${session.correctCount}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleLarge)
                Text("Yanlış: ${session.wrongCount}", color = Color(0xFFF44336), style = MaterialTheme.typography.titleLarge)
                Text("Süre: ${session.formattedTime}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
            } else if (question == null) {
                Text("Quiz için yeterli kelime yok (öğrenilmemiş kelime kalmadı).")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
            } else {
                // ────── SORU EKRANI ──────
                Text("Bu kelimenin anlamı nedir?", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(question.word.word, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                question.options.forEach { option ->
                    val isSelected = selectedAnswer == option
                    val buttonColor = when {
                        isSelected && answerState == "correct" -> Color(0xFF4CAF50)
                        isSelected && answerState == "wrong" -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Button(
                        onClick = {
                            if (selectedAnswer == null) {
                                selectedAnswer = option
                                val correct = option == question.correctAnswer
                                if (correct) {
                                    answerState = "correct"
                                    session.correctCount++
                                    onAnswerCorrect(question)
                                } else {
                                    answerState = "wrong"
                                    session.wrongCount++
                                    onAnswerWrong(question)
                                }
                            }
                        },
                        enabled = selectedAnswer == null,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(option, color = if (isSelected) Color.White else Color.Unspecified)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (answerState == "correct") {
                    Text("✅ Doğru!", color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // QuizCorrectCount kontrolü
                            if (question.word.quizCorrectCount + 1 >= memorizationThreshold) {
                                onMarkLearned(question)
                            }
                            // Sonraki soru
                            if (session.currentIndex + 1 >= session.totalQuestions) {
                                session.isFinished = true
                                session.isRunning = false
                            } else {
                                session.currentIndex++
                                selectedAnswer = null
                                answerState = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (session.currentIndex + 1 >= session.totalQuestions) "Sonuçları Gör" else "Sonraki Soru")
                    }
                }

                if (answerState == "wrong") {
                    Text(
                        "❌ Yanlış! Doğru cevap: ${question.correctAnswer}",
                        color = Color(0xFFF44336),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Aynı soruyu tekrar dene (index değişmez, sadece state sıfırlanır)
                            selectedAnswer = null
                            answerState = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Tekrar Dene") }
                }

                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { session.isRunning = false; onBack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Quiz'den Çık")
                }
            }
        }
    }
}
