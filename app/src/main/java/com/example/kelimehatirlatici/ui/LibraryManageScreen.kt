package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.LibraryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryManageScreen(
    libraryInfoList: List<LibraryInfo>,
    onDeleteLibrary: (String) -> Unit,
    onRenameLibrary: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var deleteDialog by remember { mutableStateOf<String?>(null) }
    var renameDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kütüphaneleri Yönet") })
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = info.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

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
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Kalan: ${info.notLearnedCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            newName = info.name
                                            renameDialog = Pair(info.name, info.name)
                                        }
                                    ) {
                                        Text("Ad Değiştir")
                                    }

                                    Button(
                                        onClick = { deleteDialog = info.name },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Sil")
                                    }
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

    // ──────── SİLME ONAY DİALOGU ────────

    deleteDialog?.let { lib ->
        AlertDialog(
            onDismissRequest = { deleteDialog = null },
            title = { Text("Kütüphaneyi Sil") },
            text = {
                Text("«$lib» kütüphanesindeki tüm kelimeler silinecek. Emin misin?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLibrary(lib)
                        deleteDialog = null
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialog = null }) {
                    Text("İptal")
                }
            }
        )
    }

    // ──────── AD DEĞİŞTİRME DİALOGU ────────

    renameDialog?.let { (oldName, _) ->
        AlertDialog(
            onDismissRequest = {
                renameDialog = null
                newName = ""
            },
            title = { Text("Kütüphane Adını Değiştir") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Yeni ad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank() && newName != oldName) {
                            onRenameLibrary(oldName, newName)
                        }
                        renameDialog = null
                        newName = ""
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        renameDialog = null
                        newName = ""
                    }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}
