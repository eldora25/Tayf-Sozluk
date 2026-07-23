package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    currentGoal: Int = 10,
    completed: Int = 0,
    onSaveGoal: (Int) -> Unit,
    onBack: () -> Unit
) {
    var targetCount by remember { mutableStateOf(currentGoal.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Günlük Hedef") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bugünkü İlerleme",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$completed / $currentGoal",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = targetCount,
                onValueChange = { targetCount = it },
                label = { Text("Günlük Hedef") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val target = targetCount.toIntOrNull() ?: 10
                    onSaveGoal(target)
                }
            ) {
                Text("Kaydet")
            }
        }
    }
}
