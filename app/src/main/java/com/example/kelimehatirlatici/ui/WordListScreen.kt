package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kelimehatirlatici.data.Word

// Çoklu değerleri listeye çevirir
private fun parseMultiValue(text: String): List<String> {
    return text.split("|||").map { it.trim() }.filter { it.isNotBlank() }
}

// Çoklu anlamları alt alta görüntüleme
private fun formatMultiMeanings(meaning: String): String {
    val parts = parseMultiValue(meaning)
    return if (parts.size > 1) {
        parts.joinToString("\n") { "• $it" }
    } else {
        meaning
    }
}

// Çoklu örnekleri alt alta görüntüleme
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
fun WordListScreen(
    words: List<Word>,
    onUpdateWord: (Int, String, String, String, String) -> Unit,
    onDeleteWord: (Int) -> Unit,
    onBack: () -> Unit
) {
    var editingWord by remember { mutableStateOf<Word?>(null) }
    var editWordText by remember { mutableStateOf("") }
    var editMeaningText by remember { mutableStateOf("") }
    var editExampleText by remember { mutableStateOf("") }
    var editLevelText by remember { mutableStateOf("Genel") }
    var editMode by remember { mutableStateOf("update") }
    var editMessage by remember { mutableStateOf("") }
    var showEditSuccess by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kelime Listesi (${words.size})" }) }
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
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(w.word, fontWeight = FontWeight.Bold)

                                    // Çoklu anlamları alt alta • ile göster
                                    Text(
                                        text = formatMultiMeanings(w.meaning),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    // Çoklu örnekleri alt alta • ile göster
                                    if (w.example.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatMultiExamples(w.example),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    // Seviye bilgisi
                                    Text(
                                        "Seviye: ${w.level}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1976D2)
                                    )
                                }
                                // ★ ÇARK İKONU - Düzenle/Sil
                                IconButton(onClick = {
                                    editingWord = w
                                    editWordText = w.word
                                    editMeaningText = w.meaning
                                    editExampleText = w.example
                                    editLevelText = w.level
                                    editMode = "update"
                                    editMessage = ""
                                    showEditSuccess = false
                                }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Düzenle/Sil",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
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

    // ════════════════════════════════════════════════════════════
    // DÜZENLEME / SİL DİALOGU
    // ════════════════════════════════════════════════════════════
    if (editingWord != null) {
        AlertDialog(
            onDismissRequest = { editingWord = null },
            title = {
                Column {
                    Text("Kelimeyi Düzenle", fontWeight = FontWeight.Bold)
                    if (showEditSuccess) {
                        Text(text = editMessage, color = Color(0xFF4CAF50), fontSize = 14.sp)
                    }
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editWordText,
                        onValueChange = { editWordText = it },
                        label = { Text("Kelime") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editMeaningText,
                        onValueChange = { editMeaningText = it },
                        label = { Text("Anlam") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Birden fazla anlam varsa ||| ile ayırın", fontSize = 10.sp, color = Color.Gray)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editExampleText,
                        onValueChange = { editExampleText = it },
                        label = { Text("Örnek Cümle") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Birden fazla örnek varsa ||| ile ayırın", fontSize = 10.sp, color = Color.Gray)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Seviye seçimi
                    Text("Seviye", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2", "Genel")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        levels.forEach { lvl ->
                            FilterChip(
                                selected = editLevelText == lvl,
                                onClick = { editLevelText = lvl },
                                label = { Text(lvl, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // İşlem türü (Güncelle / Sil)
                    Text("İşlem Türü", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = editMode == "update",
                            onClick = { editMode = "update" },
                            label = { Text("Güncelle", fontSize = 12.sp) },
                            leadingIcon = {
                                if (editMode == "update") Icon(
                                    Icons.Default.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = editMode == "delete",
                            onClick = { editMode = "delete" },
                            label = { Text("🗑 Sil", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFCDD2)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (editMode == "delete") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Kelime tamamen silinecek! Bu işlem geri alınamaz.",
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            },
            confirmButton = {
                if (editMode == "delete") {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        )
                    ) { Text("🗑 Sil") }
                } else {
                    Button(
                        onClick = {
                            val id = editingWord!!.id
                            val finalLevel = if (editLevelText.isBlank()) "Genel" else editLevelText
                            editMessage = "✅ Kelime güncellendi!"
                            showEditSuccess = true
                            editingWord = null
                            onUpdateWord(
                                id,
                                editWordText.trim(),
                                editMeaningText.trim(),
                                editExampleText.trim(),
                                finalLevel
                            )
                        },
                        enabled = editWordText.isNotBlank() && editMeaningText.isNotBlank()
                    ) { Text("Kaydet") }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    editingWord = null
                    editMessage = ""
                    showEditSuccess = false
                }) { Text("İptal") }
            }
        )
    }

    // ════════════════════════════════════════════════════════════
    // SİLME ONAY DİALOGU
    // ════════════════════════════════════════════════════════════
    if (showDeleteConfirm && editingWord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Kelimeyi Sil", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Bu kelimeyi kalıcı olarak silmek istediğine emin misin?",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Kelime: ${editingWord!!.word}", fontWeight = FontWeight.Bold)
                            Text("Anlam: ${formatMultiMeanings(editingWord!!.meaning)}")
                            Text("Seviye: ${editingWord!!.level}")
                            Text("Kütüphane: ${editingWord!!.library}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bu işlem geri alınamaz!", color = Color(0xFFD32F2F), fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = editingWord!!.id
                        showDeleteConfirm = false
                        editingWord = null
                        editMessage = "✅ Kelime silindi!"
                        showEditSuccess = true
                        onDeleteWord(id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                ) { Text("Evet, Sil") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("İptal") }
            }
        )
    }
}
