package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            OutlinedTextField(value = word, onValueChange = { word = it }, label = { Text("Kelime") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = meaning, onValueChange = { meaning = it }, label = { Text("Anlam") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = example, onValueChange = { example = it }, label = { Text("Örnek Cümle") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))

            // ────── KÜTÜPHANE DROPDOWN ──────
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
                    modifier = Modifier.fillMaxWidth().menuAnchor()
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
                    Divider()
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

            OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("Seviye") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank()) {
                        onSave(word.trim(), meaning.trim(), example.trim(), selectedLibrary.trim().ifBlank { "Genel" }, level.trim().ifBlank { "Genel" })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kaydet") }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }

    // ────── YENİ KÜTÜPHANE DİALOGU ──────
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
