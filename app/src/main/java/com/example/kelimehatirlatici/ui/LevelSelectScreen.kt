package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LevelSelectScreen(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val levels = listOf(
        "Genel",
        "A1",
        "A2",
        "B1",
        "B2",
        "C1",
        "C2"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Seviye Seç")
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
            levels.forEach { level ->
                val isSelected = level == selectedLevel

                if (isSelected) {
                    Button(
                        onClick = {
                            onLevelSelected(level)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(level)
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            onLevelSelected(level)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(level)
                    }
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
