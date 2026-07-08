package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.packs.WordPack

@Composable
fun WordPackScreen(
    packs: List<WordPack>,
    onInstallPack: (WordPack) -> Unit,
    onInstallAll: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Hazır Paketler")
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
            if (packs.isEmpty()) {
                Text("Hazır paket bulunamadı.")
            } else {
                Button(
                    onClick = onInstallAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tüm Kütüphaneleri Yükle")
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(packs) { pack ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = pack.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(pack.description)
                                Text("Dil: ${pack.language} → ${pack.targetLanguage}")
                                Text("Seviye: ${pack.level}")
                                Text("Kelime sayısı: ${pack.words.size}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        onInstallPack(pack)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Bu Paketi Yükle")
                                }
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
