package com.example.kelimehatirlatici.importer

import android.content.Context
import android.net.Uri
import com.example.kelimehatirlatici.data.Word
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvImportHelper {

    fun importFromCsv(
        context: Context,
        uri: Uri
    ): List<Word> {
        val words = mutableListOf<Word>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var isFirstLine = true

                reader.lineSequence().forEach { line ->
                    if (isFirstLine) {
                        isFirstLine = false
                    } else {
                        val parts = parseCsvLine(line)

                        if (parts.size >= 2) {
                            words.add(
                                Word(
                                    word = parts.getOrNull(0)?.trim().orEmpty(),
                                    meaning = parts.getOrNull(1)?.trim().orEmpty(),
                                    example = parts.getOrNull(2)?.trim().orEmpty(),
                                    library = parts.getOrNull(3)?.trim().ifNullOrBlank("İçe Aktarılan CSV"),
                                    level = parts.getOrNull(4)?.trim().ifNullOrBlank("Genel")
                                )
                            )
                        }
                    }
                }
            }
        }

        return words.filter {
            it.word.isNotBlank() && it.meaning.isNotBlank()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false

        for (char in line) {
            when {
                char == '"' -> {
                    insideQuotes = !insideQuotes
                }

                char == ',' && !insideQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }

                else -> {
                    current.append(char)
                }
            }
        }

        result.add(current.toString())
        return result
    }

    private fun String?.ifNullOrBlank(defaultValue: String): String {
        return if (this.isNullOrBlank()) defaultValue else this
    }
}
