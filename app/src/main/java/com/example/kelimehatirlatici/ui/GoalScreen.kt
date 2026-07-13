package com.example.kelimehatirlatici.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GoalScreen(
    currentGoal: Int,
    completed: Int,
    onSaveGoal: (Int) -> Unit,
    onBack: () -> Unit
) {
    var goalText by remember(currentGoal) {
        mutableStateOf(currentGoal.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Günlük Hedef")
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
            Text(
                text = "Bugün tamamlanan: $completed",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = goalText,
                onValueChange = {
                    goalText = it.filter { char -> char.isDigit() }
                },
                label = {
                    Text("Günlük kelime hedefi")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val value = goalText.toIntOrNull() ?: currentGoal
                    onSaveGoal(value.coerceAtLeast(1))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Geri")
            }
        }
    }
}
