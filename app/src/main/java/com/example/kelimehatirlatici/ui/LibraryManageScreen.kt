package com.example.kelimehatirlatici.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kelimehatirlatici.LibraryInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryManageScreen(
    libraryInfoList: List<LibraryInfo>,
    onDeleteLibrary: (String) -> Unit,
    onRenameLibrary: (String, String) -> Unit,
    onExportLibrary: (String) -> Unit,
    onBack: () -> Unit
) {
    var deleteConfirm by remember { mutableStateOf<String?>(null) }
    var renameDialog by remember { mutableStateOf<String?>(null) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kütüphaneleri Düzenle") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (libraryInfoList.isEmpty()) {
                Text("Henüz kütüphane yok.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(libraryInfoList) { info ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(info.name, fontWeight = FontWeight.Bold)
                                Text("Toplam: ${info.totalCount}  |  ✅ ${info.learnedCount}  |  📚 ${info.notLearnedCount}")

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    OutlinedButton(
                                        onClick = { newName = info.name; renameDialog = info.name },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("✏️ Ad Değiştir") }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedButton(
                                        onClick = { onExportLibrary(info.name) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("📤 Dışa Aktar") }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { deleteConfirm = info.name },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("🗑️ Sil") }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Geri") }
        }
    }

    // ────── SİLME ONAY DİALOGU ──────
    if (deleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            title = { Text("Kütüphaneyi Sil") },
            text = { Text("\"${deleteConfirm}\" kütüphanesini ve içindeki tüm kelimeleri silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteLibrary(deleteConfirm!!)
                    deleteConfirm = null
                }) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteConfirm = null }) { Text("İptal") } }
        )
    }

    // ────── AD DEĞİŞTİRME DİALOGU ──────
    if (renameDialog != null) {
        AlertDialog(
            onDismissRequest = { renameDialog = null },
            title = { Text("Kütüphane Adını Değiştir") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Yeni ad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        onRenameLibrary(renameDialog!!, newName.trim())
                    }
                    renameDialog = null
                }) { Text("Değiştir") }
            },
            dismissButton = { TextButton(onClick = { renameDialog = null }) { Text("İptal") } }
        )
    }
}
