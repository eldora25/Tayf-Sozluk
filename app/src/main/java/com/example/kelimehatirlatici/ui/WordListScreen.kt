package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.Word
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    words: List<Word>,
    onUpdateWord: (id: Int, word: String, meaning: String, meanings: String, example: String, examples: String, level: String, library: String) -> Unit,
    onDeleteWord: (Int) -> Unit,
    onMoveWord: (Int, String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelime Listesi (${words.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz kelime bulunmuyor.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                items(words) { word ->
                    WordListItem(
                        word = word,
                        onUpdateWord = onUpdateWord,
                        onDeleteWord = onDeleteWord,
                        onMoveWord = onMoveWord
                    )
                }
            }
        }
    }
}

@Composable
private fun WordListItem(
    word: Word,
    onUpdateWord: (id: Int, word: String, meaning: String, meanings: String, example: String, examples: String, level: String, library: String) -> Unit,
    onDeleteWord: (Int) -> Unit,
    onMoveWord: (Int, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var editWord by remember { mutableStateOf(word.word) }
    var editMeaning by remember { mutableStateOf(word.meaning) }
    var editMeanings by remember { mutableStateOf(word.meanings) }
    var editExample by remember { mutableStateOf(word.example) }
    var editExamples by remember { mutableStateOf(word.examples) }
    var editLevel by remember { mutableStateOf(word.level) }
    var editLibrary by remember { mutableStateOf(word.library) }

    // Çoklu anlamları parse et
    val meaningsList = remember(word) {
        try {
            val arr = JSONArray(word.meanings)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            listOf(word.meaning)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium
                )
                if (meaningsList.isNotEmpty()) {
                    Text(
                        text = meaningsList.joinToString(" | "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Row {
                    Text(
                        text = word.library,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = word.level,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (word.wrongCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hata: ${word.wrongCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
