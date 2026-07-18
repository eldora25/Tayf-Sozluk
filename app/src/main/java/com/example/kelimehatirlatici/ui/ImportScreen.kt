package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onCsvImportClick: (libraryName: String) -> Unit,
    onExcelImportClick: (libraryName: String) -> Unit,
    onLingoesImportClick: (libraryName: String) -> Unit,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf("") }
    var libraryName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("İçe Aktar") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Desteklenen formatlar:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("CSV: word,meaning,example,library,level")
            Text("Excel: İlk sayfa, ilk 5 sütun")
            Text("Lingoes TXT: kelime - anlam formatı")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    pendingAction = "csv"
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CSV İçe Aktar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    pendingAction = "excel"
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Excel İçe Aktar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    pendingAction = "lingoes"
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lingoes TXT İçe Aktar")
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Geri")
            }
        }
    }

    // ──────── KÜTÜPHANE İSMİ SORMA DİALOGU ────────

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                libraryName = ""
            },
            title = { Text("Kütüphane Adı") },
            text = {
                OutlinedTextField(
                    value = libraryName,
                    onValueChange = { libraryName = it },
                    label = { Text("Kütüphane adı girin") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = libraryName.ifBlank { "İçe Aktarılan" }
                        showDialog = false
                        libraryName = ""
                        when (pendingAction) {
                            "csv" -> onCsvImportClick(name)
                            "excel" -> onExcelImportClick(name)
                            "lingoes" -> onLingoesImportClick(name)
                        }
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        libraryName = ""
                    }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}
