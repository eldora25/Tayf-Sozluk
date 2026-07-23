package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.data.Word
import org.json.JSONArray

enum class WordEditAction {
    UPDATE, COPY, MOVE, DELETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordEditScreen(
    word: Word,
    libraries: List<String>,
    onSave: (word: String, meaning: String, meanings: List<String>, example: String, examples: List<String>, level: String, library: String) -> Unit,
    onAction: (action: WordEditAction, newLibrary: String?) -> Unit,
    onBack: () -> Unit
) {
    // Mevcut anlamları JSON'dan parse et
    val initialMeanings = remember {
        try {
            val arr = JSONArray(word.meanings)
            (0 until arr.length()).map { arr.getString(it) }.toMutableList()
        } catch (e: Exception) {
            mutableListOf(word.meaning)
        }
    }

    val initialExamples = remember {
        try {
            val arr = JSONArray(word.examples)
            (0 until arr.length()).map { arr.getString(it) }.toMutableList()
        } catch (e: Exception) {
            if (word.example.isNotBlank()) mutableListOf(word.example) else mutableListOf()
        }
    }

    var editWord by remember { mutableStateOf(word.word) }
    var meanings by remember { mutableStateOf(initialMeanings) }
    var examples by remember { mutableStateOf(initialExamples) }
    var selectedLevel by remember { mutableStateOf(word.level) }
    var selectedLibrary by remember { mutableStateOf(word.library) }
    var selectedAction by remember { mutableStateOf(WordEditAction.UPDATE) }
    var showNewLibraryDialog by remember { mutableStateOf(false) }
    var targetLibrary by remember { mutableStateOf(word.library) }
    var newLibraryName by remember { mutableStateOf("") }
    val allLibraries = remember { mutableStateListOf<String>().apply { addAll(libraries) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelimeyi Düzenle") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Kelime
            OutlinedTextField(
                value = editWord,
                onValueChange = { editWord = it },
                label = { Text("Kelime") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Çoklu Anlam
            Text("Anlam(lar)", style = MaterialTheme.typography.titleSmall)
            meanings.forEachIndexed { index, meaning ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = meaning,
                        onValueChange = { meanings[index] = it },
                        label = { Text("Anlam ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    if (meanings.size > 1) {
                        IconButton(onClick = { meanings.removeAt(index) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Kaldır")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            TextButton(onClick = { meanings.add("") }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Anlam Ekle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Çoklu Örnek Cümle
            Text("Örnek Cümle(ler)", style = MaterialTheme.typography.titleSmall)
            examples.forEachIndexed { index, example ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = example,
                        onValueChange = { examples[index] = it },
                        label = { Text("Örnek ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    if (examples.size > 1) {
                        IconButton(onClick = { examples.removeAt(index) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Kaldır")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            TextButton(onClick = { examples.add("") }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Örnek Cümle Ekle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seviye
            Text("Seviye", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2", "Genel")
                levels.forEach { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text(level, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kütüphane
            Text("Kütüphane", style = MaterialTheme.typography.titleSmall)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = targetLibrary,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kütüphane") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    allLibraries.forEach { library ->
                        DropdownMenuItem(
                            text = { Text(library) },
                            onClick = {
                                targetLibrary = library
                                expanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ Yeni Kütüphane", color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            expanded = false
                            showNewLibraryDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İşlem Türü
            Text("İşlem Türü", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                wordEditActions.forEach { action ->
                    FilterChip(
                        selected = selectedAction == action,
                        onClick = { selectedAction = action },
                        label = { Text(action.label, style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Butonlar
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("İptal")
                }

                Button(
                    onClick = {
                        if (editWord.isNotBlank() && meanings.any { it.isNotBlank() }) {
                            when (selectedAction) {
                                WordEditAction.UPDATE -> {
                                    val filteredMeanings = meanings.filter { it.isNotBlank() }
                                    val filteredExamples = examples.filter { it.isNotBlank() }
                                    onSave(
                                        editWord,
                                        filteredMeanings.first(),
                                        filteredMeanings,
                                        filteredExamples.firstOrNull() ?: "",
                                        filteredExamples,
                                        selectedLevel,
                                        targetLibrary
                                    )
                                }
                                WordEditAction.COPY,
                                WordEditAction.MOVE,
                                WordEditAction.DELETE -> {
                                    onAction(selectedAction, targetLibrary)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Kaydet")
                }
            }
        }
    }

    // Yeni Kütüphane Dialog
    if (showNewLibraryDialog) {
        AlertDialog(
            onDismissRequest = { showNewLibraryDialog = false },
            title = { Text("Yeni Kütüphane") },
            text = {
                OutlinedTextField(
                    value = newLibraryName,
                    onValueChange = { newLibraryName = it },
                    label = { Text("Kütüphane Adı") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newLibraryName.isNotBlank()) {
                        allLibraries.add(newLibraryName)
                        targetLibrary = newLibraryName
                        newLibraryName = ""
                        showNewLibraryDialog = false
                    }
                }) {
                    Text("Oluştur")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewLibraryDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// İşlem türü verileri
private data class WordEditActionData(
    val action: WordEditAction,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val wordEditActions = listOf(
    WordEditActionData(WordEditAction.UPDATE, "Güncelle", Icons.Default.Edit),
    WordEditActionData(WordEditAction.COPY, "Kopyala", Icons.Default.ContentCopy),
    WordEditActionData(WordEditAction.MOVE, "Taşı", Icons.Default.DriveFileMove),
    WordEditActionData(WordEditAction.DELETE, "Sil", Icons.Default.Delete)
)
