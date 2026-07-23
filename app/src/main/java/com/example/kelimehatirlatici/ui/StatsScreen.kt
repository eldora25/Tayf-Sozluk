package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    words: List<Word>,
    onBack: () -> Unit
) {
    val totalWords = words.size
    val learnedWords = words.count { it.wrongCount == 0 }
    val notLearnedWords = totalWords - learnedWords

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İstatistikler") },
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kelime İstatistikleri", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Toplam Kelime: $totalWords", style = MaterialTheme.typography.bodyLarge)
                    Text("Öğrenilen: $learnedWords", style = MaterialTheme.typography.bodyLarge)
                    Text("Öğrenilmeyen: $notLearnedWords", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
