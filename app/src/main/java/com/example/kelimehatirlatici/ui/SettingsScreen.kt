package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    quizQuestionCount: Int,
    randomOrder: Boolean,
    memorizationThreshold: Int,
    darkMode: Boolean,
    onQuizQuestionCountChange: (Int) -> Unit,
    onRandomOrderChange: (Boolean) -> Unit,
    onMemorizationThresholdChange: (Int) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var questionCountText by remember { mutableStateOf(quizQuestionCount.toString()) }
    var thresholdText by remember { mutableStateOf(memorizationThreshold.toString()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ayarlar") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Quiz Ayarları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Quiz soru sayısı
            OutlinedTextField(
                value = questionCountText,
                onValueChange = { newValue ->
                    questionCountText = newValue.filter { it.isDigit() }
                },
                label = { Text("Quiz soru sayısı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Karışık sıralama
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sorular karışık mı?", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = randomOrder,
                    onCheckedChange = onRandomOrderChange
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Ezberleme eşik değeri
            OutlinedTextField(
                value = thresholdText,
                onValueChange = { newValue ->
                    thresholdText = newValue.filter { it.isDigit() }
                },
                label = { Text("Ezberleme eşik değeri") },
                supportingText = { Text("Bu kadar doğru cevap verince kelime ezberlenmiş sayılır") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // 🌙 GECE MODU
            Text("Görünüm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Gece Modu",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (darkMode) "Koyu tema aktif" else "Açık tema aktif",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = onDarkModeChange
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Kaydet butonu
            Button(
                onClick = {
                    val count = questionCountText.toIntOrNull()?.coerceIn(1, 100) ?: 10
                    val threshold = thresholdText.toIntOrNull()?.coerceIn(1, 20) ?: 3
                    onQuizQuestionCountChange(count)
                    onMemorizationThresholdChange(threshold)
                    questionCountText = count.toString()
                    thresholdText = threshold.toString()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Geri")
            }
        }
    }
}
