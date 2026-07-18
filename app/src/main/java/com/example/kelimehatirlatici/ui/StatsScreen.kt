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
fun StatsScreen(
    libraryInfoList: List<LibraryInfo>,
    totalLibraries: Int,
    totalWords: Int,
    totalLearned: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("İstatistikler") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            Text("Kütüphane Bazında", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (libraryInfoList.isEmpty()) {
                Text("Henüz kütüphane yok.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(libraryInfoList) { info ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Toplam: ${info.totalCount}")
                                    Text("✅ ${info.learnedCount}", color = MaterialTheme.colorScheme.primary)
                                    Text("📚 ${info.notLearnedCount}", color = MaterialTheme.colorScheme.error)
                                }
                                LinearProgressIndicator(
                                    progress = {
                                        if (info.totalCount > 0) info.learnedCount.toFloat() / info.totalCount.toFloat() else 0f
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // ────── GENEL ÖZET ──────
            Text("Genel Özet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("📁 Toplam kütüphane: $totalLibraries")
            Text("📖 Toplam kelime: $totalWords")
            Text("✅ Toplam öğrenilen: $totalLearned")

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }
}
