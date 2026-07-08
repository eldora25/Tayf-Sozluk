package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImportScreen(
    onCsvImportClick: () -> Unit,
    onExcelImportClick: () -> Unit,
    onLingoesImportClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("İçe Aktar")
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
                onClick = onCsvImportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CSV İçe Aktar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onExcelImportClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Excel İçe Aktar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onLingoesImportClick,
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
}
