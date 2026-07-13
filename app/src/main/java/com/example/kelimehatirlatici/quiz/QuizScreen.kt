package com.example.kelimehatirlatici.quiz

import androidx.compose.material3.ExperimentalMaterial3Api // bu satırı ekle

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    question: QuizQuestion?,
    onAnswerSelected: (Boolean) -> Unit,
    onNextQuestion: () -> Unit,
    onBack: () -> Unit
) {
    var selectedAnswer by remember(question?.word?.id) {
        mutableStateOf<String?>(null)
    }

    var isCorrect by remember(question?.word?.id) {
        mutableStateOf<Boolean?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Quiz")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (question == null) {
                Text("Quiz için yeterli kelime yok.")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Geri")
                }
            } else {
                Text(
                    text = "Bu kelimenin anlamı nedir?",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = question.word.word,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                question.options.forEach { option ->
                    val enabled = selectedAnswer == null

                    OutlinedButton(
                        onClick = {
                            selectedAnswer = option
                            val correct = option == question.correctAnswer
                            isCorrect = correct
                            onAnswerSelected(correct)
                        },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(option)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isCorrect != null) {
                    if (isCorrect == true) {
                        Text(
                            text = "Doğru!",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "Yanlış. Doğru cevap: ${question.correctAnswer}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNextQuestion,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sonraki Soru")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Geri")
                }
            }
        }
    }
}
