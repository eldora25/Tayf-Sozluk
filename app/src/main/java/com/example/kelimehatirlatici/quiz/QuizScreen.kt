package com.example.kelimehatirlatici.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.R
import com.example.kelimehatirlatici.ui.GifImage

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
    onBack: () -> Unit
) {
    val currentQuestion = session.currentQuestion
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var totalCorrect by remember { mutableIntStateOf(0) }
    var totalWrong by remember { mutableIntStateOf(0) }
    var showGif by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }

    if (session.isFinished) {
        QuizCompletedScreen(
            totalCorrect = session.correctCount,
            totalWrong = session.wrongCount,
            totalQuestions = session.questions.size,
            elapsedTime = session.elapsedTime,
            onBack = onBack
        )
        return
    }

    if (currentQuestion == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Soru bulunamadı")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Soru ${session.currentQuestionIndex + 1} / ${session.questions.size}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(onClick = onBack) { Text("Çık") }
            FilledTonalButton(onClick = onToggleMute) {
                Text(if (isSoundMuted) "🔇 Ses Kapalı" else "🔊 Ses Açık")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = currentQuestion.word.word,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onSpeak(currentQuestion.word.word) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("🔊 Seslendir") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(currentQuestion.options) { option ->  // option tipi: String
                val isSelected = selectedAnswer == option
                val isCorrect = option == currentQuestion.correctAnswer

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = {
                        if (!showResult) {
                            selectedAnswer = option
                            showResult = true
                            showGif = true
                            if (option == currentQuestion.correctAnswer) {
                                lastAnswerCorrect = true
                                totalCorrect++
                                onAnswerCorrect(currentQuestion)
                                if (!isSoundMuted) onPlayCorrectSound()
                            } else {
                                lastAnswerCorrect = false
                                totalWrong++
                                onAnswerWrong(currentQuestion)
                                if (!isSoundMuted) onPlayWrongSound()
                            }
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            !showResult -> MaterialTheme.colorScheme.surfaceVariant
                            isSelected && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                            isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            showResult && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (showGif) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                GifImage(
                    gifRes = if (lastAnswerCorrect) R.raw.success else R.raw.error,
                    modifier = Modifier.size(80.dp),
                    contentDescription = if (lastAnswerCorrect) "Doğru" else "Yanlış"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showResult) {
            Button(
                onClick = {
                    showResult = false
                    showGif = false
                    selectedAnswer = null
                    session.nextQuestion()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sonraki Soru ➡️")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Doğru: $totalCorrect / Yanlış: $totalWrong",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun QuizCompletedScreen(
    totalCorrect: Int,
    totalWrong: Int,
    totalQuestions: Int,
    elapsedTime: Long,
    onBack: () -> Unit
) {
    val minutes = elapsedTime / 60000
    val seconds = (elapsedTime % 60000) / 1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp),
            contentAlignment = Alignment.Center
        ) {
            GifImage(
                gifRes = R.raw.congrats,
                modifier = Modifier.size(150.dp),
                contentDescription = "Tebrikler"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quiz Tamamlandı!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Süre: ${minutes}dak ${seconds}saniye",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Doğru: $totalCorrect / $totalQuestions  |  Yanlış: $totalWrong",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBack) {
            Text("Geri Dön")
        }
    }
}
