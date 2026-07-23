package com.example.kelimehatirlatici.importer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.kelimehatirlatici.data.Word
import org.json.JSONArray

/**
 * Excel, CSV ve TXT dosyalarından kelime içe aktarma yardımcı sınıfı.
 * Apache POI kütüphanesi olmadan çalışır.
 * 
 * Desteklenen formatlar:
 * - CSV: word,meaning1|||meaning2|||...,example,level,library
 * - TXT (Lingoes): word: meaning1; meaning2
 * - TXT (FreeDict): word=meaning1|||meaning2|||...
 * - JSON: [{"word":"...", "meanings":[...], "library":"...", "level":"..."}]
 */
object ExcelImportHelper {

    private const val TAG = "ExcelImportHelper"

    /**
     * Seçilen dosyayı işler ve kelimeleri veritabanına ekler.
     * @return Pair<eklenenSayı, atlananSayı>
     */
    fun importFile(
        context: Context,
        repository: com.example.kelimehatirlatici.WordRepository,
        uri: Uri,
        fileType: String = "auto"
    ): Pair<Int, Int> {
        var imported = 0
        var skipped = 0

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Dosya açılamadı!")

            val content = inputStream.bufferedReader().use { it.readText() }
            val lines = content.lines().filter { it.isNotBlank() }

            Log.d(TAG, "Dosya okundu: ${lines.size} satır")

            // Her satırı işle
            for (line in lines) {
                try {
                    val word = parseLine(line.trim())
                    if (word != null) {
                        repository.addWord(word)
                        imported++
                        if (imported % 100 == 0) {
                            Log.d(TAG, "$imported kelime eklendi...")
                        }
                    } else {
                        skipped++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Satır atlandı: ${line.take(50)}... -> ${e.message}")
                    skipped++
                }
            }

            Log.d(TAG, "İşlem tamam: $imported eklendi, $skipped atlandı")
        } catch (e: Exception) {
            Log.e(TAG, "Dosya işleme hatası: ${e.message}", e)
            throw e
        }

        return Pair(imported, skipped)
    }

    /**
     * Bir satırı parse eder.
     * Sırasıyla dener:
     * 1. JSON formatı
     * 2. CSV formatı (virgülle ayrılmış)
     * 3. Lingoes TXT formatı (iki nokta üst üste ile ayrılmış)
     * 4. FreeDict formatı (eşittir ile ayrılmış)
     */
    private fun parseLine(line: String): Word? {
        // 1. JSON formatı
        if (line.trimStart().startsWith("{") && line.trimEnd().endsWith("}")) {
            return parseJsonLine(line)
        }

        // 2. CSV formatı - virgülle ayrılmış
        if (line.contains(",")) {
            val parts = parseCsvLine(line)
            if (parts.size >= 2) {
                val word = parts[0].trim('"', ' ').trim()
                val meaningField = parts[1].trim('"', ' ').trim()

                if (word.isNotBlank() && meaningField.isNotBlank()) {
                    val meanings = meaningField.split("|||")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    val firstMeaning = if (meanings.isNotEmpty()) meanings.first() else meaningField
                    val allMeanings = if (meanings.isNotEmpty()) meanings else listOf(meaningField)
                    val meaningsJson = JSONArray(allMeanings).toString()

                    val example = if (parts.size > 2) parts[2].trim('"', ' ').trim() else ""
                    val level = if (parts.size > 3) parts[3].trim('"', ' ').trim().ifBlank { "Genel" } else "Genel"
                    val library = if (parts.size > 4) parts[4].trim('"', ' ').trim().ifBlank { "Genel" } else "Genel"

                    val examplesJson = if (example.isNotBlank()) {
                        JSONArray(listOf(example)).toString()
                    } else "[]"

                    return Word(
                        word = word,
                        meaning = firstMeaning,
                        meanings = meaningsJson,
                        example = example,
                        examples = examplesJson,
                        library = library,
                        level = level
                    )
                }
            }
        }

        // 3. Lingoes TXT formatı: word: meaning1; meaning2
        if (line.contains(":")) {
            val colonIndex = line.indexOf(":")
            val word = line.substring(0, colonIndex).trim()
            val meaningPart = line.substring(colonIndex + 1).trim()

            if (word.isNotBlank() && meaningPart.isNotBlank()) {
                val meanings = meaningPart.split(";")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (meanings.isNotEmpty()) {
                    val firstMeaning = meanings.first()
                    val meaningsJson = JSONArray(meanings).toString()

                    return Word(
                        word = word,
                        meaning = firstMeaning,
                        meanings = meaningsJson,
                        example = "",
                        examples = "[]",
                        library = "Genel",
                        level = "Genel"
                    )
                }
            }
        }

        // 4. FreeDict TXT formatı: word=meaning1|||meaning2|||...
        if (line.contains("=")) {
            val eqIndex = line.indexOf("=")
            val word = line.substring(0, eqIndex).trim()
            val meaningPart = line.substring(eqIndex + 1).trim()

            if (word.isNotBlank() && meaningPart.isNotBlank()) {
                val meanings = meaningPart.split("|||")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                if (meanings.isNotEmpty()) {
                    val firstMeaning = meanings.first()
                    val meaningsJson = JSONArray(meanings).toString()

                    return Word(
                        word = word,
                        meaning = firstMeaning,
                        meanings = meaningsJson,
                        example = "",
                        examples = "[]",
                        library = "Genel",
                        level = "Genel"
                    )
                }
            }
        }

        return null
    }

    /**
     * JSON satırını parse eder.
     * Format: {"word":"...", "meanings":["..."], "example":"...", "library":"...", "level":"..."}
     */
    private fun parseJsonLine(line: String): Word? {
        return try {
            val obj = org.json.JSONObject(line)
            val word = obj.optString("word", "").trim()
            
            if (word.isBlank()) return null

            val meaning = obj.optString("meaning", "").trim()
            
            val meanings: List<String>
            val meaningsJson: String
            
            if (obj.has("meanings")) {
                val meaningsArr = obj.getJSONArray("meanings")
                meanings = (0 until meaningsArr.length())
                    .map { meaningsArr.getString(it).trim() }
                    .filter { it.isNotBlank() }
                meaningsJson = JSONArray(meanings).toString()
            } else {
                val singleMeaning = meaning.ifBlank { word }
                meanings = listOf(singleMeaning)
                meaningsJson = JSONArray(listOf(singleMeaning)).toString()
            }

            val example = obj.optString("example", "")
            val examples: String
            
            if (obj.has("examples")) {
                val examplesArr = obj.getJSONArray("examples")
                val examplesList = (0 until examplesArr.length())
                    .map { examplesArr.getString(it).trim() }
                    .filter { it.isNotBlank() }
                examples = JSONArray(examplesList).toString()
            } else {
                examples = if (example.isNotBlank()) JSONArray(listOf(example)).toString() else "[]"
            }

            val library = obj.optString("library", "Genel").ifBlank { "Genel" }
            val level = obj.optString("level", "Genel").ifBlank { "Genel" }

            Word(
                word = word,
                meaning = meaning.ifBlank { meanings.firstOrNull() ?: word },
                meanings = meaningsJson,
                example = example,
                examples = examples,
                library = library,
                level = level
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse hatası: ${e.message}")
            null
        }
    }

    /**
     * CSV satırını tırnak işaretlerini dikkate alarak parçalar.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())

        return result
    }
}
