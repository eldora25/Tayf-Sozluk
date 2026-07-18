package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    quizQuestionCount: Int,
    randomOrder: Boolean,
    memorizationThreshold: Int,
    onQuizQuestionCountChange: (Int) -> Unit,
    onRandomOrderChange: (Boolean) -> Unit,
    onMemorizationThresholdChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    var countText by remember(quizQuestionCount) { mutableStateOf(quizQuestionCount.toString()) }
    var thresholdText by remember(memorizationThreshold) { mutableStateOf(memorizationThreshold.toString()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ayarlar") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()
        ) {
            Text("Quiz Ayarları", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Soru sayısı
            OutlinedTextField(
                value = countText,
                onValueChange = { countText = it.filter { c -> c.isDigit() } },
                label = { Text("Quiz soru sayısı") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Karışık / Alfabetik
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Sorular karışık gelsin mi?", modifier = Modifier.weight(1f))
                Switch(checked = randomOrder, onCheckedChange = onRandomOrderChange)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Ezberleme eşik değeri
            OutlinedTextField(
                value = thresholdText,
                onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                label = { Text("Ezberleme eşik değeri (tekrar)") },
                supportingText = { Text("Bu kadar doğru cevap verince kelime ezberlenmiş sayılır") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val count = countText.toIntOrNull() ?: 10
                    val threshold = thresholdText.toIntOrNull() ?: 3
                    onQuizQuestionCountChange(count.coerceIn(1, 100))
                    onMemorizationThresholdChange(threshold.coerceIn(1, 20))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kaydet") }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }
}
