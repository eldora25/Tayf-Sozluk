package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.LibraryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySelectScreen(
    libraryInfoList: List<LibraryInfo>,
    selectedLibrary: String,
    onLibrarySelected: (String) -> Unit,
    onManageLibraries: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kütüphane Seç") },
                actions = {
                    TextButton(onClick = onManageLibraries) {
                        Text("Düzenle")
                    }
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
            if (libraryInfoList.isEmpty()) {
                Text("Henüz kütüphane yok.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(libraryInfoList) { info ->
                        val isSelected = info.name == selectedLibrary

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = if (isSelected)
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            else
                                CardDefaults.cardColors()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = info.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Toplam: ${info.totalCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Öğrenilen: ${info.learnedCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Kalan: ${info.notLearnedCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Button(
                            onClick = { onLibrarySelected(info.name) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isSelected) "Seçili: ${info.name}" else "Seç")
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
