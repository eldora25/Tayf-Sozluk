package com.example.kelimehatirlatici.importer

import android.content.Context
import android.net.Uri
import com.example.kelimehatirlatici.data.Word
import java.io.BufferedReader
import java.io.InputStreamReader

object LingoesImportHelper {

    fun importFromLingoesText(
        context: Context,
        uri: Uri
    ): List<Word> {
        val words = mutableListOf<Word>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val cleaned = line.trim()

                    if (cleaned.isNotBlank()) {
                        val pair = splitLine(cleaned)

                        if (pair != null) {
                            words.add(
                                Word(
                                    word = pair.first,
                                    meaning = pair.second,
                                    example = "",
                                    library = "Lingoes TXT",
                                    level = "Genel"
                                )
                            )
                        }
                    }
                }
            }
        }

        return words
    }

    private fun splitLine(line: String): Pair<String, String>? {
        val separators = listOf(" - ", "\t", ":", "=")

        for (separator in separators) {
            if (line.contains(separator)) {
                val parts = line.split(separator, limit = 2)
                val word = parts.getOrNull(0)?.trim().orEmpty()
                val meaning = parts.getOrNull(1)?.trim().orEmpty()

                if (word.isNotBlank() && meaning.isNotBlank()) {
                    return word to meaning
                }
            }
        }

        return null
    }
}
