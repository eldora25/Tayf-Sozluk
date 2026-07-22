package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.Word

// ★ Yeni: ||| ile ayrılmış çoklu değerleri listeye çevirir
private fun parseMultiValue(text: String): List<String> {
    return text.split("|||").map { it.trim() }.filter { it.isNotBlank() }
}

// ★ Yeni: Çoklu anlamları alt alta görüntüleme
private fun formatMultiMeanings(meaning: String): String {
    val parts = parseMultiValue(meaning)
    return if (parts.size > 1) {
        parts.joinToString("\n") { "• $it" }
    } else {
        meaning
    }
}

// ★ Yeni: Çoklu örnekleri alt alta görüntüleme
private fun formatMultiExamples(example: String): String {
    val parts = parseMultiValue(example)
    return if (parts.size > 1) {
        parts.joinToString("\n") { "• \"$it\"" }
    } else if (example.isNotBlank()) {
        "\"$example\""
    } else {
        ""
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongWordsScreen(
    words: List<Word>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Yanlış Kelimeler (${words.size})") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (words.isEmpty()) {
                Text("Yanlış kelime bulunmuyor.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(words) { w ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(w.word, fontWeight = FontWeight.Bold)
                                    // Yanlış sayısı
                                    Text(
                                        "❌ ${w.wrongCount}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFD32F2F)
                                    )
                                }

                                // ★ Değişiklik: Çoklu anlamları alt alta • ile göster
                                Text(
                                    text = formatMultiMeanings(w.meaning),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                // ★ Değişiklik: Çoklu örnekleri alt alta • ile göster
                                if (w.example.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatMultiExamples(w.example),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                // Metadata
                                Text(
                                    "Kütüphane: ${w.library} | Seviye: ${w.level}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }
}
