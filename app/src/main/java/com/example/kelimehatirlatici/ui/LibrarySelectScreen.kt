package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibrarySelectScreen(
    libraries: List<String>,
    selectedLibrary: String,
    onLibrarySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Kütüphane Seç")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (libraries.isEmpty()) {
                Text("Henüz kütüphane yok.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(libraries) { library ->
                        val isSelected = library == selectedLibrary

                        if (isSelected) {
                            Button(
                                onClick = {
                                    onLibrarySelected(library)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(library)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    onLibrarySelected(library)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(library)
                            }
                        }
                    }
                }
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
