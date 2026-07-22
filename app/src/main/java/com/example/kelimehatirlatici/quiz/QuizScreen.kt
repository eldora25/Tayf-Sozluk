package com.example.kelimehatirlatici.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.R
import com.example.kelimehatirlatici.ui.GifImage
import kotlinx.coroutines.delay

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
    onRestartQuiz: () -> Unit = {},
    onBack: () -> Unit
) {
    val question = session.currentQuestion

    var selectedAnswers by remember(question?.word?.id) { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var correctAnswerSelected by remember(question?.word?.id) { mutableStateOf(false) }

    var showGif by remember(question?.word?.id) { mutableStateOf(false) }
    var lastAnswerCorrect by remember(question?.word?.id) { mutableStateOf(false) }

    var muteState by remember { mutableStateOf(isSoundMuted) }

    LaunchedEffect(isSoundMuted) { muteState = isSoundMuted }

    LaunchedEffect(question?.word?.id) {
        selectedAnswers = emptyMap()
        correctAnswerSelected = false
        showGif = false
        lastAnswerCorrect = false
        if (!muteState && question != null) {
            delay(400)
            onSpeak(question.word.word)
        }
    }

    LaunchedEffect(session.isRunning) {
        while (session.isRunning && !session.isFinished) {
            delay(1000L)
            session.elapsedTime += 1000L
        }
    }

    LaunchedEffect(correctAnswerSelected) {
        if (correctAnswerSelected) {
            delay(500)
            question?.let { q ->
                if (q.word.quizCorrectCount + 1 >= memorizationThreshold) {
                    onMarkLearned(q)
                }
            }
            if (session.currentIndex + 1 >= session.totalQuestions) {
                session.isFinished = true
                session.isRunning = false
            } else {
                session.currentQuestionIndex++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz") },
                navigationIcon = {
                    IconButton(onClick = {
                        session.isRunning = false
                        onBack()
                    }) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            if (session.isFinished) {
                // ========== QUIZ BİTTİ EKRANI ==========
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Quiz Tamamlandı! 🎉",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 32.sp)
                        Text(
                            "Doğru: ${session.correctCount}",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❌", fontSize = 32.sp)
                        Text(
                            "Yanlış: ${session.wrongCount}",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Süre: ${session.formattedTime}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GifImage(
                        gifRes = R.raw.congrats,
                        modifier = Modifier.size(120.dp),
                        contentDescription = "Tebrikler"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ========== YENİ QUIZ BUTONU ==========
                Button(
                    onClick = {
                        onRestartQuiz()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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

                // Geri Butonu
                OutlinedButton(
                    onClick = {
                        session.isRunning = false
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Geri")
                }

            } else if (question == null) {
                Text("Quiz için yeterli kelime yok.", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = {
                    session.isRunning = false
                    onBack()
                }, modifier = Modifier.fillMaxWidth()) { Text("Geri") }

            } else {
                // ========== QUIZ SORU EKRANI ==========
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        muteState = !muteState
                        onToggleMute()
                    }, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = if (!muteState) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (!muteState) "Sesi kapat" else "Sesi aç",
                            tint = if (!muteState) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                        Text(" ${session.correctCount}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                        Text(" ${session.formattedTime}", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF424242))
                    }
                    Text("${session.currentQuestionNumber}/${session.totalQuestions}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(22.dp))
                        Text(" ${session.wrongCount}", color = Color(0xFFF44336), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE3F2FD))
                        .clickable { if (!muteState) onSpeak(question.word.word) }
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(question.word.word, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = if (!muteState) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Seslendir",
                            tint = if (!muteState) Color(0xFF1976D2) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                question.options.forEach { option ->
                    val isSelectedWrong = selectedAnswers[option] == false
                    val isSelectedCorrect = selectedAnswers[option] == true
                    val isDisabled = correctAnswerSelected
                    val canClick = !isDisabled && selectedAnswers[option] == null

                    val bgColor = when {
                        isSelectedCorrect -> Color(0xFFE8F5E9)
                        isSelectedWrong -> Color(0xFFFFEBEE)
                        else -> Color(0xFFF3E5F5)
                    }
                    val borderColor = when {
                        isSelectedCorrect -> Color(0xFF4CAF50)
                        isSelectedWrong -> Color(0xFFF44336)
                        else -> Color(0xFFAB47BC)
                    }
                    val radioColor = when {
                        isSelectedCorrect -> Color(0xFF4CAF50)
                        isSelectedWrong -> Color(0xFFF44336)
                        else -> Color(0xFFAB47BC)
                    }

                    Button(
                        onClick = {
                            if (canClick) {
                                val isCorrect = option == question.correctAnswer
                                selectedAnswers = selectedAnswers + (option to isCorrect)

                                lastAnswerCorrect = isCorrect
                                showGif = true

                                if (isCorrect) {
                                    session.correctCount++
                                    onAnswerCorrect(question)
                                    onPlayCorrectSound()
                                    correctAnswerSelected = true
                                } else {
                                    session.wrongCount++
                                    onAnswerWrong(question)
                                    onPlayWrongSound()
                                }
                            }
                        },
                        enabled = canClick,
                        colors = ButtonDefaults.buttonColors(containerColor = bgColor, disabledContainerColor = bgColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .height(56.dp)
                            .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(22.dp).border(2.dp, radioColor, CircleShape), contentAlignment = Alignment.Center) {
                                    if (isSelectedCorrect || isSelectedWrong) {
                                        Box(modifier = Modifier.size(12.dp).background(radioColor, CircleShape))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(option, color = Color(0xFF212121), fontWeight = if (isSelectedCorrect || isSelectedWrong) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp)
                            }
                            if (isSelectedCorrect) Icon(Icons.Default.Check, contentDescription = "Doğru", tint = Color(0xFF4CAF50), modifier = Modifier.size(26.dp))
                            else if (isSelectedWrong) Icon(Icons.Default.Close, contentDescription = "Yanlış", tint = Color(0xFFF44336), modifier = Modifier.size(26.dp))
                        }
                    }
                }

                if (showGif) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp, max = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GifImage(
                            gifRes = if (lastAnswerCorrect) R.raw.success else R.raw.error,
                            modifier = Modifier.size(60.dp),
                            contentDescription = if (lastAnswerCorrect) "Doğru" else "Yanlış"
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
