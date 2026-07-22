package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    libraries: List<String>,
    onSave: (word: String, meaning: String, example: String, library: String, level: String) -> Unit,
    onBack: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var selectedLibrary by remember { mutableStateOf(libraries.firstOrNull() ?: "Genel") }
    var level by remember { mutableStateOf("Genel") }

    var libraryExpanded by remember { mutableStateOf(false) }
    var showNewLibraryDialog by remember { mutableStateOf(false) }
    var newLibraryName by remember { mutableStateOf("") }

    val allLibraries = remember(libraries, newLibraryName) {
        val list = libraries.toMutableList()
        if (newLibraryName.isNotBlank() && newLibraryName !in list) {
            list.add(newLibraryName)
        }
        list
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kelime Ekle") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = word,
                onValueChange = { word = it },
                label = { Text("Kelime") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ★ Değişiklik: Anlam alanı - çoklu anlam desteği açıklaması eklendi
            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                label = { Text("Anlam") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        "Birden fazla anlam varsa ||| ile ayırın. Örn: özgür, hür|||serbest|||bedava",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ★ Değişiklik: Örnek cümle alanı - çoklu örnek desteği açıklaması eklendi
            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Örnek Cümle") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        "Birden fazla örnek varsa ||| ile ayırın. Örn: Örnek 1|||Örnek 2",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kütüphane dropdown
            ExposedDropdownMenuBox(
                expanded = libraryExpanded,
                onExpandedChange = { libraryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLibrary,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kütüphane") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = libraryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = libraryExpanded,
                    onDismissRequest = { libraryExpanded = false }
                ) {
                    allLibraries.forEach { lib ->
                        DropdownMenuItem(
                            text = { Text(lib) },
                            onClick = {
                                selectedLibrary = lib
                                libraryExpanded = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("➕ Yeni Kütüphane Oluştur") },
                        onClick = {
                            libraryExpanded = false
                            showNewLibraryDialog = true
                        }
                    )
                }
            }

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
                        selected = level == lvl,
                        onClick = { level = lvl },
                        label = { Text(lvl, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank()) {
                        val finalLevel = if (level.isBlank()) "Genel" else level
                        onSave(
                            word.trim(),
                            meaning.trim(),
                            example.trim(),
                            selectedLibrary.trim().ifBlank { "Genel" },
                            finalLevel
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kaydet") }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }

    // Yeni kütüphane dialogu
    if (showNewLibraryDialog) {
        AlertDialog(
            onDismissRequest = { showNewLibraryDialog = false; newLibraryName = "" },
            title = { Text("Yeni Kütüphane") },
            text = {
                OutlinedTextField(
                    value = newLibraryName,
                    onValueChange = { newLibraryName = it },
                    label = { Text("Kütüphane adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newLibraryName.isNotBlank()) {
                        selectedLibrary = newLibraryName.trim()
                    }
                    showNewLibraryDialog = false
                }) { Text("Oluştur") }
            },
            dismissButton = {
                TextButton(onClick = { showNewLibraryDialog = false; newLibraryName = "" }) { Text("İptal") }
            }
        )
    }
}
