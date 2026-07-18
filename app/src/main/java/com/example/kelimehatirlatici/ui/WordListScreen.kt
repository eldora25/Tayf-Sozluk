package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    words: List<Word>,
    onUpdateWord: (Int, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var editingWord by remember { mutableStateOf<Word?>(null) }
    var editWordText by remember { mutableStateOf("") }
    var editMeaningText by remember { mutableStateOf("") }
    var editExampleText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kelime Listesi (${words.size})") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (words.isEmpty()) {
                Text("Bu kütüphane ve seviyede kelime yok.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(words) { w ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(w.word, fontWeight = FontWeight.Bold)
                                    Text(w.meaning, style = MaterialTheme.typography.bodyMedium)
                                    if (w.example.isNotBlank()) {
                                        Text(w.example, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                                IconButton(onClick = {
                                    editingWord = w
                                    editWordText = w.word
                                    editMeaningText = w.meaning
                                    editExampleText = w.example
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }

    // ────── DÜZENLEME DİALOGU ──────
    if (editingWord != null) {
        AlertDialog(
            onDismissRequest = { editingWord = null },
            title = { Text("Kelimeyi Düzenle") },
            text = {
                Column {
                    OutlinedTextField(value = editWordText, onValueChange = { editWordText = it }, label = { Text("Kelime") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editMeaningText, onValueChange = { editMeaningText = it }, label = { Text("Anlam") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editExampleText, onValueChange = { editExampleText = it }, label = { Text("Örnek Cümle") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = editingWord!!.id
                    editingWord = null
                    onUpdateWord(id, editWordText.trim(), editMeaningText.trim(), editExampleText.trim())
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { editingWord = null }) { Text("İptal") }
            }
        )
    }
}
