package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddWordScreen(
    onSave: (
        word: String,
        meaning: String,
        example: String,
        library: String,
        level: String
    ) -> Unit,
    onBack: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var library by remember { mutableStateOf("Genel") }
    var level by remember { mutableStateOf("Genel") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Kelime Ekle")
                }
            )
        }
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

            OutlinedTextField(
                value = meaning,
                onValueChange = { meaning = it },
                label = { Text("Anlam") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Örnek Cümle") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = library,
                onValueChange = { library = it },
                label = { Text("Kütüphane") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = level,
                onValueChange = { level = it },
                label = { Text("Seviye") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (word.isNotBlank() && meaning.isNotBlank()) {
                        onSave(
                            word.trim(),
                            meaning.trim(),
                            example.trim(),
                            library.trim().ifBlank { "Genel" },
                            level.trim().ifBlank { "Genel" }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Geri")
            }
        }
    }
}
