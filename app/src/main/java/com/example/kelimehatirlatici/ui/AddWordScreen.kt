package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    libraries: List<String>,
    onSave: (word: String, meaning: String, meanings: List<String>, example: String, examples: List<String>, library: String, level: String) -> Unit,
    onBack: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meanings by remember { mutableStateOf(mutableListOf("")) }
    var examples by remember { mutableStateOf(mutableListOf("")) }
    var selectedLibrary by remember { mutableStateOf("Genel") }
    var selectedLevel by remember { mutableStateOf("Genel") }
    var showNewLibraryDialog by remember { mutableStateOf(false) }
    var newLibraryName by remember { mutableStateOf("") }
    val allLibraries = remember { mutableStateListOf<String>().apply { addAll(libraries) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelime Ekle") },
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
                value = word,
                onValueChange = { word = it },
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
                    value = selectedLibrary,
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
                                selectedLibrary = library
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

            Spacer(modifier = Modifier.height(24.dp))

            // Kaydet
            Button(
                onClick = {
                    if (word.isNotBlank() && meanings.any { it.isNotBlank() }) {
                        val filteredMeanings = meanings.filter { it.isNotBlank() }
                        val filteredExamples = examples.filter { it.isNotBlank() }
                        onSave(
                            word,
                            filteredMeanings.first(),
                            filteredMeanings,
                            filteredExamples.firstOrNull() ?: "",
                            filteredExamples,
                            selectedLibrary,
                            selectedLevel
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
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
                        selectedLibrary = newLibraryName
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
